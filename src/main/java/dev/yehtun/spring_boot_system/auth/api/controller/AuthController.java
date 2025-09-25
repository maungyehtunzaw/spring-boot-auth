package dev.yehtun.spring_boot_system.auth.api.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.yehtun.spring_boot_system.auth.api.dto.AuthRequestDto;
import dev.yehtun.spring_boot_system.auth.api.dto.AuthResponseDto;
import dev.yehtun.spring_boot_system.auth.api.dto.GuestUserResponseDto;
import dev.yehtun.spring_boot_system.auth.api.dto.RefreshTokenRequestDto;
import dev.yehtun.spring_boot_system.auth.api.dto.RegisterRequestDto;
import dev.yehtun.spring_boot_system.auth.domain.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for authentication operations.
 * 
 * Provides endpoints for:
 * - User registration and login
 * - Guest user creation
 * - Token refresh
 * - Logout operations
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Create a guest user
     */
    @PostMapping("/guest")
    public ResponseEntity<GuestUserResponseDto> createGuestUser() {
        log.debug("Creating guest user");
        
        var guestUser = authService.createGuestUser();
        
        var response = GuestUserResponseDto.builder()
            .userId(guestUser.getId())
            .userType(guestUser.getUserType())
            .message("Guest user created successfully")
            .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        log.debug("Registering new user: {}", request.username());
        
        try {
            var user = authService.registerUser(
                request.username(), 
                request.email(), 
                request.password(),
                request.firstName(),
                request.lastName()
            );
            
            var response = AuthResponseDto.builder()
                .success(true)
                .message("Registration successful")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userType(user.getUserType())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            var response = AuthResponseDto.builder()
                .success(false)
                .message(e.getMessage())
                .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Authenticate user login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        log.debug("Authenticating user: {}", request.usernameOrEmail());
        
        var authResult = authService.authenticate(request.usernameOrEmail(), request.password());
        
        var response = AuthResponseDto.builder()
            .success(authResult.success())
            .message(authResult.message())
            .sessionToken(authResult.sessionToken())
            .refreshToken(authResult.refreshToken())
            .requiresTwoFactor(authResult.requiresTwoFactor())
            .expiresIn(authResult.expiresIn())
            .build();
        
        if (authResult.user() != null) {
            response = response.toBuilder()
                .userId(authResult.user().getId())
                .username(authResult.user().getUsername())
                .email(authResult.user().getEmail())
                .userType(authResult.user().getUserType())
                .build();
        }
        
        if (authResult.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Refresh authentication token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        log.debug("Refreshing token");
        
        var authResult = authService.refreshToken(request.refreshToken());
        
        var response = AuthResponseDto.builder()
            .success(authResult.success())
            .message(authResult.message())
            .sessionToken(authResult.sessionToken())
            .refreshToken(authResult.refreshToken())
            .expiresIn(authResult.expiresIn())
            .build();
        
        if (authResult.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Promote guest user to regular user
     */
    @PostMapping("/promote-guest")
    public ResponseEntity<AuthResponseDto> promoteGuestUser(@Valid @RequestBody PromoteGuestRequestDto request) {
        log.debug("Promoting guest user: {}", request.guestUserId());
        
        try {
            var user = authService.promoteGuestToUser(
                request.guestUserId(),
                request.username(),
                request.email(), 
                request.password()
            );
            
            var response = AuthResponseDto.builder()
                .success(true)
                .message("Guest user promoted successfully")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userType(user.getUserType())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            var response = AuthResponseDto.builder()
                .success(false)
                .message(e.getMessage())
                .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * DTO for promoting guest user
     */
    public record PromoteGuestRequestDto(
        UUID guestUserId,
        String username,
        String email,
        String password
    ) {}
}