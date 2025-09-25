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
        String password = "password123";
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