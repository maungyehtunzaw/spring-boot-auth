package dev.yehtun.spring_boot_system.auth.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserSessionEntity;

/**
 * Repository interface for UserSessionEntity operations.
 * 
 * Features:
 * - Session management and tracking
 * - Active session queries
 * - Session cleanup and expiration
 * - Security and audit operations
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {

    // Basic session queries
    Optional<UserSessionEntity> findBySessionToken(String sessionToken);
    
    Optional<UserSessionEntity> findByRefreshToken(String refreshToken);
    
    List<UserSessionEntity> findByUser(UserEntity user);
    
    List<UserSessionEntity> findByUserId(UUID userId);

    // Active session queries
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user = :user AND s.isActive = true")
    List<UserSessionEntity> findActiveSessionsByUser(@Param("user") UserEntity user);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.id = :userId AND s.isActive = true")
    List<UserSessionEntity> findActiveSessionsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.isActive = true")
    List<UserSessionEntity> findAllActiveSessions();

    // Session expiration queries
    @Query("SELECT s FROM UserSessionEntity s WHERE s.expiresAt < :currentTime")
    List<UserSessionEntity> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.id = :userId AND s.expiresAt < :currentTime")
    List<UserSessionEntity> findExpiredSessionsByUserId(@Param("userId") UUID userId, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.refreshTokenExpiresAt < :currentTime")
    List<UserSessionEntity> findExpiredRefreshTokens(@Param("currentTime") LocalDateTime currentTime);

    // Session device and location queries
    @Query("SELECT s FROM UserSessionEntity s WHERE s.ipAddress = :ipAddress")
    List<UserSessionEntity> findByIpAddress(@Param("ipAddress") String ipAddress);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.userAgent = :userAgent")
    List<UserSessionEntity> findByUserAgent(@Param("userAgent") String userAgent);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.deviceFingerprint = :deviceFingerprint")
    List<UserSessionEntity> findByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.id = :userId AND s.ipAddress = :ipAddress")
    List<UserSessionEntity> findByUserIdAndIpAddress(@Param("userId") UUID userId, @Param("ipAddress") String ipAddress);

    // Session activity tracking
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.id = :userId AND s.isActive = false AND s.createdAt < :cutoffTime")
    List<UserSessionEntity> findInactiveSessionsByUserId(@Param("userId") UUID userId, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.lastAccessedAt >= :fromTime")
    List<UserSessionEntity> findRecentlyActiveSessions(@Param("fromTime") LocalDateTime fromTime);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.id = :userId AND s.ipAddress != :currentIp AND s.isActive = true")
    List<UserSessionEntity> findActiveSessionsFromDifferentIp(@Param("userId") UUID userId, @Param("currentIp") String currentIp);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.id = :userId AND s.createdAt >= :fromTime")
    List<UserSessionEntity> findRecentSessionsByUserId(@Param("userId") UUID userId, @Param("fromTime") LocalDateTime fromTime);
    
    @Query("SELECT COUNT(s) FROM UserSessionEntity s WHERE s.ipAddress = :ipAddress AND s.createdAt >= :fromTime")
    long countSessionsFromIpSince(@Param("ipAddress") String ipAddress, @Param("fromTime") LocalDateTime fromTime);

    // Session statistics
    @Query("SELECT COUNT(s) FROM UserSessionEntity s WHERE s.isActive = true")
    long countActiveSessions();
    
    @Query("SELECT COUNT(s) FROM UserSessionEntity s WHERE s.user.id = :userId AND s.isActive = true")
    long countActiveSessionsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(s) FROM UserSessionEntity s WHERE s.createdAt >= :fromDate")
    long countSessionsCreatedSince(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(s) FROM UserSessionEntity s WHERE s.lastAccessedAt >= :fromDate")
    long countActiveSessionsSince(@Param("fromDate") LocalDateTime fromDate);

    // Session cleanup operations
    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.isActive = false, s.endedAt = :endTime WHERE s.id = :sessionId")
    int endSession(@Param("sessionId") UUID sessionId, @Param("endTime") LocalDateTime endTime);
    
    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.isActive = false, s.endedAt = :endTime WHERE s.user.id = :userId")
    int endAllUserSessions(@Param("userId") UUID userId, @Param("endTime") LocalDateTime endTime);
    
    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.isActive = false, s.endedAt = :endTime WHERE s.expiresAt < :currentTime")
    int expireExpiredSessions(@Param("currentTime") LocalDateTime currentTime, @Param("endTime") LocalDateTime endTime);
    
    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.createdAt < :cutoffDate")
    int deleteOldSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.isActive = false AND s.endedAt < :cutoffTime")
    int deleteInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Session update operations
    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.lastAccessedAt = :activityTime WHERE s.sessionToken = :sessionToken")
    int updateLastActivity(@Param("sessionToken") String sessionToken, @Param("activityTime") LocalDateTime activityTime);
    
    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.refreshToken = :newRefreshToken, s.refreshTokenExpiresAt = :newExpiresAt WHERE s.id = :sessionId")
    int updateRefreshToken(@Param("sessionId") UUID sessionId, @Param("newRefreshToken") String newRefreshToken, @Param("newExpiresAt") LocalDateTime newExpiresAt);

    // Advanced queries
    @Query("SELECT s FROM UserSessionEntity s WHERE " +
           "(:userId IS NULL OR s.user.id = :userId) AND " +
           "(:ipAddress IS NULL OR s.ipAddress = :ipAddress) AND " +
           "(:active IS NULL OR s.isActive = :active) AND " +
           "(:fromDate IS NULL OR s.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR s.createdAt <= :toDate)")
    Page<UserSessionEntity> findSessionsWithFilters(
        @Param("userId") UUID userId,
        @Param("ipAddress") String ipAddress,
        @Param("active") Boolean active,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );

    // Session validation
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM UserSessionEntity s WHERE s.sessionToken = :sessionToken AND s.isActive = true AND s.expiresAt > :currentTime")
    boolean isValidSession(@Param("sessionToken") String sessionToken, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM UserSessionEntity s WHERE s.refreshToken = :refreshToken AND s.isActive = true AND s.refreshTokenExpiresAt > :currentTime")
    boolean isValidRefreshToken(@Param("refreshToken") String refreshToken, @Param("currentTime") LocalDateTime currentTime);

    // Guest session specific queries
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.userType = 'GUEST'")
    List<UserSessionEntity> findGuestSessions();
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.userType = 'GUEST' AND s.createdAt < :cutoffDate")
    List<UserSessionEntity> findExpiredGuestSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.user.userType = 'GUEST' AND s.createdAt < :cutoffDate")
    int deleteExpiredGuestSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
}