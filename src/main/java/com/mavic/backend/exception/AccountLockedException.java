package com.mavic.backend.exception;

import lombok.Getter;

/**
 * Custom exception for account lockout scenarios.
 * Provides information about remaining login attempts and lockout status.
 */
@Getter
public class AccountLockedException extends RuntimeException {
    private final int attemptsRemaining;
    private final boolean isLocked;

    /**
     * Creates a new AccountLockedException
     * 
     * @param message The error message to display to the user
     * @param attemptsRemaining Number of login attempts remaining (0 if locked)
     * @param isLocked Whether the account is currently locked
     */
    public AccountLockedException(String message, int attemptsRemaining, boolean isLocked) {
        super(message);
        this.attemptsRemaining = attemptsRemaining;
        this.isLocked = isLocked;
    }
}
