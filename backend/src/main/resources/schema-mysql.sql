-- UrbanNexus MySQL Procedural Logic
-- Run this file manually after the schema is initialized when using MySQL.
-- Spring Boot SQL init does not support the DELIMITER syntax required here.

DELIMITER //

DROP PROCEDURE IF EXISTS AutoBookTechnician //
CREATE PROCEDURE AutoBookTechnician(
    IN p_resident_id INT,
    IN p_skill VARCHAR(50),
    IN p_slot INT,
    IN p_assign_date DATE
)
BEGIN
    DECLARE v_tech_id INT;
    DECLARE v_trans_no VARCHAR(50);
    DECLARE v_base_price DECIMAL(10,2);

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

    SELECT base_price INTO v_base_price
    FROM pricing
    WHERE item_name = p_skill
      AND category = 'Technician';

    SET v_trans_no = CONCAT('TXN-TECH-', FLOOR(RAND() * 899999 + 100000));
    INSERT INTO payment (trans_no, status, type, cost, payment_date)
    VALUES (v_trans_no, 'Pending', 'Technician', ROUND(v_base_price * 1.18, 2), NOW());

    INSERT INTO technician_management (resident_id, tech_id, trans_no, slot, assign_date, status)
    VALUES (p_resident_id, v_tech_id, v_trans_no, p_slot, p_assign_date, 'Assigned');

    SELECT LAST_INSERT_ID() as assignment_id, v_trans_no as trans_no;
END //

DROP PROCEDURE IF EXISTS AutoBookAmenity //
CREATE PROCEDURE AutoBookAmenity(
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

    SELECT name INTO v_amenity_name
    FROM amenity
    WHERE amenity_id = p_amenity_id;

    SELECT base_price INTO v_base_price
    FROM pricing
    WHERE item_name = v_amenity_name
      AND category = 'Amenity';

    SET v_trans_no = CONCAT('TXN-AMEN-', FLOOR(RAND() * 899999 + 100000));
    INSERT INTO payment (trans_no, status, type, cost, payment_date)
    VALUES (v_trans_no, 'Pending', 'Amenity', ROUND(v_base_price * 1.18, 2), NOW());

    INSERT INTO amenity_mgmt (resident_id, amenity_id, trans_no, date, slot, capacity_booked, status)
    VALUES (p_resident_id, p_amenity_id, v_trans_no, p_date, p_slot, p_capacity_booked, 'Confirmed');

    SELECT v_trans_no as trans_no;
END //

DROP PROCEDURE IF EXISTS ProcessOverduePayments //
CREATE PROCEDURE ProcessOverduePayments()
BEGIN
    UPDATE payment
    SET status = 'Overdue'
    WHERE status = 'Pending'
      AND payment_date < NOW() - INTERVAL '30' DAY;
END //

-- Trigger: audit resident deletions at DB level when using MySQL directly
DROP TRIGGER IF EXISTS LogResidentDeletion //
CREATE TRIGGER LogResidentDeletion
    AFTER DELETE ON resident
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (table_affected, record_id, action_type, details)
    VALUES ('resident', OLD.resident_id, 'DELETE', CONCAT('Resident ', OLD.name, ' removed from system'));
END //

DELIMITER ;
