package dev.yehtun.spring_boot_system.auth.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserSessionEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import dev.yehtun.spring_boot_system.auth.infrastructure.repository.UserRepository;
import dev.yehtun.spring_boot_system.auth.infrastructure.service.AuthServiceImpl;

/**
 * Unit tests for AuthService implementation
 * Tests authentication and registration business logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private SessionService sessionService;
    
    @Mock
    private SecurityService securityService;
    
    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity createActiveUser() {
        return UserEntity.builder()
                .username("activeUser")
                .email("active@example.com")
                .passwordHash("encoded")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should create guest user successfully")
    void shouldCreateGuestUser() {
        // Given
        UserEntity guestUser = UserEntity.builder()
                .username("guest_12345678")
                .userType(UserType.GUEST)
                .build();
        
        when(userRepository.save(any(UserEntity.class))).thenReturn(guestUser);
        
        // When
        UserEntity result = authService.createGuestUser();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).startsWith("guest_");
        assertThat(result.getUserType()).isEqualTo(UserType.GUEST);
        
        verify(userRepository).save(any(UserEntity.class));
    }
    
    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUser() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "Password123"; // Valid password with uppercase, lowercase, and digit
        String encodedPassword = "encoded-password";
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        
        UserEntity savedUser = UserEntity.builder()
                .username(username)
                .email(email)
                .passwordHash(encodedPassword)
                .userType(UserType.USER)
                .build();
        
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        
        // When
        UserEntity result = authService.registerUser(username, email, password);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getUserType()).isEqualTo(UserType.USER);
        
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should promote guest user to regular user")
    void shouldPromoteGuestUser() {
        // Given
        UserEntity guestUser = UserEntity.createGuestUser("guest_1234");
        guestUser.setId(java.util.UUID.randomUUID());
        when(userRepository.findById(any())).thenReturn(java.util.Optional.of(guestUser));
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserEntity result = authService.promoteGuestToUser(
                java.util.UUID.randomUUID(), "newUser", "new@example.com", "Password123");

        // Then
        assertThat(result.getUserType()).isEqualTo(UserType.USER);
        assertThat(result.getUsername()).isEqualTo("newUser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(guestUser);
    }

    @Test
    @DisplayName("Should throw when promoting non-guest user")
    void shouldNotPromoteNonGuestUser() {
        // Given
        UserEntity existingUser = createActiveUser();
        existingUser.setUserType(UserType.USER);
        when(userRepository.findById(any())).thenReturn(java.util.Optional.of(existingUser));

        // When / Then
        assertThatThrownBy(() -> authService.promoteGuestToUser(
                java.util.UUID.randomUUID(), "newUser", "new@example.com", "Password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not a guest user");
    }

    @Test
    @DisplayName("Should prevent promotion when username is taken")
    void shouldNotPromoteGuestWhenUsernameTaken() {
        UserEntity guestUser = UserEntity.createGuestUser("guest");
        when(userRepository.findById(any())).thenReturn(java.util.Optional.of(guestUser));
        when(userRepository.existsByUsername("newUser")).thenReturn(true);

        assertThatThrownBy(() -> authService.promoteGuestToUser(
                java.util.UUID.randomUUID(), "newUser", "new@example.com", "Password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username is already taken");
    }

    @Test
    @DisplayName("Should authenticate valid user and create session")
    void shouldAuthenticateUser() {
        UserEntity user = createActiveUser();
        user.setId(java.util.UUID.randomUUID());

        when(userRepository.findByUsernameOrEmail("activeUser", "activeUser"))
            .thenReturn(java.util.Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(securityService.isTwoFactorEnabled(user.getId())).thenReturn(false);

        UserSessionEntity sessionEntity = UserSessionEntity.builder()
            .user(user)
            .sessionToken("session-token")
            .refreshToken("refresh-token")
            .expiresAt(java.time.LocalDateTime.now().plusHours(1))
            .isActive(true)
            .build();

        when(sessionService.createSession(user, null)).thenReturn(sessionEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        AuthService.AuthResult result = authService.authenticate("activeUser", "Password123");

        assertThat(result.success()).isTrue();
        assertThat(result.sessionToken()).isEqualTo("session-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user()).isEqualTo(user);

        verify(securityService).clearFailedLoginAttempts(user.getId());
        verify(sessionService).createSession(user, null);
    }

    @Test
    @DisplayName("Should return two-factor required when enabled")
    void shouldRequireTwoFactorAuthentication() {
        UserEntity user = createActiveUser();
        user.setId(java.util.UUID.randomUUID());

        when(userRepository.findByUsernameOrEmail("activeUser", "activeUser"))
            .thenReturn(java.util.Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPasswordHash())).thenReturn(true);
        when(securityService.isTwoFactorEnabled(user.getId())).thenReturn(true);

        AuthService.AuthResult result = authService.authenticate("activeUser", "Password123");

        assertThat(result.success()).isTrue();
        assertThat(result.requiresTwoFactor()).isTrue();
        assertThat(result.sessionToken()).isNull();
        verify(sessionService, never()).createSession(any(), any());
    }

    @Test
    @DisplayName("Should track failed login attempts when user not found")
    void shouldHandleAuthenticationFailureForUnknownUser() {
        when(userRepository.findByUsernameOrEmail("missing", "missing"))
            .thenReturn(java.util.Optional.empty());

        AuthService.AuthResult result = authService.authenticate("missing", "Password123");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Invalid credentials");
        verify(securityService).trackFailedLoginAttempt(eq("missing"), isNull());
    }

    @Test
    @DisplayName("Should reject weak passwords during validation")
    void shouldValidatePasswordStrength() {
        assertThat(authService.validatePassword("short")).isFalse();
        assertThat(authService.validatePassword("alllowercase"))
            .isFalse();
        assertThat(authService.validatePassword("NoDigitsHere"))
            .isFalse();
        assertThat(authService.validatePassword("StrongPass1"))
            .isTrue();
    }

    @Test
    @DisplayName("Should check username availability")
    void shouldCheckUsernameAvailability() {
        // Given
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(false);
        
        // When
        boolean result = authService.isUsernameAvailable(username);
        
        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByUsername(username);
    }
    
    @Test
    @DisplayName("Should check email availability")
    void shouldCheckEmailAvailability() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = authService.isEmailAvailable(email);
        
        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }
}