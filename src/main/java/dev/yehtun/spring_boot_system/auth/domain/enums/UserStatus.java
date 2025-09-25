package dev.yehtun.spring_boot_system.auth.domain.enums;

import java.util.Arrays;
import java.util.Set;

/**
 * Enum representing the status of a user account.
 * 
 * Status definitions:
 * - ACTIVE: User account is active and can login
 * - INACTIVE: User account is inactive (disabled by admin or user)
 * - SUSPENDED: User account is temporarily suspended (admin action)
 * - PENDING_VERIFICATION: User account is pending email verification
 * - LOCKED: User account is locked due to security reasons (too many failed attempts)
 */
public enum UserStatus {
    
    ACTIVE("Active User"),
    INACTIVE("Inactive User"),
    SUSPENDED("Suspended User"),
    PENDING_VERIFICATION("Pending Email Verification"),
    LOCKED("Account Locked");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    /**
     * @return true if the user status is active
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * @return true if the user can login with this status
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * @return true if the status requires email verification
     */
    public boolean requiresVerification() {
        return this == PENDING_VERIFICATION;
    }

    /**
     * @return human-readable description of the status
     */
    public String getDescription() {
        return description;
    }

    /**
     * Validates if transition from current status to target status is allowed.
     * 
     * Business rules:
     * - PENDING_VERIFICATION can only go to ACTIVE or INACTIVE
     * - ACTIVE can go to any other status except PENDING_VERIFICATION
     * - LOCKED can go to any status
     * - SUSPENDED can go to ACTIVE or INACTIVE
     * - INACTIVE can go to ACTIVE or SUSPENDED
     * 
     * @param targetStatus the target status to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(UserStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        switch (this) {
            case PENDING_VERIFICATION:
                return Set.of(ACTIVE, INACTIVE).contains(targetStatus);
            case ACTIVE:
                return Set.of(INACTIVE, SUSPENDED, LOCKED).contains(targetStatus);
            case LOCKED:
                return Set.of(ACTIVE, INACTIVE, SUSPENDED).contains(targetStatus);
            case SUSPENDED:
                return Set.of(ACTIVE, INACTIVE).contains(targetStatus);
            case INACTIVE:
                return Set.of(ACTIVE, SUSPENDED).contains(targetStatus);
            default:
                return false;
        }
    }

    /**
     * Creates UserStatus from string value.
     * 
     * @param value the string value
     * @return UserStatus enum
     * @throws IllegalArgumentException if value is invalid
     */
    public static UserStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("UserStatus cannot be null or empty");
        }
        
        return Arrays.stream(values())
                .filter(status -> status.name().equals(value.trim().toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid UserStatus: " + value));
    }
}