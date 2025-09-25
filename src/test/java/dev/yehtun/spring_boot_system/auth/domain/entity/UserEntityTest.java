package dev.yehtun.spring_boot_system.auth.domain.entity;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Test class for UserEntity.
 * 
 * Tests:
 * - Entity creation and validation
 * - Guest user functionality
 * - Email and username validation
 * - Password hashing
 * - User status management
 * - Relationships with roles and sessions
 */
@ActiveProfiles("test")
class UserEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void should_create_valid_user_entity() {
        // Given
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        // When
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getUserType()).isEqualTo(UserType.USER);
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerified()).isTrue();
    }

    @Test
    void should_create_guest_user() {
        // Given
        String guestIdentifier = "guest_12345";
        
        // When
        UserEntity guestUser = UserEntity.createGuestUser(guestIdentifier);

        // Then
        assertThat(guestUser).isNotNull();
        assertThat(guestUser.getUserType()).isEqualTo(UserType.GUEST);
        assertThat(guestUser.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(guestUser.getUsername()).isEqualTo(guestIdentifier);
        assertThat(guestUser.getEmail()).isNull();
        assertThat(guestUser.getPasswordHash()).isNull();
        assertThat(guestUser.getEmailVerified()).isFalse();
        assertThat(guestUser.isGuest()).isTrue();
    }

    @Test
    void should_generate_unique_guest_identifier() {
        // When
        String identifier1 = UserEntity.generateGuestIdentifier();
        String identifier2 = UserEntity.generateGuestIdentifier();

        // Then
        assertThat(identifier1).isNotNull();
        assertThat(identifier2).isNotNull();
        assertThat(identifier1).isNotEqualTo(identifier2);
        assertThat(identifier1).startsWith("guest_");
        assertThat(identifier2).startsWith("guest_");
    }

    @Test
    void should_validate_email_format() {
        // Given
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .email("invalid-email")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<UserEntity> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void should_validate_username_constraints() {
        // Given
        UserEntity user = UserEntity.builder()
                .username("ab") // Too short
                .email("test@example.com")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        // When
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        boolean hasUsernameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        assertThat(hasUsernameViolation).isTrue();
    }

    @Test
    void should_check_if_user_is_guest() {
        // Given
        UserEntity guestUser = UserEntity.createGuestUser("guest_123");
        UserEntity regularUser = UserEntity.builder()
                .username("regularuser")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        // When & Then
        assertThat(guestUser.isGuest()).isTrue();
        assertThat(regularUser.isGuest()).isFalse();
    }

    @Test
    void should_check_if_user_can_login() {
        // Given
        UserEntity activeUser = UserEntity.builder()
                .username("activeuser")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
        
        UserEntity suspendedUser = UserEntity.builder()
                .username("suspendeduser")
                .userType(UserType.USER)
                .userStatus(UserStatus.SUSPENDED)
                .build();

        // When & Then
        assertThat(activeUser.canLogin()).isTrue();
        assertThat(suspendedUser.canLogin()).isFalse();
    }

    @Test
    void should_check_password_expiration() {
        // Given
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .passwordChangedAt(LocalDateTime.now().minusDays(91)) // 91 days ago
                .build();

        // When & Then
        assertThat(user.isPasswordExpired(90)).isTrue(); // Password expires after 90 days
        assertThat(user.isPasswordExpired(100)).isFalse(); // Not expired if limit is 100 days
    }

    @Test
    void should_check_account_lock_status() {
        // Given
        UserEntity lockedUser = UserEntity.builder()
                .username("lockeduser")
                .userType(UserType.USER)
                .userStatus(UserStatus.LOCKED)
                .lockedUntil(LocalDateTime.now().plusHours(1)) // Locked for 1 more hour
                .build();

        UserEntity expiredLockUser = UserEntity.builder()
                .username("expiredlockuser")
                .userType(UserType.USER)
                .userStatus(UserStatus.LOCKED)
                .lockedUntil(LocalDateTime.now().minusHours(1)) // Lock expired 1 hour ago
                .build();

        // When & Then
        assertThat(lockedUser.isAccountLocked()).isTrue();
        assertThat(expiredLockUser.isAccountLocked()).isFalse();
    }

    @Test
    void should_lock_account_after_failed_attempts() {
        // Given
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .loginAttempts(0)
                .build();

        // When
        user.incrementLoginAttempts();
        user.incrementLoginAttempts();
        user.incrementLoginAttempts();

        // Then
        assertThat(user.getLoginAttempts()).isEqualTo(3);
        
        // When - lock account after max attempts
        user.lockAccount(LocalDateTime.now().plusMinutes(15));

        // Then
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void should_reset_login_attempts_on_successful_login() {
        // Given
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .loginAttempts(3)
                .build();

        // When
        user.resetLoginAttempts();
        user.updateLastLogin();

        // Then
        assertThat(user.getLoginAttempts()).isEqualTo(0);
        assertThat(user.getLastLoginAt()).isNotNull();
        assertThat(user.getLastLoginAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void should_promote_guest_to_regular_user() {
        // Given
        UserEntity guestUser = UserEntity.createGuestUser("guest_123");

        // When
        guestUser.promoteToRegularUser("newuser@example.com", "newuser", "password123");

        // Then
        assertThat(guestUser.getUserType()).isEqualTo(UserType.USER);
        assertThat(guestUser.getUserStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(guestUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(guestUser.getUsername()).isEqualTo("newuser");
        assertThat(guestUser.getPasswordHash()).isEqualTo("password123");
        assertThat(guestUser.getEmailVerified()).isFalse();
    }

    @Test
    void should_verify_email() {
        // Given
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .userType(UserType.USER)
                .userStatus(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();

        // When
        user.verifyEmail();

        // Then
        assertThat(user.getEmailVerified()).isTrue();
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void should_calculate_full_name() {
        // Given
        UserEntity user = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        // When & Then
        assertThat(user.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void should_handle_null_names_in_full_name() {
        // Given
        UserEntity userWithFirstName = UserEntity.builder()
                .firstName("John")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        UserEntity userWithLastName = UserEntity.builder()
                .lastName("Doe")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        UserEntity userWithNoNames = UserEntity.builder()
                .username("testuser")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        // When & Then
        assertThat(userWithFirstName.getFullName()).isEqualTo("John");
        assertThat(userWithLastName.getFullName()).isEqualTo("Doe");
        assertThat(userWithNoNames.getFullName()).isEqualTo("testuser");
    }
}