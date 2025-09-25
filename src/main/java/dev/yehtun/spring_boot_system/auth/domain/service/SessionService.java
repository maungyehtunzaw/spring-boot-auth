package dev.yehtun.spring_boot_system.auth.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserSessionEntity;

/**
 * Service interface for session management operations.
 * 
 * Features:
 * - Session creation and validation
 * - Session tracking and monitoring
 * - Security and cleanup operations
 * - Device and location tracking
 */
public interface SessionService {

    /**
     * Session Creation and Management
     */
    UserSessionEntity createSession(UserEntity user, String deviceInfo);
    UserSessionEntity createSession(UserEntity user, String deviceInfo, String ipAddress, String userAgent);
    UserSessionEntity createGuestSession(String deviceInfo);
    UserSessionEntity createGuestSession(String deviceInfo, String ipAddress);
    Optional<UserSessionEntity> findBySessionToken(String sessionToken);
    Optional<UserSessionEntity> findByRefreshToken(String refreshToken);
    void invalidateSession(String sessionToken);
    void invalidateAllUserSessions(UUID userId);
    
    /**
     * Session Validation
     */
    boolean isValidSession(String sessionToken);
    boolean isValidRefreshToken(String refreshToken);
    boolean isSessionExpired(String sessionToken);
    boolean isRefreshTokenExpired(String refreshToken);
    SessionValidationResult validateSession(String sessionToken);
    SessionValidationResult validateRefreshToken(String refreshToken);
    
    /**
     * Session Activity Tracking
     */
    void updateSessionActivity(String sessionToken);
    void updateSessionActivity(String sessionToken, String ipAddress);
    void extendSession(String sessionToken);
    void extendSession(String sessionToken, int additionalMinutes);
    LocalDateTime getSessionLastActivity(String sessionToken);
    LocalDateTime getSessionExpiryTime(String sessionToken);
    
    /**
     * Token Management
     */
    String generateSessionToken();
    String generateRefreshToken();
    RefreshTokenResult refreshSessionToken(String refreshToken);
    void updateRefreshToken(UUID sessionId, String newRefreshToken);
    void revokeRefreshToken(String refreshToken);
    
    /**
     * User Session Queries
     */
    List<UserSessionEntity> findUserSessions(UUID userId);
    List<UserSessionEntity> findActiveUserSessions(UUID userId);
    List<UserSessionEntity> findUserSessionsFromIp(UUID userId, String ipAddress);
    long countActiveUserSessions(UUID userId);
    long countUserSessionsFromIp(UUID userId, String ipAddress);
    Page<UserSessionEntity> findUserSessionHistory(UUID userId, Pageable pageable);
    
    /**
     * Session Security
     */
    List<UserSessionEntity> findSuspiciousSessions(UUID userId);
    List<UserSessionEntity> findSessionsFromDifferentLocation(UUID userId, String currentIp);
    List<UserSessionEntity> findConcurrentSessions(UUID userId);
    void flagSuspiciousSession(UUID sessionId, String reason);
    boolean isSessionFromTrustedDevice(String sessionToken);
    boolean exceedsMaxConcurrentSessions(UUID userId);
    
    /**
     * Session Cleanup
     */
    void cleanupExpiredSessions();
    void cleanupInactiveSessions();
    void cleanupExpiredGuestSessions();
    void cleanupOldSessions(LocalDateTime cutoffDate);
    int deleteExpiredSessions();
    int deleteInactiveSessions(LocalDateTime cutoffTime);
    int deleteSessionsOlderThan(LocalDateTime cutoffDate);
    
    /**
     * Session Monitoring
     */
    List<UserSessionEntity> findActiveSessions();
    List<UserSessionEntity> findRecentSessions(LocalDateTime since);
    List<UserSessionEntity> findSessionsByIpAddress(String ipAddress);
    List<UserSessionEntity> findSessionsByUserAgent(String userAgent);
    long countActiveSessionsFromIp(String ipAddress);
    long countRecentSessionsFromIp(String ipAddress, LocalDateTime since);
    
    /**
     * Session Statistics
     */
    long getTotalActiveSessions();
    long getTotalSessionsToday();
    long getUniqueActiveUsers();
    SessionStatistics getSessionStatistics();
    List<SessionLocationStats> getSessionsByLocation();
    List<SessionDeviceStats> getSessionsByDevice();
    
    /**
     * Advanced Session Operations
     */
    void transferSession(String sessionToken, String newDeviceInfo);
    void duplicateSession(String sessionToken, String newDeviceInfo);
    void mergeUserSessions(UUID sourceUserId, UUID targetUserId);
    void convertGuestSession(String sessionToken, UUID newUserId);
    void upgradeGuestSession(String sessionToken, UserEntity user);
    
    /**
     * Session Search and Filtering
     */
    Page<UserSessionEntity> findSessionsWithFilters(
        UUID userId,
        String ipAddress,
        Boolean active,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        Pageable pageable
    );
    Page<UserSessionEntity> searchSessions(String searchTerm, Pageable pageable);
    List<UserSessionEntity> findSessionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Session Configuration
     */
    void setSessionTimeout(int timeoutMinutes);
    void setRefreshTokenExpiration(int expirationDays);
    void setMaxConcurrentSessions(int maxSessions);
    void enableSessionExtension(boolean enabled);
    SessionConfiguration getSessionConfiguration();
    
    /**
     * Session Validation Result DTO
     */
    record SessionValidationResult(
        boolean valid,
        boolean expired,
        String reason,
        UserSessionEntity session,
        LocalDateTime expiresAt
    ) {}
    
    /**
     * Refresh Token Result DTO
     */
    record RefreshTokenResult(
        boolean success,
        String newSessionToken,
        String newRefreshToken,
        LocalDateTime expiresAt,
        String errorMessage
    ) {}
    
    /**
     * Session Statistics DTO
     */
    record SessionStatistics(
        long totalActiveSessions,
        long totalSessionsToday,
        long uniqueActiveUsers,
        long averageSessionDuration,
        long peakConcurrentSessions,
        double averageSessionsPerUser
    ) {}
    
    /**
     * Session Location Statistics DTO
     */
    record SessionLocationStats(
        String location,
        String country,
        long sessionCount,
        long uniqueUsers
    ) {}
    
    /**
     * Session Device Statistics DTO
     */
    record SessionDeviceStats(
        String deviceType,
        String platform,
        String browser,
        long sessionCount,
        long uniqueUsers
    ) {}
    
    /**
     * Session Configuration DTO
     */
    record SessionConfiguration(
        int sessionTimeoutMinutes,
        int refreshTokenExpirationDays,
        int maxConcurrentSessions,
        boolean sessionExtensionEnabled,
        boolean deviceTrackingEnabled,
        boolean locationTrackingEnabled
    ) {}
}