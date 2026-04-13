USE `UrbanNexus`;

-- 1. Residents: The 2026 Grid
-- (Explicitly setting resident_id to 1, 2, 3 etc. to prevent Admin login FK errors)
INSERT INTO `resident` (resident_id, name, house_block, house_floor, house_unit, ownership_status, contact, no_of_members)
VALUES
    (1, 'Lewis Hamilton', 'A', 4, '44', 'Owner', '9444444444', 2),
    (2, 'Max Verstappen', 'B', 1, '01', 'Owner', '9010101010', 1),
    (3, 'Charles Leclerc', 'A', 16, '1601', 'Tenant', '9161616161', 2),
    (4, 'Fernando Alonso', 'C', 14, '1401', 'Owner', '9141414141', 1),
    (5, 'Lando Norris', 'B', 4, '404', 'Tenant', '9040404040', 3);

-- 2. Technicians: The Pit Wall (Meme Edition)
INSERT INTO `technician` (tech_id, name, contact, skill)
VALUES
    (101, 'Toto Wolff', '8888888888', 'Electrician'),
    (102, 'Charlie Whiting', '7777777777', 'Plumber'),
    (103, 'Gunther Steiner', '6666666666', 'Maintenance'),
    (104, 'Christian Horner', '5555555555', 'Carpenter');

-- 3. Amenities
INSERT INTO `amenity` (amenity_id, name, capacity)
VALUES
    (1, 'Paddock Club Lounge', 20),
    (2, 'Monaco Rooftop Pool', 15),
    (3, 'Parc Fermé Gym', 10);

-- 4. Login Accounts (Password: pwd123#)
-- Safely maps 'sir_lewis' to resident_id 1
INSERT INTO `admin` (username, password_hash, role, resident_id)
VALUES
    ('sir_lewis', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 1),
    ('toto_admin', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'SuperAdmin', NULL);

-- 5. Sample Payments
INSERT INTO `payment` (trans_no, status, type, cost)
VALUES
    ('TXN-TECH-44', 'Pending', 'Technician', 440.00),
    ('TXN-AMEN-01', 'Paid', 'Amenity', 1000.00),
    ('TXN-TECH-33', 'Overdue', 'Technician', 330.00),
    ('TXN-AMEN-16', 'Pending', 'Amenity', 160.00);

-- 6. Pricing
-- (FIXED: Names now match the amenities exactly, and Carpenter is added)
INSERT INTO `UrbanNexus`.`pricing` (item_name, category, base_price)
VALUES
    ('Plumber', 'Technician', 500.00),
    ('Electrician', 'Technician', 600.00),
    ('Maintenance', 'Technician', 400.00),
    ('Carpenter', 'Technician', 550.00),
    ('Paddock Club Lounge', 'Amenity', 1000.00),
    ('Monaco Rooftop Pool', 'Amenity', 1500.00),
    ('Parc Fermé Gym', 'Amenity', 300.00);