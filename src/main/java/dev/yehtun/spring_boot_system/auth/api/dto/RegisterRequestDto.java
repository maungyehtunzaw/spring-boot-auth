package dev.yehtun.spring_boot_system.auth.api.dto;

/**
 * Request DTO for user registration
 */
public record RegisterRequestDto(
    String username,
    String email,
    String password,
    String firstName,
    String lastName
) {}