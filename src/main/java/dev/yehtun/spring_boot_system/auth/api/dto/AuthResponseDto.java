package dev.yehtun.spring_boot_system.auth.api.dto;

import java.util.UUID;

import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import lombok.Builder;

/**
 * Response DTO for authentication operations
 */
@Builder(toBuilder = true)
public record AuthResponseDto(
    boolean success,
    String message,
    String sessionToken,
    String refreshToken,
    UUID userId,
    String username,
    String email,
    UserType userType,
    boolean requiresTwoFactor,
    int expiresIn
) {}