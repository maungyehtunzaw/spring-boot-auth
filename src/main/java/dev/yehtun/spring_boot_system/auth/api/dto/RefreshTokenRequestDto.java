package dev.yehtun.spring_boot_system.auth.api.dto;

/**
 * Request DTO for token refresh
 */
public record RefreshTokenRequestDto(
    String refreshToken
) {}