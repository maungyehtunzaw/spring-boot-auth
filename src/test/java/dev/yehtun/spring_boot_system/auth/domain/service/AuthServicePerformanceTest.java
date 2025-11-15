package dev.yehtun.spring_boot_system.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserSessionEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import dev.yehtun.spring_boot_system.auth.infrastructure.repository.UserRepository;
import dev.yehtun.spring_boot_system.auth.infrastructure.service.AuthServiceImpl;
import dev.yehtun.spring_boot_system.auth.domain.service.SessionService;
import dev.yehtun.spring_boot_system.auth.domain.service.SecurityService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth service performance characteristics")
class AuthServicePerformanceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private SecurityService securityService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    private UserEntity activeUser;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, sessionService, securityService, passwordEncoder);

        activeUser = UserEntity.builder()
            .username("performance-user")
            .email("perf@example.com")
            .passwordHash("hashed")
            .userStatus(UserStatus.ACTIVE)
            .userType(UserType.USER)
            .build();
        activeUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Authenticate completes within an acceptable threshold")
    void authenticateCompletesUnderPerformanceBudget() {
        when(userRepository.findByUsernameOrEmail(eq("performance-user"), eq("performance-user")))
            .thenReturn(Optional.of(activeUser));
        when(userRepository.findById(activeUser.getId())).thenReturn(Optional.of(activeUser));
        when(userRepository.save(activeUser)).thenReturn(activeUser);
        when(passwordEncoder.matches("Password123!", "hashed")).thenReturn(true);
        when(securityService.isTwoFactorEnabled(activeUser.getId())).thenReturn(false);
        doNothing().when(securityService).clearFailedLoginAttempts(activeUser.getId());
        when(sessionService.createSession(eq(activeUser), any()))
            .thenReturn(UserSessionEntity.builder()
                .user(activeUser)
                .sessionToken("session-token")
                .refreshToken("refresh-token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .refreshTokenExpiresAt(LocalDateTime.now().plusDays(7))
                .build());

        int iterations = 25;
        long totalDurationNanos = 0L;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            AuthService.AuthResult result = authService.authenticate("performance-user", "Password123!");
            long duration = System.nanoTime() - start;

            assertThat(result.success()).isTrue();
            totalDurationNanos += duration;
        }

        Duration averageExecution = Duration.ofNanos(totalDurationNanos / iterations);
        assertThat(averageExecution).isLessThan(Duration.ofMillis(100));
    }
}
