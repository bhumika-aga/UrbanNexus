-- UrbanNexus MySQL Procedural Logic
-- This file is executed only when the 'mysql' platform is active.

DELIMITER //

DROP
PROCEDURE IF EXISTS AutoBookTechnician //
CREATE
PROCEDURE AutoBookTechnician(
    IN p_resident_id INT,
    IN p_skill VARCHAR(50),
    IN p_slot INT,
    IN p_assign_date DATE
)
BEGIN
    DECLARE v_tech_id INT;
DECLARE v_trans_no VARCHAR(50);
DECLARE v_base_price DECIMAL(10,2);
    
    -- 1. Find an available technician with the required skill
SELECT t.tech_id INTO v_tech_id
FROM technician t
WHERE t.skill = p_skill
  AND t.available = 1
  AND NOT EXISTS (SELECT 1
                  FROM technician_management tm
                  WHERE tm.tech_id = t.tech_id
                    AND tm.assign_date = p_assign_date
                    AND tm.slot = p_slot)
LIMIT 1;

IF v_tech_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
SET MESSAGE_TEXT = 'No available technicians for the requested slot';
END IF;

    -- 2. Fetch pricing
SELECT base_price INTO v_base_price
FROM pricing
WHERE item_name = p_skill
  AND category = 'Technician';

-- 3. Generate Transaction No and Create Payment
SET v_trans_no = CONCAT('TXN-TECH-', FLOOR(RAND() * 899999 + 100000));
INSERT INTO payment (trans_no, status, type, cost, payment_date)
VALUES (v_trans_no, 'Pending', 'Technician', v_base_price, NOW());

-- 4. Create Assignment
INSERT INTO technician_management (resident_id, tech_id, trans_no, slot, assign_date, status)
VALUES (p_resident_id, v_tech_id, v_trans_no, p_slot, p_assign_date, 'Assigned');

-- Return result set for @Procedure mapping
SELECT LAST_INSERT_ID() as assignment_id, v_trans_no as trans_no;
END //

DROP
PROCEDURE IF EXISTS AutoBookAmenity //
CREATE
PROCEDURE AutoBookAmenity(
    IN p_resident_id INT,
    IN p_amenity_id INT,
    IN p_date DATE,
    IN p_slot INT,
    IN p_capacity_booked INT
)
BEGIN
    DECLARE v_trans_no VARCHAR(50);
DECLARE v_base_price DECIMAL(10,2);
DECLARE v_amenity_name VARCHAR(100);
    
    -- 1. Validate Amenity and Fetch Name
SELECT name INTO v_amenity_name
FROM amenity
WHERE amenity_id = p_amenity_id;

-- 2. Fetch Pricing
SELECT base_price INTO v_base_price
FROM pricing
WHERE item_name = v_amenity_name
  AND category = 'Amenity';

-- 3. Create Payment
SET v_trans_no = CONCAT('TXN-AMEN-', FLOOR(RAND() * 899999 + 100000));
INSERT INTO payment (trans_no, status, type, cost, payment_date)
VALUES (v_trans_no, 'Pending', 'Amenity', v_base_price, NOW());

-- 4. Create Booking
INSERT INTO amenity_mgmt (booking_id, resident_id, amenity_id, trans_no, date, slot, capacity_booked, status)
VALUES (CONCAT('BKG-', FLOOR(RAND() * 899999 + 100000)), p_resident_id, p_amenity_id, v_trans_no, p_date, p_slot,
        p_capacity_booked, 'Confirmed');

SELECT v_trans_no as trans_no;
END //

DROP
PROCEDURE IF EXISTS GetResidentPendingDues //
CREATE
PROCEDURE GetResidentPendingDues(IN p_resident_id BIGINT)
BEGIN
SELECT p.trans_no,
       p.status,
       p.type as service_type,
       p.cost,
       p.payment_date
FROM payment p
         LEFT JOIN amenity_mgmt am ON p.trans_no = am.trans_no
         LEFT JOIN technician_management tm ON p.trans_no = tm.trans_no
WHERE (am.resident_id = p_resident_id OR tm.resident_id = p_resident_id)
  AND p.status IN ('Pending', 'Overdue');
END //

DROP
PROCEDURE IF EXISTS ProcessOverduePayments //
CREATE
PROCEDURE ProcessOverduePayments()
BEGIN
UPDATE payment
SET status = 'Overdue'
WHERE status = 'Pending'
  AND payment_date < DATE_SUB(NOW(), INTERVAL 30 DAY);
END //

-- -----------------------------------------------------
-- Triggers
-- -----------------------------------------------------

DROP TRIGGER IF EXISTS LogResidentDeletion
//
CREATE TRIGGER LogResidentDeletion
    AFTER DELETE
    ON resident
    FOR EACH ROW
    BEGIN
INSERT INTO audit_log (table_affected, record_id, action_type, details)
VALUES ('resident', OLD.resident_id, 'DELETE', CONCAT('Resident ', OLD.name, ' removed from system'));
END //

DELIMITER ;
