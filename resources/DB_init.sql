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
DROP TABLE IF EXISTS `UrbanNexus`.`amenity` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`amenity` (
                                                      `amenity_id` INT NOT NULL,
                                                      `name` VARCHAR(100) NOT NULL,
    `capacity` INT NULL DEFAULT NULL,
    `slot_1` TINYINT(1) NULL DEFAULT '0',
    `slot_2` TINYINT(1) NULL DEFAULT '0',
    `slot_3` TINYINT(1) NULL DEFAULT '0',
    `slot_4` TINYINT(1) NULL DEFAULT '0',
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
    `job_id` VARCHAR(50) NULL DEFAULT NULL,
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
                                                           `job_id` VARCHAR(50) NOT NULL,
    `resident_id` INT NULL DEFAULT NULL,
    `amenity_id` INT NULL DEFAULT NULL,
    `trans_no` VARCHAR(50) NULL DEFAULT NULL,
    `date` DATE NULL DEFAULT NULL,
    `status` VARCHAR(50) NULL DEFAULT NULL,
    `capacity_booked` INT NULL DEFAULT NULL,
    `slot` INT NULL DEFAULT NULL,
    PRIMARY KEY (`job_id`),
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
DROP TABLE IF EXISTS `UrbanNexus`.`technician` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`technician` (
                                                         `tech_id` INT NOT NULL,
                                                         `name` VARCHAR(100) NOT NULL,
    `contact` VARCHAR(20) NULL DEFAULT NULL,
    `skill` VARCHAR(50) NULL DEFAULT NULL,
    `slot_1` TINYINT(1) NULL DEFAULT '0',
    `slot_2` TINYINT(1) NULL DEFAULT '0',
    `slot_3` TINYINT(1) NULL DEFAULT '0',
    `slot_4` TINYINT(1) NULL DEFAULT '0',
    PRIMARY KEY (`tech_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `UrbanNexus`.`technician_management`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UrbanNexus`.`technician_management` ;

CREATE TABLE IF NOT EXISTS `UrbanNexus`.`technician_management` (
                                                                    `management_id` INT NOT NULL AUTO_INCREMENT,
                                                                    `resident_id` INT NULL DEFAULT NULL,
                                                                    `tech_id` INT NULL DEFAULT NULL,
                                                                    `trans_no` VARCHAR(50) NULL DEFAULT NULL,
    `status` VARCHAR(50) NULL DEFAULT NULL,
    `slot` INT NOT NULL,
    `assign_date` DATE NOT NULL,
    PRIMARY KEY (`management_id`),
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

    -- Get the total capacity of the requested amenity
    SELECT capacity INTO max_capacity
    FROM `UrbanNexus`.`amenity`
    WHERE amenity_id = NEW.amenity_id;

    -- If the requested booking exceeds the limit, throw an error
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

    -- Declare the cursor to find all pending payments
    DECLARE payment_cursor CURSOR FOR
SELECT trans_no, status
FROM `UrbanNexus`.`payment`
WHERE status = 'Pending';

-- Declare continue handler to exit the loop when no more rows exist
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

OPEN payment_cursor;

read_loop: LOOP
        FETCH payment_cursor INTO current_trans_no, current_status;

        IF done THEN
            LEAVE read_loop;
END IF;

        -- Logic: Update the status from Pending to Overdue
        -- In a real app, you might check dates, but this fulfills the cursor requirement
UPDATE `UrbanNexus`.`payment`
SET status = 'Overdue'
WHERE trans_no = current_trans_no;

END LOOP;

CLOSE payment_cursor;
END//
DELIMITER ;

DELIMITER //
DROP PROCEDURE IF EXISTS GetResidentPendingDues//
CREATE PROCEDURE GetResidentPendingDues(IN p_resident_id INT)
BEGIN
    -- This query joins payments with both amenity and technician management
    -- to find all pending transactions for a specific resident.
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


DELIMITER //
DROP PROCEDURE IF EXISTS SafeBookAmenity//
CREATE PROCEDURE SafeBookAmenity(
    IN p_job_id VARCHAR(50),
    IN p_resident_id INT,
    IN p_amenity_id INT,
    IN p_date DATE,
    IN p_capacity INT,
    IN p_slot INT
)
BEGIN
    -- Start a transaction to ensure data integrity
START TRANSACTION;

-- Check if the resident actually exists in the database
IF NOT EXISTS (SELECT 1 FROM `UrbanNexus`.`resident` WHERE resident_id = p_resident_id) THEN
        -- Throw an error and rollback if they don't exist
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking Failed: Resident ID does not exist.';
ROLLBACK;
ELSE
        -- If they exist, proceed with the booking
        INSERT INTO `UrbanNexus`.`amenity_mgmt`
        (job_id, resident_id, amenity_id, date, status, capacity_booked, slot)
        VALUES
        (p_job_id, p_resident_id, p_amenity_id, p_date, 'Confirmed', p_capacity, p_slot);

COMMIT;
END IF;
END//
DELIMITER ;

-- Procedure for checking if the slot chosen is valid
DELIMITER //
DROP PROCEDURE IF EXISTS UpdateTechnicianSlot//
CREATE PROCEDURE UpdateTechnicianSlot(
    IN p_tech_id INT,
    IN p_slot INT
)
BEGIN
    -- Use conditional logic to determine which column to update based on the input
    IF p_slot = 1 THEN
UPDATE `UrbanNexus`.`technician` SET slot_1 = 1 WHERE tech_id = p_tech_id;
ELSEIF p_slot = 2 THEN
UPDATE `UrbanNexus`.`technician` SET slot_2 = 1 WHERE tech_id = p_tech_id;
ELSEIF p_slot = 3 THEN
UPDATE `UrbanNexus`.`technician` SET slot_3 = 1 WHERE tech_id = p_tech_id;
ELSEIF p_slot = 4 THEN
UPDATE `UrbanNexus`.`technician` SET slot_4 = 1 WHERE tech_id = p_tech_id;
ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid slot number. Must be between 1 and 4.';
END IF;
END//
DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;