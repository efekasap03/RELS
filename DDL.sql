CREATE DATABASE relsdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE relsdb;


-- ---
-- Users Table (Handles User, Landlord, Client via Single Table Inheritance)
-- ---
CREATE TABLE users (
    user_id VARCHAR(36) NOT NULL,                      -- Using VARCHAR for UUID flexibility, could be INT UNSIGNED AUTO_INCREMENT
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,               -- Store hashed passwords, never plain text
    phone_number VARCHAR(20) NULL,
    role ENUM('CLIENT', 'LANDLORD', 'ADMIN') NOT NULL, -- Discriminator column for STI
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,        -- General account verification
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Landlord specific fields (NULL for non-landlords)
    agent_license_number VARCHAR(100) NULL,

    -- Client specific fields (NULL for non-clients)
    receives_market_updates BOOLEAN NULL DEFAULT FALSE, -- Making NULL default, as FALSE might imply opted-out specifically

    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---
-- Properties Table
-- ---
CREATE TABLE properties (
    property_id VARCHAR(36) NOT NULL,
    landlord_id VARCHAR(36) NOT NULL,                  -- Foreign Key to users table
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    property_type VARCHAR(50) NULL,                    -- e.g., 'Apartment', 'House', 'Condo'
    description TEXT NULL,
    price DECIMAL(12, 2) NOT NULL,                     -- Adjust precision/scale as needed for property values
    square_footage DECIMAL(10, 2) NULL,
    bedrooms TINYINT UNSIGNED NULL DEFAULT 0,
    bathrooms TINYINT UNSIGNED NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,           -- Is the property currently listed/active?
    date_listed TIMESTAMP NULL,                        -- When the property was actively listed
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (property_id),
    INDEX idx_properties_landlord_id (landlord_id),
    INDEX idx_properties_location (city, postal_code),
    INDEX idx_properties_price (price),
    INDEX idx_properties_type (property_type),
    INDEX idx_properties_active_listed (is_active, date_listed),

    CONSTRAINT fk_properties_landlord
        FOREIGN KEY (landlord_id)
        REFERENCES users (user_id)
        ON DELETE RESTRICT   -- Prevent deleting a user if they still have properties listed
        ON UPDATE CASCADE    -- If user_id changes (unlikely with UUIDs), update here
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---
-- Bids Table
-- ---
CREATE TABLE bids (
    bid_id VARCHAR(36) NOT NULL,
    property_id VARCHAR(36) NOT NULL,                  -- Foreign Key to properties table
    client_id VARCHAR(36) NOT NULL,                    -- Foreign Key to users table
    amount DECIMAL(12, 2) NOT NULL,                    -- Bid amount
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'PENDING',
    bid_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- When the bid was placed
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (bid_id),
    INDEX idx_bids_property_id (property_id),
    INDEX idx_bids_client_id (client_id),
    INDEX idx_bids_status (status),

    CONSTRAINT fk_bids_property
        FOREIGN KEY (property_id)
        REFERENCES properties (property_id)
        ON DELETE CASCADE,    -- If property is deleted, bids on it are likely irrelevant/removed
    CONSTRAINT fk_bids_client
        FOREIGN KEY (client_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE     -- If client account is deleted, remove their bids
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; -- InnoDB is recommended for production systems

-- ---
-- Notes:
-- 1. UUIDs: Using VARCHAR(36) for IDs assumes UUIDs. If using auto-increment integers, change to `INT UNSIGNED AUTO_INCREMENT`.
-- 2. Timestamps: Using TIMESTAMP with defaults for `created_at` and `updated_at` is standard practice.
-- 3. Character Set/Collation: Using `utf8mb4` is recommended for full Unicode support.
-- 4. Indexes: Added basic indexes on foreign keys and commonly filtered fields. More specific indexes might be needed based on actual query patterns.
-- 5. Foreign Key Actions: `ON DELETE`/`ON UPDATE` actions (RESTRICT, CASCADE) are chosen based on plausible business rules, adjust as needed.
-- 6. NULL constraints: Adjusted based on likely requirements (e.g., phone number might be optional).
-- 7. Filter Class: The `Filter` class represents search criteria and does not map directly to a database table.
-- ---

-- User Permissions
-- Regular User (Client)
CREATE USER 'regular'@'%' IDENTIFIED BY 'regularpass';
GRANT SELECT ON relsdb.properties TO 'regular'@'%';
GRANT SELECT, INSERT ON relsdb.bids TO 'regular'@'%';
GRANT SELECT ON relsdb.users TO 'regular'@'%';

-- Landlord
CREATE USER 'landlord'@'%' IDENTIFIED BY 'landlordpass';
GRANT SELECT, INSERT, UPDATE ON relsdb.properties TO 'landlord'@'%';
GRANT SELECT ON relsdb.bids TO 'landlord'@'%';
GRANT SELECT ON relsdb.users TO 'landlord'@'%';

-- Admin
CREATE USER 'admin'@'%' IDENTIFIED BY 'adminpass';
GRANT ALL PRIVILEGES ON relsdb.* TO 'admin'@'%';