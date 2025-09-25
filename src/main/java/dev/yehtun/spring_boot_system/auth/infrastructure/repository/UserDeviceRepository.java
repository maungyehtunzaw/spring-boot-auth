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

import dev.yehtun.spring_boot_system.auth.domain.entity.UserDeviceEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;

/**
 * Repository interface for UserDeviceEntity operations.
 * 
 * Features:
 * - Device registration and tracking
 * - Trusted device management
 * - Device security and validation
 * - Device cleanup and maintenance
 */
@Repository
public interface UserDeviceRepository extends JpaRepository<UserDeviceEntity, UUID> {

    // Basic device queries
    Optional<UserDeviceEntity> findByDeviceId(String deviceId);
    
    Optional<UserDeviceEntity> findByFingerprint(String fingerprint);
    
    List<UserDeviceEntity> findByUser(UserEntity user);
    
    List<UserDeviceEntity> findByUserId(UUID userId);

    // Device trust and status queries
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user = :user AND d.trusted = true")
    List<UserDeviceEntity> findTrustedDevicesByUser(@Param("user") UserEntity user);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.trusted = true")
    List<UserDeviceEntity> findTrustedDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.active = true")
    List<UserDeviceEntity> findActiveDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.trusted = true AND d.active = true")
    List<UserDeviceEntity> findAllTrustedActiveDevices();

    // Device identification queries
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.deviceId = :deviceId")
    Optional<UserDeviceEntity> findByUserIdAndDeviceId(@Param("userId") UUID userId, @Param("deviceId") String deviceId);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.fingerprint = :fingerprint")
    Optional<UserDeviceEntity> findByUserIdAndFingerprint(@Param("userId") UUID userId, @Param("fingerprint") String fingerprint);
    
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
           "FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.deviceId = :deviceId")
    boolean existsByUserIdAndDeviceId(@Param("userId") UUID userId, @Param("deviceId") String deviceId);

    // Device activity tracking
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.lastUsedAt < :cutoffTime")
    List<UserDeviceEntity> findInactiveDevices(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.lastUsedAt < :cutoffTime")
    List<UserDeviceEntity> findInactiveDevicesByUserId(@Param("userId") UUID userId, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.lastUsedAt >= :fromTime")
    List<UserDeviceEntity> findRecentlyUsedDevices(@Param("fromTime") LocalDateTime fromTime);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.lastUsedAt >= :fromTime")
    List<UserDeviceEntity> findRecentlyUsedDevicesByUserId(@Param("userId") UUID userId, @Param("fromTime") LocalDateTime fromTime);

    // Device type and platform queries
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.deviceType = :deviceType")
    List<UserDeviceEntity> findByDeviceType(@Param("deviceType") String deviceType);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.platform = :platform")
    List<UserDeviceEntity> findByPlatform(@Param("platform") String platform);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.deviceType = :deviceType")
    List<UserDeviceEntity> findByUserIdAndDeviceType(@Param("userId") UUID userId, @Param("deviceType") String deviceType);
    
    @Query("SELECT DISTINCT d.deviceType FROM UserDeviceEntity d WHERE d.user.id = :userId")
    List<String> findDeviceTypesByUserId(@Param("userId") UUID userId);

    // Location and IP tracking
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.lastIpAddress = :ipAddress")
    List<UserDeviceEntity> findByLastIpAddress(@Param("ipAddress") String ipAddress);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.location = :location")
    List<UserDeviceEntity> findByLocation(@Param("location") String location);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.lastIpAddress = :ipAddress")
    List<UserDeviceEntity> findByUserIdAndIpAddress(@Param("userId") UUID userId, @Param("ipAddress") String ipAddress);

    // Security and validation queries
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.registeredAt < :cutoffDate AND d.trusted = false")
    List<UserDeviceEntity> findUnverifiedOldDevices(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT d FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.trusted = false")
    List<UserDeviceEntity> findUntrustedDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(d) FROM UserDeviceEntity d WHERE d.lastIpAddress = :ipAddress AND d.registeredAt >= :fromTime")
    long countDevicesRegisteredFromIpSince(@Param("ipAddress") String ipAddress, @Param("fromTime") LocalDateTime fromTime);

    // Device statistics
    @Query("SELECT COUNT(d) FROM UserDeviceEntity d WHERE d.user.id = :userId")
    long countDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(d) FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.trusted = true")
    long countTrustedDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(d) FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.active = true")
    long countActiveDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(d) FROM UserDeviceEntity d WHERE d.registeredAt >= :fromDate")
    long countDevicesRegisteredSince(@Param("fromDate") LocalDateTime fromDate);

    // Device management operations
    @Modifying
    @Query("UPDATE UserDeviceEntity d SET d.trusted = :trusted WHERE d.id = :deviceId")
    int updateTrustedStatus(@Param("deviceId") UUID deviceId, @Param("trusted") boolean trusted);
    
    @Modifying
    @Query("UPDATE UserDeviceEntity d SET d.active = :active WHERE d.id = :deviceId")
    int updateActiveStatus(@Param("deviceId") UUID deviceId, @Param("active") boolean active);
    
    @Modifying
    @Query("UPDATE UserDeviceEntity d SET d.lastUsedAt = :lastUsedAt, d.lastIpAddress = :ipAddress WHERE d.id = :deviceId")
    int updateLastUsed(@Param("deviceId") UUID deviceId, @Param("lastUsedAt") LocalDateTime lastUsedAt, @Param("ipAddress") String ipAddress);
    
    @Modifying
    @Query("UPDATE UserDeviceEntity d SET d.active = false WHERE d.user.id = :userId")
    int deactivateAllUserDevices(@Param("userId") UUID userId);

    // Device cleanup operations
    @Modifying
    @Query("DELETE FROM UserDeviceEntity d WHERE d.registeredAt < :cutoffDate AND d.trusted = false")
    int deleteUnverifiedOldDevices(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM UserDeviceEntity d WHERE d.lastUsedAt < :cutoffDate AND d.active = false")
    int deleteInactiveOldDevices(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM UserDeviceEntity d WHERE d.user.userType = 'GUEST' AND d.registeredAt < :cutoffDate")
    int deleteGuestDevices(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Advanced filtering
    @Query("SELECT d FROM UserDeviceEntity d WHERE " +
           "(:userId IS NULL OR d.user.id = :userId) AND " +
           "(:deviceType IS NULL OR d.deviceType = :deviceType) AND " +
           "(:platform IS NULL OR d.platform = :platform) AND " +
           "(:trusted IS NULL OR d.trusted = :trusted) AND " +
           "(:active IS NULL OR d.active = :active) AND " +
           "(:fromDate IS NULL OR d.registeredAt >= :fromDate) AND " +
           "(:toDate IS NULL OR d.registeredAt <= :toDate)")
    Page<UserDeviceEntity> findDevicesWithFilters(
        @Param("userId") UUID userId,
        @Param("deviceType") String deviceType,
        @Param("platform") String platform,
        @Param("trusted") Boolean trusted,
        @Param("active") Boolean active,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );

    // Device validation
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
           "FROM UserDeviceEntity d WHERE d.user.id = :userId AND d.deviceId = :deviceId AND d.trusted = true AND d.active = true")
    boolean isTrustedActiveDevice(@Param("userId") UUID userId, @Param("deviceId") String deviceId);
    
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
           "FROM UserDeviceEntity d WHERE d.fingerprint = :fingerprint AND d.trusted = true")
    boolean isTrustedDeviceByFingerprint(@Param("fingerprint") String fingerprint);

    // Device search
    @Query("SELECT d FROM UserDeviceEntity d WHERE " +
           "LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.deviceType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.platform) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<UserDeviceEntity> searchDevices(@Param("searchTerm") String searchTerm, Pageable pageable);
}