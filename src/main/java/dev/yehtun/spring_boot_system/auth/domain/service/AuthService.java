package dev.yehtun.spring_boot_system.auth.domain.service;

import java.util.UUID;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;

/**
 * Service interface for authentication operations.
 * 
 * Features:
 * - User registration and authentication
 * - Guest user management
 * - Session management
 * - Password operations
 * - Account security
 */
public interface AuthService {

    /**
     * Guest User Operations
     */
    UserEntity createGuestUser();
    UserEntity createGuestUser(String deviceInfo);
    UserEntity promoteGuestToUser(UUID guestUserId, String username, String email, String password);
    void cleanupExpiredGuestUsers();
    
    /**
     * User Registration
     */
    UserEntity registerUser(String username, String email, String password);
    UserEntity registerUser(String username, String email, String password, String firstName, String lastName);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
    
    /**
     * Authentication
     */
    AuthResult authenticate(String usernameOrEmail, String password);
    AuthResult authenticate(String usernameOrEmail, String password, String deviceInfo);
    AuthResult authenticateWithTwoFactor(String sessionToken, String twoFactorCode);
    AuthResult refreshToken(String refreshToken);
    void logout(String sessionToken);
    void logoutAllSessions(UUID userId);
    
    /**
     * Password Operations
     */
    void changePassword(UUID userId, String currentPassword, String newPassword);
    void resetPasswordRequest(String email);
    void resetPassword(String resetToken, String newPassword);
    boolean validatePassword(String password);
    
    /**
     * Account Security
     */
    void lockAccount(UUID userId, String reason);
    void unlockAccount(UUID userId);
    void suspendAccount(UUID userId, String reason);
    void activateAccount(UUID userId);
    boolean isAccountLocked(UUID userId);
    boolean isAccountActive(UUID userId);
    
    /**
     * Session Management
     */
    void invalidateSession(String sessionToken);
    void invalidateAllUserSessions(UUID userId);
    boolean isValidSession(String sessionToken);
    void extendSession(String sessionToken);
    
    /**
     * Security Validation
     */
    boolean hasPermission(UUID userId, String permission);
    boolean hasRole(UUID userId, String roleName);
    void trackFailedLoginAttempt(String usernameOrEmail);
    void clearFailedLoginAttempts(UUID userId);
    
    /**
     * Account Verification
     */
    void sendEmailVerification(UUID userId);
    void verifyEmail(String verificationToken);
    boolean isEmailVerified(UUID userId);
    
    /**
     * Authentication Result DTO
     */
    record AuthResult(
        boolean success,
        String message,
        String sessionToken,
        String refreshToken,
        UserEntity user,
        boolean requiresTwoFactor,
        int expiresIn
    ) {}
}