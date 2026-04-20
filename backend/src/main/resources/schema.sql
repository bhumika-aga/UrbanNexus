-- UrbanNexus Schema Initialization (Universal DDL)
-- This script is designed for both MySQL and H2 compatibility.

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS technician_management;
DROP TABLE IF EXISTS amenity_mgmt;
DROP TABLE IF EXISTS pricing;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS resident;
DROP TABLE IF EXISTS technician;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS amenity;

-- -----------------------------------------------------
-- Table amenity
-- -----------------------------------------------------
CREATE TABLE amenity (
  amenity_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  capacity INT NULL DEFAULT NULL,
  PRIMARY KEY (amenity_id)
);

-- -----------------------------------------------------
-- Table resident
-- -----------------------------------------------------
CREATE TABLE resident (
  resident_id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  house_block VARCHAR(10) NULL DEFAULT NULL,
  house_floor VARCHAR(10) NULL DEFAULT NULL,
  house_unit VARCHAR(10) NULL DEFAULT NULL,
  ownership_status VARCHAR(50) NULL DEFAULT NULL,
  contact VARCHAR(20) NULL DEFAULT NULL,
  no_of_members INT NULL DEFAULT NULL,
  PRIMARY KEY (resident_id)
);

-- -----------------------------------------------------
-- Table payment
-- -----------------------------------------------------
CREATE TABLE payment (
  trans_no VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  type VARCHAR(50) NOT NULL,
  cost DECIMAL(10,2) NOT NULL,
  payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (trans_no)
);

-- -----------------------------------------------------
-- Table amenity_mgmt
-- -----------------------------------------------------
CREATE TABLE amenity_mgmt (
  booking_id VARCHAR(50) NOT NULL,
  resident_id INT NULL DEFAULT NULL,
  amenity_id INT NULL DEFAULT NULL,
  trans_no VARCHAR(50) NULL DEFAULT NULL,
  date DATE NULL DEFAULT NULL,
  status VARCHAR(50) NULL DEFAULT NULL,
  capacity_booked INT NULL DEFAULT NULL,
  slot INT NULL DEFAULT NULL,
  PRIMARY KEY (booking_id),
  FOREIGN KEY (resident_id) REFERENCES resident (resident_id) ON DELETE CASCADE,
  FOREIGN KEY (amenity_id) REFERENCES amenity (amenity_id) ON DELETE CASCADE,
  FOREIGN KEY (trans_no) REFERENCES payment (trans_no) ON DELETE SET NULL
);

-- -----------------------------------------------------
-- Table technician
-- -----------------------------------------------------
CREATE TABLE technician (
  tech_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  contact VARCHAR(20) NULL DEFAULT NULL,
  skill VARCHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (tech_id)
);

-- -----------------------------------------------------
-- Table technician_management
-- -----------------------------------------------------
CREATE TABLE technician_management (
  assignment_id INT NOT NULL AUTO_INCREMENT,
  resident_id INT NULL DEFAULT NULL,
  tech_id INT NULL DEFAULT NULL,
  trans_no VARCHAR(50) NULL DEFAULT NULL,
  status VARCHAR(50) NULL DEFAULT NULL,
  slot INT NOT NULL,
  assign_date DATE NOT NULL,
  PRIMARY KEY (assignment_id),
  FOREIGN KEY (resident_id) REFERENCES resident (resident_id) ON DELETE CASCADE,
  FOREIGN KEY (tech_id) REFERENCES technician (tech_id) ON DELETE CASCADE,
  FOREIGN KEY (trans_no) REFERENCES payment (trans_no) ON DELETE SET NULL
);

-- -----------------------------------------------------
-- Table admin
-- -----------------------------------------------------
CREATE TABLE admin (
  admin_id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  resident_id INT NULL,
  tech_id INT NULL,
  PRIMARY KEY (admin_id),
  FOREIGN KEY (resident_id) REFERENCES resident (resident_id) ON DELETE CASCADE,
  FOREIGN KEY (tech_id) REFERENCES technician (tech_id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table audit_log
-- -----------------------------------------------------
CREATE TABLE audit_log (
  log_id INT AUTO_INCREMENT PRIMARY KEY,
  table_affected VARCHAR(50) NOT NULL,
  record_id VARCHAR(50) NOT NULL,
  action_type VARCHAR(50) NOT NULL,
  details TEXT NOT NULL,
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Table pricing
-- -----------------------------------------------------
CREATE TABLE pricing (
  item_name VARCHAR(50) NOT NULL,
  category VARCHAR(50) NOT NULL,
  base_price DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (item_name, category)
);

-- Logic (Triggers, Procedures) moved to Java Service Layer for DB-agnosticism.
