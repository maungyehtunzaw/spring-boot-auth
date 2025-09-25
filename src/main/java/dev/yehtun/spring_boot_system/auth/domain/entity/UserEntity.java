package dev.yehtun.spring_boot_system.auth.domain.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import dev.yehtun.spring_boot_system.shared.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * User entity representing system users including guest users.
 * 
 * Features:
 * - Guest user support with auto-generation
 * - Email and phone verification
 * - Account locking mechanism
 * - Password expiration tracking
 * - Multi-device session support
 * - Role-based access control
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserEntity extends AuditableEntity {

    @Column(name = "username", length = 50, unique = true)
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(name = "email", length = 100, unique = true)
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Column(name = "last_name", length = 100)
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Column(name = "phone_number", length = 20)
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    @NotNull(message = "User type is required")
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    @NotNull(message = "User status is required")
    private UserStatus userStatus;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "phone_verified_at")
    private LocalDateTime phoneVerifiedAt;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_attempts", nullable = false)
    @Builder.Default
    private Integer loginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // Relationships
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserSessionEntity> sessions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserDeviceEntity> devices = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TwoFactorAuthEntity twoFactorAuth;

    /**
     * Creates a guest user with auto-generated identifier.
     *
     * @param guestIdentifier unique identifier for the guest user
     * @return new guest user entity
     */
    public static UserEntity createGuestUser(String guestIdentifier) {
        return UserEntity.builder()
                .username(guestIdentifier)
                .userType(UserType.GUEST)
                .userStatus(UserStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .loginAttempts(0)
                .build();
    }

    /**
     * Generates a unique guest identifier.
     *
     * @return unique guest identifier
     */
    public static String generateGuestIdentifier() {
        return "guest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * Checks if this user is a guest user.
     *
     * @return true if user is a guest
     */
    public boolean isGuest() {
        return UserType.GUEST.equals(this.userType);
    }

    /**
     * Checks if user can login based on their status.
     *
     * @return true if user can login
     */
    public boolean canLogin() {
        return this.userStatus != null && this.userStatus.canLogin() && !isAccountLocked();
    }

    /**
     * Checks if the user's password has expired.
     *
     * @param maxPasswordAgeDays maximum password age in days
     * @return true if password is expired
     */
    public boolean isPasswordExpired(int maxPasswordAgeDays) {
        if (passwordChangedAt == null) {
            return false; // If never changed, consider it not expired
        }
        return passwordChangedAt.isBefore(LocalDateTime.now().minusDays(maxPasswordAgeDays));
    }

    /**
     * Checks if the account is currently locked.
     *
     * @return true if account is locked
     */
    public boolean isAccountLocked() {
        if (UserStatus.LOCKED.equals(this.userStatus)) {
            return lockedUntil == null || lockedUntil.isAfter(LocalDateTime.now());
        }
        return false;
    }

    /**
     * Increments the login attempt counter.
     */
    public void incrementLoginAttempts() {
        this.loginAttempts = (this.loginAttempts == null ? 0 : this.loginAttempts) + 1;
    }

    /**
     * Resets the login attempt counter to zero.
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }

    /**
     * Updates the last login timestamp to current time.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Locks the account until the specified time.
     *
     * @param lockUntil time until which the account should be locked
     */
    public void lockAccount(LocalDateTime lockUntil) {
        this.userStatus = UserStatus.LOCKED;
        this.lockedUntil = lockUntil;
    }

    /**
     * Unlocks the account.
     */
    public void unlockAccount() {
        if (UserStatus.LOCKED.equals(this.userStatus)) {
            this.userStatus = UserStatus.ACTIVE;
            this.lockedUntil = null;
            this.resetLoginAttempts();
        }
    }

    /**
     * Promotes a guest user to a regular user.
     *
     * @param email user's email address
     * @param username new username
     * @param passwordHash hashed password
     */
    public void promoteToRegularUser(String email, String username, String passwordHash) {
        if (!isGuest()) {
            throw new IllegalStateException("Only guest users can be promoted");
        }
        
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.userType = UserType.USER;
        this.userStatus = UserStatus.PENDING_VERIFICATION;
        this.emailVerified = false;
        this.passwordChangedAt = LocalDateTime.now();
    }

    /**
     * Verifies the user's email address.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
        
        // If user was pending verification, activate them
        if (UserStatus.PENDING_VERIFICATION.equals(this.userStatus)) {
            this.userStatus = UserStatus.ACTIVE;
        }
    }

    /**
     * Verifies the user's phone number.
     */
    public void verifyPhone() {
        this.phoneVerified = true;
        this.phoneVerifiedAt = LocalDateTime.now();
    }

    /**
     * Gets the user's full name.
     *
     * @return full name or username if names are not available
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }

    /**
     * Gets the display name for the user.
     *
     * @return display name (full name for regular users, username for guests)
     */
    public String getDisplayName() {
        if (isGuest()) {
            return "Guest User";
        }
        return getFullName();
    }

    /**
     * Checks if the user has a specific role.
     *
     * @param roleName name of the role to check
     * @return true if user has the role
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }

    /**
     * Adds a role to the user.
     *
     * @param role role to add
     */
    public void addRole(RoleEntity role) {
        this.roles.add(role);
    }

    /**
     * Removes a role from the user.
     *
     * @param role role to remove
     */
    public void removeRole(RoleEntity role) {
        this.roles.remove(role);
    }

    /**
     * Enables two-factor authentication for the user.
     */
    public void enableTwoFactor() {
        this.twoFactorEnabled = true;
    }

    /**
     * Disables two-factor authentication for the user.
     */
    public void disableTwoFactor() {
        this.twoFactorEnabled = false;
    }

    /**
     * Updates the user's password and sets the password changed timestamp.
     *
     * @param newPasswordHash new hashed password
     */
    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = LocalDateTime.now();
        this.resetLoginAttempts(); // Reset attempts on password change
    }

    /**
     * Deactivates the user account.
     */
    public void deactivate() {
        this.userStatus = UserStatus.INACTIVE;
    }

    /**
     * Activates the user account.
     */
    public void activate() {
        if (!UserStatus.LOCKED.equals(this.userStatus)) {
            this.userStatus = UserStatus.ACTIVE;
            this.resetLoginAttempts();
        }
    }

    /**
     * Suspends the user account.
     */
    public void suspend() {
        this.userStatus = UserStatus.SUSPENDED;
    }
}