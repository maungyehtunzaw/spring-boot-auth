package dev.yehtun.spring_boot_system.auth.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;

/**
 * Comprehensive tests for UserRepository.
 * 
 * Tests cover:
 * - Basic CRUD operations
 * - Guest user management
 * - Authentication queries
 * - User lifecycle operations
 * - Security features
 * - Cleanup operations
 * - Statistics and reporting
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;
    private UserEntity guestUser;
    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        // Create test data
        testUser = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        guestUser = UserEntity.createGuestUser("guest_12345");
        
        adminUser = UserEntity.builder()
                .username("admin")
                .email("admin@example.com")  
                .passwordHash("adminpassword")
                .firstName("Admin")
                .lastName("User")
                .userType(UserType.ADMIN)
                .userStatus(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        // Persist test data
        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(guestUser);
        entityManager.persistAndFlush(adminUser);
    }

    @Nested
    @DisplayName("Basic Repository Operations")
    class BasicOperationsTest {

        @Test
        @DisplayName("Should find user by username")
        void should_find_user_by_username() {
            // When
            Optional<UserEntity> found = userRepository.findByUsername("testuser");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should find user by email")
        void should_find_user_by_email() {
            // When
            Optional<UserEntity> found = userRepository.findByEmail("test@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should find user by username or email")
        void should_find_user_by_username_or_email() {
            // When
            Optional<UserEntity> foundByUsername = userRepository.findByUsernameOrEmail("testuser", "nonexistent@example.com");
            Optional<UserEntity> foundByEmail = userRepository.findByUsernameOrEmail("nonexistent", "test@example.com");

            // Then
            assertThat(foundByUsername).isPresent();
            assertThat(foundByEmail).isPresent();
            assertThat(foundByUsername.get().getId()).isEqualTo(foundByEmail.get().getId());
        }

        @Test
        @DisplayName("Should check if username exists")
        void should_check_username_exists() {
            // Then
            assertThat(userRepository.existsByUsername("testuser")).isTrue();
            assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Should check if email exists")
        void should_check_email_exists() {
            // Then
            assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("Guest User Operations")
    class GuestUserOperationsTest {

        @Test
        @DisplayName("Should find users by user type")
        void should_find_users_by_type() {
            // When
            List<UserEntity> guestUsers = userRepository.findByUserType(UserType.GUEST);
            List<UserEntity> regularUsers = userRepository.findByUserType(UserType.USER);
            List<UserEntity> adminUsers = userRepository.findByUserType(UserType.ADMIN);

            // Then
            assertThat(guestUsers).hasSize(1);
            assertThat(guestUsers.get(0).getUserType()).isEqualTo(UserType.GUEST);
            
            assertThat(regularUsers).hasSize(1);
            assertThat(regularUsers.get(0).getUserType()).isEqualTo(UserType.USER);
            
            assertThat(adminUsers).hasSize(1);
            assertThat(adminUsers.get(0).getUserType()).isEqualTo(UserType.ADMIN);
        }

        @Test
        @DisplayName("Should count guest users")
        void should_count_guest_users() {
            // When
            long guestCount = userRepository.countGuestUsers();

            // Then
            assertThat(guestCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find expired guest users")
        void should_find_expired_guest_users() {
            // Given - Use a future cutoff date to make existing guest appear expired
            LocalDateTime cutoffDate = LocalDateTime.now().plusDays(1); // Tomorrow
            List<UserEntity> expiredGuests = userRepository.findExpiredGuestUsers(cutoffDate);

            // Then
            assertThat(expiredGuests).hasSize(1);
            assertThat(expiredGuests.get(0).getUsername()).isEqualTo("guest_12345");
        }

        @Test
        @DisplayName("Should delete expired guest users")
        void should_delete_expired_guest_users() {
            // Given
            long initialCount = userRepository.countGuestUsers();

            // When - Use future cutoff date to delete existing guest
            LocalDateTime cutoffDate = LocalDateTime.now().plusDays(1);
            int deletedCount = userRepository.deleteExpiredGuestUsers(cutoffDate);

            // Then
            assertThat(deletedCount).isEqualTo(1);
            assertThat(userRepository.countGuestUsers()).isEqualTo(initialCount - 1);
        }
    }

    @Nested
    @DisplayName("Authentication Operations")
    class AuthenticationOperationsTest {

        @Test
        @DisplayName("Should find user for authentication")
        void should_find_user_for_authentication() {
            // When
            Optional<UserEntity> foundByUsername = userRepository.findByUsernameOrEmailForAuthentication("testuser");
            Optional<UserEntity> foundByEmail = userRepository.findByUsernameOrEmailForAuthentication("test@example.com");

            // Then
            assertThat(foundByUsername).isPresent();
            assertThat(foundByEmail).isPresent();
            assertThat(foundByUsername.get().getId()).isEqualTo(foundByEmail.get().getId());
        }

        @Test
        @DisplayName("Should find only active users for authentication")
        void should_find_only_active_users_for_authentication() {
            // Given - Create an inactive user
            UserEntity inactiveUser = UserEntity.builder()
                    .username("inactive")
                    .email("inactive@example.com")
                    .userStatus(UserStatus.INACTIVE)
                    .userType(UserType.USER)
                    .build();
            entityManager.persistAndFlush(inactiveUser);

            // When
            Optional<UserEntity> activeUser = userRepository.findActiveUserByUsernameOrEmail("testuser");
            Optional<UserEntity> inactiveResult = userRepository.findActiveUserByUsernameOrEmail("inactive");

            // Then
            assertThat(activeUser).isPresent();
            assertThat(inactiveResult).isEmpty();
        }

        @Test
        @DisplayName("Should find user by verified email")
        void should_find_user_by_verified_email() {
            // Given - Create user with unverified email
            UserEntity unverifiedUser = UserEntity.builder()
                    .username("unverified")
                    .email("unverified@example.com")
                    .userType(UserType.USER)
                    .userStatus(UserStatus.PENDING_VERIFICATION)
                    .emailVerified(false)
                    .build();
            entityManager.persistAndFlush(unverifiedUser);

            // When
            Optional<UserEntity> verifiedUser = userRepository.findByVerifiedEmail("test@example.com");
            Optional<UserEntity> unverifiedResult = userRepository.findByVerifiedEmail("unverified@example.com");

            // Then
            assertThat(verifiedUser).isPresent();
            assertThat(unverifiedResult).isEmpty();
        }

        @Test
        @DisplayName("Should update last login time")
        void should_update_last_login_time() {
            // Given
            LocalDateTime loginTime = LocalDateTime.now();

            // When
            userRepository.updateLastLoginTime(testUser.getId(), loginTime);
            entityManager.flush();
            entityManager.clear();

            // Then
            UserEntity updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updatedUser.getLastLoginAt()).isEqualTo(loginTime);
        }

        @Test
        @DisplayName("Should update login attempts")
        void should_update_login_attempts() {
            // When
            userRepository.updateLoginAttempts(testUser.getId(), 3);
            entityManager.flush();
            entityManager.clear();

            // Then
            UserEntity updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updatedUser.getLoginAttempts()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("User Status Operations")
    class UserStatusOperationsTest {

        @Test
        @DisplayName("Should find users by status")
        void should_find_users_by_status() {
            // When
            List<UserEntity> activeUsers = userRepository.findByUserStatus(UserStatus.ACTIVE);

            // Then
            assertThat(activeUsers).hasSize(3); // testUser, guestUser, adminUser
        }

        @Test
        @DisplayName("Should find non-guest users by status")
        void should_find_non_guest_users_by_status() {
            // When
            List<UserEntity> activeNonGuests = userRepository.findNonGuestUsersByStatus(UserStatus.ACTIVE);

            // Then
            assertThat(activeNonGuests).hasSize(2); // testUser, adminUser (excluding guest)
            assertThat(activeNonGuests).allMatch(user -> user.getUserType() != UserType.GUEST);
        }

        @Test
        @DisplayName("Should lock user")
        void should_lock_user() {
            // Given
            LocalDateTime lockTime = LocalDateTime.now().plusHours(1);

            // When
            userRepository.lockUser(testUser.getId(), UserStatus.LOCKED, lockTime);
            entityManager.flush();
            entityManager.clear();

            // Then
            UserEntity lockedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(lockedUser.getUserStatus()).isEqualTo(UserStatus.LOCKED);
            assertThat(lockedUser.getLockedUntil()).isEqualTo(lockTime);
        }

        @Test
        @DisplayName("Should unlock user")
        void should_unlock_user() {
            // Given - Lock the user first
            userRepository.lockUser(testUser.getId(), UserStatus.LOCKED, LocalDateTime.now().plusHours(1));
            userRepository.updateLoginAttempts(testUser.getId(), 5);
            entityManager.flush();

            // When
            userRepository.unlockUser(testUser.getId());
            entityManager.flush();
            entityManager.clear();

            // Then
            UserEntity unlockedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(unlockedUser.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(unlockedUser.getLockedUntil()).isNull();
            assertThat(unlockedUser.getLoginAttempts()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should find users with expired locks")
        void should_find_users_with_expired_locks() {
            // Given - Create locked user with expired lock
            UserEntity lockedUser = UserEntity.builder()
                    .username("locked")
                    .email("locked@example.com")
                    .userType(UserType.USER)
                    .userStatus(UserStatus.LOCKED)
                    .lockedUntil(LocalDateTime.now().minusHours(1)) // Expired 1 hour ago
                    .build();
            entityManager.persistAndFlush(lockedUser);

            // When
            List<UserEntity> expiredLocks = userRepository.findUsersWithExpiredLocks(LocalDateTime.now());

            // Then
            assertThat(expiredLocks).hasSize(1);
            assertThat(expiredLocks.get(0).getUsername()).isEqualTo("locked");
        }
    }

    @Nested
    @DisplayName("Email Verification Operations")
    class EmailVerificationOperationsTest {

        @Test
        @DisplayName("Should mark email as verified")
        void should_mark_email_as_verified() {
            // Given - Create user with unverified email
            UserEntity unverifiedUser = UserEntity.builder()
                    .username("unverified")
                    .email("unverified@example.com")
                    .userType(UserType.USER)
                    .emailVerified(false)
                    .build();
            entityManager.persistAndFlush(unverifiedUser);

            LocalDateTime verificationTime = LocalDateTime.now();

            // When
            userRepository.markEmailAsVerified(unverifiedUser.getId(), verificationTime);
            entityManager.flush();
            entityManager.clear();

            // Then
            UserEntity verifiedUser = userRepository.findById(unverifiedUser.getId()).orElseThrow();
            assertThat(verifiedUser.getEmailVerified()).isTrue();
            assertThat(verifiedUser.getEmailVerifiedAt()).isEqualTo(verificationTime);
        }
    }

    @Nested
    @DisplayName("Pagination and Search Operations")
    class PaginationOperationsTest {

        @Test
        @DisplayName("Should paginate users by type and status")
        void should_paginate_users_by_type_and_status() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<UserEntity> activePage = userRepository.findByUserTypeAndUserStatus(UserType.USER, UserStatus.ACTIVE, pageable);

            // Then
            assertThat(activePage.getContent()).hasSize(1);
            assertThat(activePage.getTotalElements()).isEqualTo(1);
            assertThat(activePage.getContent().get(0).getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should find non-guest users with pagination")
        void should_find_non_guest_users_with_pagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<UserEntity> nonGuestPage = userRepository.findNonGuestUsers(pageable);

            // Then
            assertThat(nonGuestPage.getContent()).hasSize(2);
            assertThat(nonGuestPage.getTotalElements()).isEqualTo(2);
            assertThat(nonGuestPage.getContent()).allMatch(user -> user.getUserType() != UserType.GUEST);
        }

        @Test
        @DisplayName("Should search users with filters")
        void should_search_users_with_filters() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When - Search by username
            Page<UserEntity> usernameSearch = userRepository.findUsersWithFilters("test", null, null, null, pageable);
            
            // When - Search by user type
            Page<UserEntity> typeSearch = userRepository.findUsersWithFilters(null, null, UserType.ADMIN, null, pageable);
            
            // When - Search by multiple filters
            Page<UserEntity> multiSearch = userRepository.findUsersWithFilters(null, null, UserType.USER, UserStatus.ACTIVE, pageable);

            // Then
            assertThat(usernameSearch.getContent()).hasSize(1);
            assertThat(usernameSearch.getContent().get(0).getUsername()).isEqualTo("testuser");
            
            assertThat(typeSearch.getContent()).hasSize(1);
            assertThat(typeSearch.getContent().get(0).getUserType()).isEqualTo(UserType.ADMIN);
            
            assertThat(multiSearch.getContent()).hasSize(1);
            assertThat(multiSearch.getContent().get(0).getUserType()).isEqualTo(UserType.USER);
            assertThat(multiSearch.getContent().get(0).getUserStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Statistics Operations")
    class StatisticsOperationsTest {

        @Test
        @DisplayName("Should count registered users")
        void should_count_registered_users() {
            // When
            long registeredCount = userRepository.countRegisteredUsers();

            // Then
            assertThat(registeredCount).isEqualTo(2); // Excluding guest user
        }

        @Test
        @DisplayName("Should count active users")
        void should_count_active_users() {
            // When
            long activeCount = userRepository.countActiveUsers();

            // Then
            assertThat(activeCount).isEqualTo(2); // testUser and adminUser (excluding guest)
        }

        @Test
        @DisplayName("Should count users created since date")
        void should_count_users_created_since_date() {
            // When
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            long recentCount = userRepository.countUsersCreatedSince(yesterday);

            // Then
            assertThat(recentCount).isGreaterThanOrEqualTo(3); // All test users created today
        }

        @Test
        @DisplayName("Should count users logged in since date")
        void should_count_users_logged_in_since_date() {
            // Given - Update login time for one user
            userRepository.updateLastLoginTime(testUser.getId(), LocalDateTime.now());
            entityManager.flush();

            // When
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            long loggedInCount = userRepository.countUsersLoggedInSince(yesterday);

            // Then
            assertThat(loggedInCount).isEqualTo(1); // Only testUser has recent login
        }
    }

    @Nested
    @DisplayName("Cleanup Operations")
    class CleanupOperationsTest {

        @Test
        @DisplayName("Should mark inactive users")
        void should_mark_inactive_users() {
            // Given - Set old login time
            testUser.setLastLoginAt(LocalDateTime.now().minusDays(31));
            entityManager.persistAndFlush(testUser);

            // When
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int markedCount = userRepository.markInactiveUsers(cutoffDate);

            // Then
            assertThat(markedCount).isEqualTo(1);
            
            entityManager.clear();
            UserEntity updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updatedUser.getUserStatus()).isEqualTo(UserStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should find inactive users")
        void should_find_inactive_users() {
            // Given - Set old login time
            testUser.setLastLoginAt(LocalDateTime.now().minusDays(31));
            entityManager.persistAndFlush(testUser);

            // When
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            List<UserEntity> inactiveUsers = userRepository.findInactiveUsers(cutoffDate);

            // Then
            assertThat(inactiveUsers).hasSize(1);
            assertThat(inactiveUsers.get(0).getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Should find users with expired passwords")
        void should_find_users_with_expired_passwords() {
            // Given - Set old password change time
            testUser.setPasswordChangedAt(LocalDateTime.now().minusDays(91));
            entityManager.persistAndFlush(testUser);

            // When
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
            List<UserEntity> expiredPasswordUsers = userRepository.findUsersWithExpiredPasswords(cutoffDate);

            // Then
            assertThat(expiredPasswordUsers).hasSize(1);
            assertThat(expiredPasswordUsers.get(0).getId()).isEqualTo(testUser.getId());
        }
    }
}