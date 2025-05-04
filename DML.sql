-- Insert sample data into users table
INSERT INTO users (user_id, name, email, password_hash, phone_number, role, is_verified, agent_license_number, receives_market_updates) VALUES
-- Admins
('admin1', 'Robert Johnson', 'rjohnson@rels.com', 'adminpass1', '555-100-1001', 'ADMIN', TRUE, NULL, NULL),
('admin2', 'Susan Williams', 'swilliams@rels.com', 'adminpass2', '555-100-1002', 'ADMIN', TRUE, NULL, NULL),

-- Landlords
('land1', 'Michael Brown', 'mbrown@example.com', 'landpass1', '555-200-2001', 'LANDLORD', TRUE, 'LL-1001', NULL),
('land2', 'Jennifer Davis', 'jdavis@example.com', 'landpass2', '555-200-2002', 'LANDLORD', TRUE, 'LL-1002', NULL),
('land3', 'David Miller', 'dmiller@example.com', 'landpass3', '555-200-2003', 'LANDLORD', FALSE, 'LL-1003', NULL),
('land4', 'Lisa Wilson', 'lwilson@example.com', 'landpass4', '555-200-2004', 'LANDLORD', TRUE, 'LL-1004', NULL),
('land5', 'James Moore', 'jmoore@example.com', 'landpass5', '555-200-2005', 'LANDLORD', TRUE, 'LL-1005', NULL),

-- Clients
('client1', 'Emily Taylor', 'etaylor@example.com', 'clientpass1', '555-300-3001', 'CLIENT', TRUE, NULL, TRUE),
('client2', 'Christopher Anderson', 'canderson@example.com', 'clientpass2', '555-300-3002', 'CLIENT', FALSE, NULL, FALSE),
('client3', 'Jessica Thomas', 'jthomas@example.com', 'clientpass3', '555-300-3003', 'CLIENT', TRUE, NULL, TRUE),
('client4', 'Daniel Jackson', 'djackson@example.com', 'clientpass4', '555-300-3004', 'CLIENT', TRUE, NULL, FALSE),
('client5', 'Sarah White', 'swhite@example.com', 'clientpass5', '555-300-3005', 'CLIENT', FALSE, NULL, TRUE);

-- Insert sample data into properties table
INSERT INTO properties (property_id, landlord_id, address, city, postal_code, property_type, description, price, square_footage, bedrooms, bathrooms, is_active, date_listed) VALUES
-- Properties for Michael Brown
('prop1', 'land1', '123 Oak Street', 'Boston', '02108', 'House', 'Beautiful 3-bedroom colonial home with large backyard', 450000.00, 2200.00, 3, 2, TRUE, '2023-01-15 09:00:00'),
('prop2', 'land1', '456 Maple Avenue', 'Boston', '02116', 'Condo', 'Modern 2-bedroom condo with city views', 350000.00, 1200.00, 2, 2, TRUE, '2023-02-20 10:00:00'),

-- Properties for Jennifer Davis
('prop3', 'land2', '789 Pine Road', 'Cambridge', '02139', 'Apartment', 'Spacious 1-bedroom near MIT', 220000.00, 850.00, 1, 1, TRUE, '2023-03-10 11:00:00'),
('prop4', 'land2', '101 Elm Street', 'Cambridge', '02138', 'House', 'Historic 4-bedroom near Harvard', 650000.00, 2800.00, 4, 3, TRUE, '2023-01-05 08:00:00'),

-- Properties for David Miller
('prop5', 'land3', '202 Cedar Lane', 'Somerville', '02143', 'Townhouse', 'Cozy 2-bedroom townhouse', 280000.00, 1400.00, 2, 1, FALSE, '2023-04-15 12:00:00'),

-- Properties for Lisa Wilson
('prop6', 'land4', '303 Birch Court', 'Brookline', '02445', 'Apartment', 'Luxury studio with amenities', 180000.00, 600.00, 0, 1, TRUE, '2023-05-20 13:00:00'),
('prop7', 'land4', '404 Spruce Drive', 'Brookline', '02446', 'House', 'Renovated 3-bedroom with garage', 520000.00, 2100.00, 3, 2, TRUE, '2023-06-01 14:00:00'),

-- Properties for James Moore
('prop8', 'land5', '505 Willow Way', 'Newton', '02458', 'Condo', 'Top-floor 2-bed with balcony', 380000.00, 1300.00, 2, 2, TRUE, '2023-02-15 15:00:00'),
('prop9', 'land5', '606 Redwood Circle', 'Newton', '02459', 'House', '5-bedroom family home', 750000.00, 3200.00, 5, 3, TRUE, '2023-01-10 16:00:00'),

-- Inactive property
('prop10', 'land3', '707 Magnolia Blvd', 'Somerville', '02144', 'Apartment', '1-bedroom needing renovation', 150000.00, 700.00, 1, 1, FALSE, '2023-03-01 17:00:00');

-- Insert sample data into bids table
INSERT INTO bids (bid_id, property_id, client_id, amount, status, bid_timestamp) VALUES
-- Bids on prop1 (Michael Brown's property)
('bid1', 'prop1', 'client1', 440000.00, 'PENDING', '2023-01-20 10:30:00'),
('bid2', 'prop1', 'client3', 445000.00, 'PENDING', '2023-01-21 11:15:00'),

-- Bids on prop3 (Jennifer Davis's property)
('bid3', 'prop3', 'client2', 210000.00, 'REJECTED', '2023-03-12 09:45:00'),
('bid4', 'prop3', 'client4', 215000.00, 'ACCEPTED', '2023-03-13 14:20:00'),

-- Bids on prop6 (Lisa Wilson's property)
('bid5', 'prop6', 'client5', 175000.00, 'PENDING', '2023-05-22 16:10:00'),

-- Bids on prop8 (James Moore's property)
('bid6', 'prop8', 'client1', 370000.00, 'WITHDRAWN', '2023-02-18 13:25:00'),
('bid7', 'prop8', 'client3', 375000.00, 'PENDING', '2023-02-19 10:45:00'),

-- Bids on prop4 (Jennifer Davis's property)
('bid8', 'prop4', 'client5', 630000.00, 'PENDING', '2023-01-08 11:30:00'),

-- Bids on prop7 (Lisa Wilson's property)
('bid9', 'prop7', 'client2', 500000.00, 'REJECTED', '2023-06-03 15:40:00'),
('bid10', 'prop7', 'client4', 510000.00, 'PENDING', '2023-06-04 09:15:00');

-- Note: In a real system, passwords should be properly hashed (not stored in plain text)