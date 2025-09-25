package dev.yehtun.spring_boot_system.auth.api.dto;

/**
 * Request DTO for user authentication
 */
public record AuthRequestDto(
    String usernameOrEmail,
    String password
) {}