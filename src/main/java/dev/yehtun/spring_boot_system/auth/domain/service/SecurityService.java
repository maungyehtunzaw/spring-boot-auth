package dev.yehtun.spring_boot_system.auth.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.yehtun.spring_boot_system.auth.domain.entity.RoleEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.TwoFactorAuthEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserDeviceEntity;

/**
 * Service interface for security operations.
 * 
 * Features:
 * - Two-factor authentication management
 * - Device management and trust
 * - Permission and role security
 * - Security monitoring and alerts
 * - Password and encryption utilities
 */
public interface SecurityService {

    /**
     * Two-Factor Authentication
     */
    TwoFactorAuthEntity setupTwoFactor(UUID userId, String method);
    boolean enableTwoFactor(UUID userId, String verificationCode);
    boolean disableTwoFactor(UUID userId, String password);
    boolean verifyTwoFactorCode(UUID userId, String code);
    boolean verifyBackupCode(UUID userId, String backupCode);
    List<String> generateBackupCodes(UUID userId);
    List<String> regenerateBackupCodes(UUID userId);
    boolean isTwoFactorEnabled(UUID userId);
    boolean isTwoFactorVerified(UUID userId);
    Optional<TwoFactorAuthEntity> getTwoFactorAuth(UUID userId);
    String generateQRCodeUrl(UUID userId);
    
    /**
     * Device Management
     */
    UserDeviceEntity registerDevice(UUID userId, String deviceId, String deviceInfo);
    UserDeviceEntity registerDevice(UUID userId, String deviceId, String deviceInfo, String ipAddress);
    void trustDevice(UUID userId, String deviceId);
    void untrustDevice(UUID userId, String deviceId);
    void removeDevice(UUID userId, String deviceId);
    List<UserDeviceEntity> getUserDevices(UUID userId);
    List<UserDeviceEntity> getTrustedDevices(UUID userId);
    boolean isDeviceTrusted(UUID userId, String deviceId);
    boolean isDeviceRegistered(UUID userId, String deviceId);
    void updateDeviceLastUsed(String deviceId, String ipAddress);
    void cleanupInactiveDevices();
    
    /**
     * Permission Management
     */
    boolean hasPermission(UUID userId, String permission);
    boolean hasPermissions(UUID userId, Set<String> permissions);
    boolean hasAnyPermission(UUID userId, Set<String> permissions);
    List<String> getUserPermissions(UUID userId);
    Set<String> getUserPermissionSet(UUID userId);
    boolean canPerformAction(UUID userId, String resource, String action);
    void checkPermission(UUID userId, String permission);
    void checkPermissions(UUID userId, Set<String> permissions);
    
    /**
     * Role Management
     */
    boolean hasRole(UUID userId, String roleName);
    boolean hasRoles(UUID userId, Set<String> roleNames);
    boolean hasAnyRole(UUID userId, Set<String> roleNames);
    List<RoleEntity> getUserRoles(UUID userId);
    Set<String> getUserRoleNames(UUID userId);
    boolean isAdmin(UUID userId);
    boolean isSystemUser(UUID userId);
    void checkRole(UUID userId, String roleName);
    void checkAnyRole(UUID userId, Set<String> roleNames);
    
    /**
     * Password Security
     */
    String hashPassword(String plainPassword);
    boolean verifyPassword(String plainPassword, String hashedPassword);
    boolean isPasswordStrong(String password);
    PasswordStrength checkPasswordStrength(String password);
    List<String> getPasswordRequirements();
    boolean isPasswordReused(UUID userId, String newPassword);
    void trackPasswordHistory(UUID userId, String hashedPassword);
    void clearPasswordHistory(UUID userId);
    
    /**
     * Security Monitoring
     */
    void trackFailedLoginAttempt(String usernameOrEmail, String ipAddress);
    void clearFailedLoginAttempts(UUID userId);
    boolean isAccountLocked(UUID userId);
    void lockAccount(UUID userId, String reason);
    void unlockAccount(UUID userId);
    List<SecurityEvent> getSecurityEvents(UUID userId);
    List<SecurityEvent> getRecentSecurityEvents(LocalDateTime since);
    void logSecurityEvent(UUID userId, String eventType, String description);
    
    /**
     * Session Security
     */
    boolean isValidSessionToken(String sessionToken);
    boolean isSessionFromTrustedDevice(String sessionToken);
    void validateSessionSecurity(String sessionToken);
    List<SuspiciousActivity> detectSuspiciousActivity(UUID userId);
    void flagSuspiciousSession(String sessionToken, String reason);
    void terminateSuspiciousSessions(UUID userId);
    
    /**
     * IP and Location Security
     */
    boolean isIpTrusted(String ipAddress);
    boolean isLocationTrusted(UUID userId, String location);
    void trustIpAddress(String ipAddress);
    void untrustIpAddress(String ipAddress);
    void blockIpAddress(String ipAddress, String reason);
    void unblockIpAddress(String ipAddress);
    boolean isIpBlocked(String ipAddress);
    List<String> getTrustedIpAddresses();
    List<String> getBlockedIpAddresses();
    
    /**
     * Rate Limiting
     */
    boolean isRateLimited(String identifier, String action);
    void recordAction(String identifier, String action);
    void clearRateLimit(String identifier, String action);
    RateLimitStatus getRateLimitStatus(String identifier, String action);
    void configureRateLimit(String action, int maxAttempts, int windowMinutes);
    
    /**
     * Encryption and Tokens
     */
    String generateSecureToken();
    String generateSecureToken(int length);
    String encrypt(String plainText);
    String decrypt(String encryptedText);
    String generateSecretKey();
    boolean isTokenValid(String token, String expectedHash);
    String createSignedToken(String payload);
    boolean verifySignedToken(String token);
    
    /**
     * Security Validation
     */
    ValidationResult validateUserAccess(UUID userId, String resource, String action);
    ValidationResult validateSessionAccess(String sessionToken, String resource);
    ValidationResult validateApiAccess(String apiKey, String endpoint);
    boolean isSecureContext();
    void enforceSecureContext();
    
    /**
     * Security Configuration
     */
    void setPasswordPolicy(PasswordPolicy policy);
    PasswordPolicy getPasswordPolicy();
    void setSecuritySettings(SecuritySettings settings);
    SecuritySettings getSecuritySettings();
    void enableSecurityFeature(String feature);
    void disableSecurityFeature(String feature);
    
    /**
     * Security Cleanup
     */
    void cleanupExpiredTokens();
    void cleanupOldSecurityEvents();
    void cleanupFailedAttempts();
    void performSecurityMaintenance();
    
    /**
     * Password Strength Enum
     */
    enum PasswordStrength {
        VERY_WEAK, WEAK, FAIR, GOOD, STRONG, VERY_STRONG
    }
    
    /**
     * Validation Result DTO
     */
    record ValidationResult(
        boolean valid,
        String reason,
        Set<String> missingPermissions,
        Set<String> requiredRoles
    ) {}
    
    /**
     * Security Event DTO
     */
    record SecurityEvent(
        UUID id,
        UUID userId,
        String eventType,
        String description,
        String ipAddress,
        String userAgent,
        LocalDateTime timestamp,
        String severity
    ) {}
    
    /**
     * Suspicious Activity DTO
     */
    record SuspiciousActivity(
        String type,
        String description,
        String ipAddress,
        LocalDateTime timestamp,
        String riskLevel,
        String recommendation
    ) {}
    
    /**
     * Rate Limit Status DTO
     */
    record RateLimitStatus(
        boolean limited,
        int attempts,
        int maxAttempts,
        LocalDateTime windowStart,
        LocalDateTime resetTime
    ) {}
    
    /**
     * Password Policy DTO
     */
    record PasswordPolicy(
        int minLength,
        int maxLength,
        boolean requireUppercase,
        boolean requireLowercase,
        boolean requireNumbers,
        boolean requireSpecialChars,
        boolean preventReuse,
        int historySize,
        int maxAge
    ) {}
    
    /**
     * Security Settings DTO
     */
    record SecuritySettings(
        boolean twoFactorRequired,
        boolean deviceTrackingEnabled,
        boolean ipWhitelistEnabled,
        boolean locationTrackingEnabled,
        int sessionTimeoutMinutes,
        int maxFailedAttempts,
        int lockoutDurationMinutes,
        boolean suspiciousActivityMonitoring
    ) {}
}