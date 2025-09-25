package dev.yehtun.spring_boot_system.auth.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;

/**
 * Simple repository test to identify the specific field mismatch issue.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Basic Test")
class UserRepositoryBasicTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // Create a basic test user with minimal required fields
        testUser = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword123")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .loginAttempts(0)
                .build();

        // Persist entity
        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("Should save and find user by username")
    void shouldSaveAndFindUserByUsername() {
        // When
        Optional<UserEntity> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // When
        Optional<UserEntity> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should check if username exists")
    void shouldCheckIfUsernameExists() {
        // When & Then
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }
}