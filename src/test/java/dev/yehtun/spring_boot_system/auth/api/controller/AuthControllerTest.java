package dev.yehtun.spring_boot_system.auth.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.yehtun.spring_boot_system.auth.api.dto.AuthRequestDto;
import dev.yehtun.spring_boot_system.auth.api.dto.RefreshTokenRequestDto;
import dev.yehtun.spring_boot_system.auth.api.dto.RegisterRequestDto;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import dev.yehtun.spring_boot_system.auth.domain.service.AuthService;

/**
 * Web MVC tests for {@link AuthController} verifying request/response mappings and
 * error handling behaviour.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/guest returns created guest user information")
    void createGuestUser() throws Exception {
        UserEntity guest = UserEntity.createGuestUser("guest_abc123");
        guest.setId(UUID.randomUUID());

        when(authService.createGuestUser()).thenReturn(guest);

        mockMvc.perform(post("/api/auth/guest"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userId").value(guest.getId().toString()))
            .andExpect(jsonPath("$.userType").value(UserType.GUEST.name()))
            .andExpect(jsonPath("$.message").value("Guest user created successfully"));
    }

    @Nested
    @DisplayName("Registration endpoint")
    class Registration {
        @Test
        @DisplayName("returns 200 with user details when registration succeeds")
        void registerSuccess() throws Exception {
            RegisterRequestDto request = new RegisterRequestDto(
                "newuser", "new@example.com", "Password123", "New", "User");

            UserEntity registered = UserEntity.builder()
                .username("newuser")
                .email("new@example.com")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
            registered.setId(UUID.randomUUID());

            when(authService.registerUser(eq("newuser"), eq("new@example.com"), eq("Password123"),
                eq("New"), eq("User"))).thenReturn(registered);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").value(registered.getId().toString()))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
        }

        @Test
        @DisplayName("returns 400 when service throws validation error")
        void registerValidationFailure() throws Exception {
            RegisterRequestDto request = new RegisterRequestDto(
                "newuser", "duplicate@example.com", "Password123", "New", "User");

            when(authService.registerUser(anyString(), anyString(), anyString(), any(), any()))
                .thenThrow(new IllegalArgumentException("Email is already in use"));

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is already in use"));
        }
    }

    @Nested
    @DisplayName("Authentication endpoint")
    class Authentication {
        @Test
        @DisplayName("returns tokens and user info for successful login")
        void loginSuccess() throws Exception {
            AuthRequestDto request = new AuthRequestDto("user@example.com", "Password123");

            UserEntity user = UserEntity.builder()
                .username("newuser")
                .email("user@example.com")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
            user.setId(UUID.randomUUID());

            AuthService.AuthResult result = new AuthService.AuthResult(true, "ok",
                "session-token", "refresh-token", user, false, 3600);

            when(authService.authenticate(eq("user@example.com"), eq("Password123")))
                .thenReturn(result);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sessionToken").value("session-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.userId").value(user.getId().toString()));
        }

        @Test
        @DisplayName("returns 401 when authentication fails")
        void loginFailure() throws Exception {
            AuthRequestDto request = new AuthRequestDto("user@example.com", "Password123");

            AuthService.AuthResult failure = new AuthService.AuthResult(false, "Invalid credentials",
                null, null, null, false, 0);

            when(authService.authenticate(eq("user@example.com"), eq("Password123")))
                .thenReturn(failure);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }
    }

    @Nested
    @DisplayName("Token refresh endpoint")
    class TokenRefresh {
        @Test
        @DisplayName("returns new tokens on success")
        void refreshSuccess() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("refresh-token");

            AuthService.AuthResult refreshed = new AuthService.AuthResult(true, "Token refreshed successfully",
                "new-session", "new-refresh", null, false, 3600);

            when(authService.refreshToken("refresh-token")).thenReturn(refreshed);

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").value("new-session"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
        }

        @Test
        @DisplayName("returns 401 for invalid refresh token")
        void refreshFailure() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalid");

            AuthService.AuthResult failure = new AuthService.AuthResult(false, "Refresh token not found",
                null, null, null, false, 0);
            when(authService.refreshToken("invalid")).thenReturn(failure);

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token not found"));
        }
    }

    @Nested
    @DisplayName("Promote guest endpoint")
    class PromoteGuest {
        @Test
        @DisplayName("returns promoted user when successful")
        void promoteGuestSuccess() throws Exception {
            AuthController.PromoteGuestRequestDto request = new AuthController.PromoteGuestRequestDto(
                UUID.randomUUID(), "promoted", "promoted@example.com", "Password123");

            UserEntity user = UserEntity.builder()
                .username("promoted")
                .email("promoted@example.com")
                .userType(UserType.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
            user.setId(UUID.randomUUID());

            when(authService.promoteGuestToUser(eq(request.guestUserId()), eq("promoted"),
                eq("promoted@example.com"), eq("Password123"))).thenReturn(user);

            mockMvc.perform(post("/api/auth/promote-guest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("promoted"));
        }

        @Test
        @DisplayName("returns 400 when promotion fails")
        void promoteGuestFailure() throws Exception {
            AuthController.PromoteGuestRequestDto request = new AuthController.PromoteGuestRequestDto(
                UUID.randomUUID(), "promoted", "promoted@example.com", "Password123");

            when(authService.promoteGuestToUser(any(UUID.class), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Username is already taken"));

            mockMvc.perform(post("/api/auth/promote-guest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username is already taken"));
        }
    }
}
