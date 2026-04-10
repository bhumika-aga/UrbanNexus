-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema UrbanNexus
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `UrbanNexus` ;
CREATE SCHEMA IF NOT EXISTS `UrbanNexus` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `UrbanNexus` ;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`amenity`
-- -----------------------------------------------------
-- REFACTOR: Removed static slot columns. Capacity handles booking limits.
DROP TABLE IF EXISTS `UrbanNexus`.`amenity` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`amenity` (
                                                      `amenity_id` INT NOT NULL,
                                                      `name` VARCHAR(100) NOT NULL,
                                                      `capacity` INT NULL DEFAULT NULL,
                                                      PRIMARY KEY (`amenity_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`resident`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UrbanNexus`.`resident` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`resident` (
                                                       `resident_id` INT NOT NULL AUTO_INCREMENT,
                                                       `name` VARCHAR(100) NOT NULL,
                                                       `house_block` VARCHAR(10) NULL DEFAULT NULL,
                                                       `house_floor` VARCHAR(10) NULL DEFAULT NULL,
                                                       `house_unit` VARCHAR(10) NULL DEFAULT NULL,
                                                       `ownership_status` VARCHAR(50) NULL DEFAULT NULL,
                                                       `contact` VARCHAR(20) NULL DEFAULT NULL,
                                                       `no_of_members` INT NULL DEFAULT NULL,
                                                       PRIMARY KEY (`resident_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`payment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UrbanNexus`.`payment` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`payment` (
                                                      `trans_no` VARCHAR(50) NOT NULL,
                                                      `status` VARCHAR(50) NULL DEFAULT NULL,
                                                      `type` VARCHAR(50) NULL DEFAULT NULL,
                                                      `cost` DECIMAL(10,2) NULL DEFAULT NULL,
                                                      PRIMARY KEY (`trans_no`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`amenity_mgmt`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UrbanNexus`.`amenity_mgmt` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`amenity_mgmt` (
                                                           `booking_id` VARCHAR(50) NOT NULL,
                                                           `resident_id` INT NULL DEFAULT NULL,
                                                           `amenity_id` INT NULL DEFAULT NULL,
                                                           `trans_no` VARCHAR(50) NULL DEFAULT NULL,
                                                           `date` DATE NULL DEFAULT NULL,
                                                           `status` VARCHAR(50) NULL DEFAULT NULL,
                                                           `capacity_booked` INT NULL DEFAULT NULL,
                                                           `slot` INT NULL DEFAULT NULL,
                                                           PRIMARY KEY (`booking_id`),
                                                           INDEX `resident_id` (`resident_id` ASC) VISIBLE,
                                                           INDEX `amenity_id` (`amenity_id` ASC) VISIBLE,
                                                           INDEX `trans_no` (`trans_no` ASC) VISIBLE,
                                                           CONSTRAINT `amenity_mgmt_ibfk_1`
                                                               FOREIGN KEY (`resident_id`)
                                                                   REFERENCES `UrbanNexus`.`resident` (`resident_id`)
                                                                   ON DELETE CASCADE,
                                                           CONSTRAINT `amenity_mgmt_ibfk_2`
                                                               FOREIGN KEY (`amenity_id`)
                                                                   REFERENCES `UrbanNexus`.`amenity` (`amenity_id`)
                                                                   ON DELETE CASCADE,
                                                           CONSTRAINT `amenity_mgmt_ibfk_3`
                                                               FOREIGN KEY (`trans_no`)
                                                                   REFERENCES `UrbanNexus`.`payment` (`trans_no`)
                                                                   ON DELETE SET NULL)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`technician`
-- -----------------------------------------------------
-- REFACTOR: Removed static slots. Availability is determined dynamically.
DROP TABLE IF EXISTS `UrbanNexus`.`technician` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`technician` (
                                                         `tech_id` INT NOT NULL,
                                                         `name` VARCHAR(100) NOT NULL,
                                                         `contact` VARCHAR(20) NULL DEFAULT NULL,
                                                         `skill` VARCHAR(50) NULL DEFAULT NULL,
                                                         PRIMARY KEY (`tech_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`technician_management`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UrbanNexus`.`technician_management` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`technician_management` (
                                                                    `assignment_id` INT NOT NULL AUTO_INCREMENT,
                                                                    `resident_id` INT NULL DEFAULT NULL,
                                                                    `tech_id` INT NULL DEFAULT NULL,
                                                                    `trans_no` VARCHAR(50) NULL DEFAULT NULL,
                                                                    `status` VARCHAR(50) NULL DEFAULT NULL,
                                                                    `slot` INT NOT NULL,
                                                                    `assign_date` DATE NOT NULL,
                                                                    PRIMARY KEY (`assignment_id`),
                                                                    INDEX `resident_id` (`resident_id` ASC) VISIBLE,
                                                                    INDEX `tech_id` (`tech_id` ASC) VISIBLE,
                                                                    INDEX `trans_no` (`trans_no` ASC) VISIBLE,
                                                                    CONSTRAINT `technician_management_ibfk_1`
                                                                        FOREIGN KEY (`resident_id`)
                                                                            REFERENCES `UrbanNexus`.`resident` (`resident_id`)
                                                                            ON DELETE CASCADE,
                                                                    CONSTRAINT `technician_management_ibfk_2`
                                                                        FOREIGN KEY (`tech_id`)
                                                                            REFERENCES `UrbanNexus`.`technician` (`tech_id`)
                                                                            ON DELETE CASCADE,
                                                                    CONSTRAINT `technician_management_ibfk_3`
                                                                        FOREIGN KEY (`trans_no`)
                                                                            REFERENCES `UrbanNexus`.`payment` (`trans_no`)
                                                                            ON DELETE SET NULL)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`admin`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UrbanNexus`.`admin` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`admin` (
                                                    `admin_id` INT NOT NULL AUTO_INCREMENT,
                                                    `username` VARCHAR(50) NOT NULL UNIQUE,
                                                    `password_hash` VARCHAR(255) NOT NULL,
                                                    `role` ENUM('SuperAdmin', 'Technician', 'Resident') NOT NULL,
                                                    PRIMARY KEY (`admin_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`pricing`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UrbanNexus`.`pricing` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`pricing` (
                                                      `item_name` VARCHAR(50) NOT NULL,
                                                      `category` ENUM('Technician', 'Amenity') NOT NULL,
                                                      `base_price` DECIMAL(10,2) NOT NULL,
                                                      PRIMARY KEY (`item_name`, `category`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;


-- -----------------------------------------------------
-- ADVANCED DBMS CONCEPTS
-- -----------------------------------------------------

-- 1. TRIGGER: Prevent overbooking an amenity
DELIMITER //
DROP TRIGGER IF EXISTS CheckAmenityCapacity//
CREATE TRIGGER CheckAmenityCapacity
    BEFORE INSERT ON `UrbanNexus`.`amenity_mgmt`
    FOR EACH ROW
BEGIN
    DECLARE max_capacity INT;

    SELECT capacity INTO max_capacity
    FROM `UrbanNexus`.`amenity`
    WHERE amenity_id = NEW.amenity_id;

    IF NEW.capacity_booked > max_capacity THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Booking failed: Capacity requested exceeds the maximum amenity capacity.';
    END IF;
END//
DELIMITER ;

-- 2. STORED PROCEDURE WITH CURSOR: Process Pending Payments
DELIMITER //
DROP PROCEDURE IF EXISTS ProcessOverduePayments//
CREATE PROCEDURE ProcessOverduePayments()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE current_trans_no VARCHAR(50);
    DECLARE current_status VARCHAR(50);

    DECLARE payment_cursor CURSOR FOR
        SELECT trans_no, status
        FROM `UrbanNexus`.`payment`
        WHERE status = 'Pending';

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN payment_cursor;

    read_loop: LOOP
        FETCH payment_cursor INTO current_trans_no, current_status;

        IF done THEN
            LEAVE read_loop;
        END IF;

        UPDATE `UrbanNexus`.`payment`
        SET status = 'Overdue'
        WHERE trans_no = current_trans_no;

    END LOOP;

    CLOSE payment_cursor;
END//
DELIMITER ;

-- 3. STORED PROCEDURE: Get Resident Pending Dues
DELIMITER //
DROP PROCEDURE IF EXISTS GetResidentPendingDues//
CREATE PROCEDURE GetResidentPendingDues(IN p_resident_id INT)
BEGIN
    SELECT
        p.trans_no,
        p.type AS service_type,
        p.cost,
        p.status
    FROM `UrbanNexus`.`payment` p
             LEFT JOIN `UrbanNexus`.`amenity_mgmt` am ON p.trans_no = am.trans_no
             LEFT JOIN `UrbanNexus`.`technician_management` tm ON p.trans_no = tm.trans_no
    WHERE (am.resident_id = p_resident_id OR tm.resident_id = p_resident_id)
      AND p.status = 'Pending';
END//
DELIMITER ;

-- 4. STORED PROCEDURE: Safe Book Amenity
DELIMITER //
DROP PROCEDURE IF EXISTS SafeBookAmenity//
CREATE PROCEDURE SafeBookAmenity(
    IN p_booking_id VARCHAR(50),
    IN p_resident_id INT,
    IN p_amenity_id INT,
    IN p_date DATE,
    IN p_capacity INT,
    IN p_slot INT
)
BEGIN
    START TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM `UrbanNexus`.`resident` WHERE resident_id = p_resident_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking Failed: Resident ID does not exist.';
        ROLLBACK;
    ELSE
        INSERT INTO `UrbanNexus`.`amenity_mgmt`
        (booking_id, resident_id, amenity_id, date, status, capacity_booked, slot)
        VALUES
            (p_booking_id, p_resident_id, p_amenity_id, p_date, 'Confirmed', p_capacity, p_slot);

        COMMIT;
    END IF;
END//
DELIMITER ;

-- 5. TRIGGER: Automatically calculate and add 18% GST to new invoices
DELIMITER //
DROP TRIGGER IF EXISTS CalculateGSTBeforePayment//
CREATE TRIGGER CalculateGSTBeforePayment
    BEFORE INSERT ON `UrbanNexus`.`payment`
    FOR EACH ROW
BEGIN
    IF NEW.cost IS NOT NULL THEN
        SET NEW.cost = NEW.cost * 1.18;
    END IF;
END//
DELIMITER ;

-- 6. STORED PROCEDURE: Auto-Book Technician & Generate Invoice
DELIMITER //
DROP PROCEDURE IF EXISTS AutoBookTechnician//
CREATE PROCEDURE AutoBookTechnician(
    IN p_resident_id INT,
    IN p_skill VARCHAR(50),
    IN p_slot INT,
    IN p_assign_date DATE
)
BEGIN
    DECLARE v_tech_id INT;
    DECLARE v_trans_no VARCHAR(50);
    DECLARE v_base_cost DECIMAL(10,2);

    -- STEP 1: Dynamically fetch the price
    SELECT base_price INTO v_base_cost
    FROM `UrbanNexus`.`pricing`
    WHERE item_name = p_skill AND category = 'Technician' LIMIT 1;

    IF v_base_cost IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking Failed: Invalid skill or no pricing found.';
    END IF;

    -- STEP 2: REFACTORED Dynamic Availability Check
    -- Find a technician with this skill who does NOT have an assignment on this date at this slot
    SELECT t.tech_id INTO v_tech_id
    FROM `UrbanNexus`.`technician` t
    WHERE t.skill = p_skill
      AND NOT EXISTS (
        SELECT 1
        FROM `UrbanNexus`.`technician_management` tm
        WHERE tm.tech_id = t.tech_id
          AND tm.assign_date = p_assign_date
          AND tm.slot = p_slot
    )
    LIMIT 1;

    -- STEP 3: Proceed with booking
    IF v_tech_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking Failed: No available technicians with that skill for the requested date and slot.';
    ELSE
        START TRANSACTION;

        SET v_trans_no = CONCAT('TXN-TECH-', FLOOR(RAND() * 100000));

        INSERT INTO `UrbanNexus`.`payment` (trans_no, status, type, cost)
        VALUES (v_trans_no, 'Pending', 'Technician', v_base_cost);

        -- Inserting here automatically makes them unavailable for this date/slot in future searches!
        INSERT INTO `UrbanNexus`.`technician_management`
        (resident_id, tech_id, trans_no, status, slot, assign_date)
        VALUES
            (p_resident_id, v_tech_id, v_trans_no, 'Assigned', p_slot, p_assign_date);

        COMMIT;

        SELECT
            tm.assignment_id,
            t.name AS technician_name,
            t.contact AS technician_contact,
            p.trans_no,
            v_base_cost AS base_price,
            p.cost AS total_with_gst,
            tm.assign_date,
            tm.slot
        FROM `UrbanNexus`.`technician_management` tm
                 JOIN `UrbanNexus`.`technician` t ON tm.tech_id = t.tech_id
                 JOIN `UrbanNexus`.`payment` p ON tm.trans_no = p.trans_no
        WHERE tm.trans_no = v_trans_no;

    END IF;
END//
DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;