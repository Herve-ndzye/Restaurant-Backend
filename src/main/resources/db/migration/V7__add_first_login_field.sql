-- Add first_login field to user table for admin invitation system
ALTER TABLE restaurant.user
ADD COLUMN first_login BOOLEAN NOT NULL DEFAULT FALSE;

