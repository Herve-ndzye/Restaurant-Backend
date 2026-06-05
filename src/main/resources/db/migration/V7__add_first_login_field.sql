-- Add first_login field to user table for admin invitation system
ALTER TABLE restaurant.user
ADD COLUMN first_login BOOLEAN NOT NULL DEFAULT FALSE;

-- Add comment for documentation
COMMENT ON COLUMN restaurant.user.first_login IS 'Flag indicating user must change password on first login (used for admin invitations)';
