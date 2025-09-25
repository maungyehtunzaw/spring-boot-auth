package dev.yehtun.spring_boot_system.auth.api.dto;

import java.util.UUID;

import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import lombok.Builder;

/**
 * Response DTO for guest user creation
 */
@Builder
public record GuestUserResponseDto(
    UUID userId,
    UserType userType,
    String message
) {}