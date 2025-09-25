/**
 * Authentication Module for Spring Boot System
 * 
 * This module handles all authentication and authorization concerns including:
 * - User registration and login
 * - JWT token management
 * - Session management
 * - Two-factor authentication
 * - Password management
 * - Role-based access control
 * 
 * Module Dependencies:
 * - shared (for common utilities and configurations)
 * 
 * Module Exports:
 * - Authentication APIs
 * - User principal information
 * - Security configuration
 * 
 * Events Published:
 * - UserRegisteredEvent
 * - UserLoggedInEvent
 * - PasswordResetRequestedEvent
 * - TwoFactorEnabledEvent
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Authentication Module",
    allowedDependencies = { "shared" }
)
package dev.yehtun.spring_boot_system.auth;