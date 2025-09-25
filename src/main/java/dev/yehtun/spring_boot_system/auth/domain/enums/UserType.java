package dev.yehtun.spring_boot_system.auth.domain.enums;

/**
 * User type enumeration defining different types of users in the system.
 * 
 * Types:
 * - GUEST: Temporary users created automatically for app access
 * - USER: Regular registered users
 * - ADMIN: Administrative users with elevated privileges
 * - SYSTEM: System-level users for internal operations
 */
public enum UserType {
    GUEST("Guest User"),
    USER("Regular User"),
    ADMIN("Administrator"),
    SYSTEM("System User");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this user type represents a guest user.
     * Guest users are temporary and have limited privileges.
     */
    public boolean isGuest() {
        return this == GUEST;
    }

    /**
     * Check if this user type has elevated privileges.
     * Admin and System users have elevated privileges.
     */
    public boolean isPrivileged() {
        return this == ADMIN || this == SYSTEM;
    }

    /**
     * Convert string representation to UserType enum.
     * 
     * @param value String representation of user type
     * @return UserType enum value
     * @throws IllegalArgumentException if value is invalid
     */
    public static UserType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("UserType cannot be null or empty");
        }
        
        try {
            return UserType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UserType: " + value, e);
        }
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}