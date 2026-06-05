-- Add profile fields to user table for role-specific registration
-- These fields eliminate the need for separate profile registration step

-- Add name field (for all users)
ALTER TABLE user ADD COLUMN name VARCHAR(100) NULL COMMENT 'User full name';

-- Add phone field (for customers and delivery drivers)
ALTER TABLE user ADD COLUMN phone VARCHAR(20) NULL COMMENT 'Contact phone number';

-- Add delivery driver specific fields
ALTER TABLE user ADD COLUMN vehicle_type VARCHAR(50) NULL COMMENT 'Vehicle type for delivery drivers';
ALTER TABLE user ADD COLUMN vehicle_number VARCHAR(20) NULL COMMENT 'Vehicle registration number for delivery drivers';

-- Add indexes for better query performance
CREATE INDEX idx_user_phone ON user(phone);
CREATE INDEX idx_user_vehicle_number ON user(vehicle_number);
