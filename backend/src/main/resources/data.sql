-- UrbanNexus Seed Data (Universal)

INSERT INTO resident (resident_id, name, house_block, house_floor, house_unit, ownership_status, contact, no_of_members)
VALUES (1, 'John Doe', 'A', '4', '44', 'Owner', '9444444444', 2);

INSERT INTO amenity (amenity_id, name, capacity)
VALUES (1, 'Paddock Club Lounge', 20),
       (2, 'Monaco Rooftop Pool', 15),
       (3, 'Parc Fermé Gym', 10);

INSERT INTO admin (username, password_hash, role, resident_id)
VALUES ('bhumika', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'Resident', 1),
       ('admin', '$2b$10$qzIQR37Qo.GBQvgaXFSMeerZukbS7G1WTAHwRROGlB5IHjf9j3mR6', 'SuperAdmin', NULL);

INSERT INTO pricing (item_name, category, base_price)
VALUES ('Plumber', 'Technician', 500.00),
       ('Electrician', 'Technician', 600.00),
       ('Maintenance', 'Technician', 400.00),
       ('Carpenter', 'Technician', 550.00),
       ('Paddock Club Lounge', 'Amenity', 1000.00),
       ('Monaco Rooftop Pool', 'Amenity', 1500.00),
       ('Parc Fermé Gym', 'Amenity', 300.00);
