-- UrbanNexus Seed Data (Universal)

-- RESIDENTS (25+)
INSERT INTO resident (resident_id, name, house_block, house_floor, house_unit, ownership_status, contact, no_of_members)
VALUES (1, 'John Doe', 'A', '4', '44', 'Owner', '9444444444', 2),
       (2, 'Jane Smith', 'A', '1', '101', 'Tenant', '9123456780', 3),
       (3, 'Michael Johnson', 'A', '2', '202', 'Owner', '9123456781', 4),
       (4, 'Emily Davis', 'B', '1', '105', 'Owner', '9123456782', 1),
       (5, 'Chris Brown', 'B', '3', '303', 'Tenant', '9123456783', 2),
       (6, 'Sarah Wilson', 'B', '5', '505', 'Owner', '9123456784', 5),
       (7, 'David Miller', 'C', '1', '102', 'Tenant', '9123456785', 2),
       (8, 'Jessica Taylor', 'C', '2', '204', 'Owner', '9123456786', 3),
       (9, 'Matthew Thomas', 'C', '4', '401', 'Owner', '9123456787', 4),
       (10, 'Ashley White', 'A', '3', '302', 'Tenant', '9123456788', 2),
       (11, 'Robert Moore', 'A', '5', '501', 'Owner', '9123456789', 3),
       (12, 'Jennifer Martin', 'B', '2', '201', 'Owner', '9123456790', 4),
       (13, 'Daniel Lee', 'B', '4', '404', 'Tenant', '9123456791', 2),
       (14, 'Amanda Harris', 'C', '3', '305', 'Owner', '9123456792', 1),
       (15, 'Andrew Clark', 'C', '5', '502', 'Owner', '9123456793', 5),
       (16, 'Stephanie Lewis', 'A', '1', '103', 'Tenant', '9123456794', 2),
       (17, 'Joshua Walker', 'A', '2', '205', 'Owner', '9123456795', 3),
       (18, 'Melissa Young', 'B', '1', '102', 'Tenant', '9123456796', 4),
       (19, 'Kevin Nelson', 'B', '3', '301', 'Owner', '9123456797', 2),
       (20, 'Nicole Hall', 'C', '1', '104', 'Owner', '9123456798', 3),
       (21, 'Ryan Allen', 'C', '2', '201', 'Tenant', '9123456799', 4),
       (22, 'Amy King', 'A', '4', '402', 'Owner', '9123456800', 2),
       (23, 'Brandon Wright', 'A', '5', '505', 'Tenant', '9123456801', 3),
       (24, 'Rachel Scott', 'B', '2', '203', 'Owner', '9123456802', 4),
       (25, 'Justin Green', 'B', '4', '402', 'Tenant', '9123456803', 1),
       (26, 'Bhumika Agarwal', 'C', '5', '505', 'Owner', '9123456804', 2);

-- AMENITIES
INSERT INTO amenity (amenity_id, name, capacity)
VALUES (1, 'Paddock Club Lounge', 20),
       (2, 'Monaco Rooftop Pool', 15),
       (3, 'Parc Fermé Gym', 10),
       (4, 'Spa & Wellness Center', 8),
       (5, 'Community Theater', 50);

-- TECHNICIANS (15+)
INSERT INTO technician (tech_id, name, contact, skill, available)
VALUES (1, 'Tom Kristensen', '9000000001', 'Plumber', 1),
       (2, 'Jacky Ickx', '9000000002', 'Electrician', 1),
       (3, 'Derek Bell', '9000000003', 'Carpenter', 1),
       (4, 'Frank Biela', '9000000004', 'Maintenance', 1),
       (5, 'Emanuele Pirro', '9000000005', 'Plumber', 1),
       (6, 'Sebastien Buemi', '9000000006', 'Electrician', 1),
       (7, 'Brendon Hartley', '9000000007', 'HVAC', 1),
       (8, 'Andre Lotterer', '9000000008', 'Painter', 1),
       (9, 'Benoit Treluyer', '9000000009', 'Mason', 1),
       (10, 'Marcel Fassler', '9000000010', 'Carpenter', 1),
       (11, 'Allan McNish', '9000000011', 'HVAC', 1),
       (12, 'Dindo Capello', '9000000012', 'Pest Control', 1),
       (13, 'Loic Duval', '9000000013', 'Security Expert', 1),
       (14, 'Romain Dumas', '9000000014', 'Landscaper', 1),
       (15, 'Marc Gene', '9000000015', 'Plumber', 1),
       (16, 'Alexander Wurz', '9000000016', 'Electrician', 1);

-- PRICING
INSERT INTO pricing (item_name, category, base_price)
VALUES ('Plumber', 'Technician', 500.00),
       ('Electrician', 'Technician', 600.00),
       ('Maintenance', 'Technician', 400.00),
       ('Carpenter', 'Technician', 550.00),
       ('HVAC', 'Technician', 800.00),
       ('Painter', 'Technician', 450.00),
       ('Mason', 'Technician', 700.00),
       ('Pest Control', 'Technician', 1200.00),
       ('Landscaper', 'Technician', 1500.00),
       ('Paddock Club Lounge', 'Amenity', 1000.00),
       ('Monaco Rooftop Pool', 'Amenity', 1500.00),
       ('Parc Fermé Gym', 'Amenity', 300.00),
       ('Spa & Wellness Center', 'Amenity', 2000.00),
       ('Community Theater', 'Amenity', 5000.00);

-- ADMIN ACCOUNTS (Credentials for all roles)
INSERT INTO admin (username, password_hash, role, resident_id, tech_id)
VALUES
-- SuperAdmin
('admin', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'SuperAdmin', NULL, NULL),

-- Residents (1-26)
('john_doe', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 1, NULL),
('jane_s', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 2, NULL),
('michael_j', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 3, NULL),
('emily_d', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 4, NULL),
('chris_b', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 5, NULL),
('sarah_w', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 6, NULL),
('david_m', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 7, NULL),
('jessica_t', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 8, NULL),
('matthew_t', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 9, NULL),
('ashley_w', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 10, NULL),
('robert_m', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 11, NULL),
('jennifer_m', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 12, NULL),
('daniel_l', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 13, NULL),
('amanda_h', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 14, NULL),
('andrew_c', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 15, NULL),
('steph_l', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 16, NULL),
('joshua_w', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 17, NULL),
('melissa_y', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 18, NULL),
('kevin_n', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 19, NULL),
('nicole_h', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 20, NULL),
('ryan_a', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 21, NULL),
('amy_k', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 22, NULL),
('brandon_w', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 23, NULL),
('rachel_s', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 24, NULL),
('justin_g', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 25, NULL),
('bhumika', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 26, NULL),

-- Technicians (1-16)
('tech_tom', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 1),
('tech_jacky', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 2),
('tech_derek', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 3),
('tech_frank', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 4),
('tech_eman', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 5),
('tech_seb', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 6),
('tech_brendon', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 7),
('tech_andre', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 8),
('tech_ben', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 9),
('tech_marcel', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 10),
('tech_allan', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 11),
('tech_dindo', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 12),
('tech_loic', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 13),
('tech_romain', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 14),
('tech_marc', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 15),
('tech_alex', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Technician', NULL, 16);

-- PAYMENTS (Realistic Ledger)
INSERT INTO payment (trans_no, status, type, cost)
VALUES ('TXN-101', 'Pending', 'Technician', 500.00),
       ('TXN-102', 'Paid', 'Amenity', 1500.00),
       ('TXN-103', 'Overdue', 'Technician', 600.00),
       ('TXN-104', 'Pending', 'Amenity', 300.00),
       ('TXN-105', 'Paid', 'Technician', 400.00),
       ('TXN-106', 'Pending', 'Technician', 550.00),
       ('TXN-107', 'Overdue', 'Amenity', 2000.00),
       ('TXN-108', 'Paid', 'Technician', 800.00),
       ('TXN-109', 'Pending', 'Amenity', 1000.00),
       ('TXN-110', 'Paid', 'Amenity', 5000.00);

-- ASSIGNMENTS / BOOKINGS (Simulated Tickets)
INSERT INTO technician_management (resident_id, tech_id, trans_no, slot, assign_date, status)
VALUES (2, 1, 'TXN-101', 1, '2026-04-21', 'Assigned'),
       (3, 2, 'TXN-103', 2, '2026-04-20', 'Assigned'),
       (4, 3, 'TXN-105', 3, '2026-04-19', 'Completed'),
       (5, 4, 'TXN-106', 1, '2026-04-22', 'Assigned');

INSERT INTO amenity_mgmt (resident_id, amenity_id, trans_no, date, slot, capacity_booked, status)
VALUES (2, 2, 'TXN-102', '2026-04-21', 1, 2, 'Confirmed'),
       (3, 3, 'TXN-104', '2026-04-22', 2, 1, 'Confirmed'),
       (4, 1, 'TXN-109', '2026-04-23', 3, 5, 'Confirmed');
