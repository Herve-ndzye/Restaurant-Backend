-- Add account lockout fields to user table
ALTER TABLE user
ADD COLUMN failedLoginAttempts INT NOT NULL DEFAULT 0,
ADD COLUMN accountLockedUntil DATETIME NULL,
ADD COLUMN lastFailedLogin DATETIME NULL;

-- Add index for performance
CREATE INDEX idx_user_account_locked ON user(accountLockedUntil);
