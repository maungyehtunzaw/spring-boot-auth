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

import dev.yehtun.spring_boot_system.auth.domain.entity.TwoFactorAuthEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;

/**
 * Repository interface for TwoFactorAuthEntity operations.
 * 
 * Features:
 * - 2FA token management and validation
 * - Backup code management
 * - 2FA method configuration
 * - Security and cleanup operations
 */
@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuthEntity, UUID> {

    // Basic 2FA queries
    Optional<TwoFactorAuthEntity> findByUser(UserEntity user);
    
    Optional<TwoFactorAuthEntity> findByUserId(UUID userId);
    
    List<TwoFactorAuthEntity> findByEnabledTrue();
    
    List<TwoFactorAuthEntity> findByEnabledFalse();

    // Method-specific queries
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.method = :method")
    List<TwoFactorAuthEntity> findByMethod(@Param("method") String method);
    
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.method = :method")
    Optional<TwoFactorAuthEntity> findByUserIdAndMethod(@Param("userId") UUID userId, @Param("method") String method);
    
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.enabled = true AND tfa.method = :method")
    List<TwoFactorAuthEntity> findEnabledByMethod(@Param("method") String method);

    // Secret and backup code queries
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.secretKey = :secretKey")
    Optional<TwoFactorAuthEntity> findBySecretKey(@Param("secretKey") String secretKey);
    
    @Query("SELECT CASE WHEN COUNT(tfa) > 0 THEN true ELSE false END " +
           "FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.backupCodes LIKE CONCAT('%', :backupCode, '%')")
    boolean hasValidBackupCode(@Param("userId") UUID userId, @Param("backupCode") String backupCode);

    // Verification and validation queries
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.enabled = true")
    List<TwoFactorAuthEntity> findEnabledByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT CASE WHEN COUNT(tfa) > 0 THEN true ELSE false END " +
           "FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.enabled = true")
    boolean hasTwoFactorEnabled(@Param("userId") UUID userId);
    
    @Query("SELECT CASE WHEN COUNT(tfa) > 0 THEN true ELSE false END " +
           "FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.enabled = true AND tfa.verified = true")
    boolean hasTwoFactorVerified(@Param("userId") UUID userId);

    // Time-based queries
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.lastVerifiedAt < :cutoffTime")
    List<TwoFactorAuthEntity> findUnusedSince(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.lastVerifiedAt >= :fromTime")
    List<TwoFactorAuthEntity> findRecentlyUsed(@Param("fromTime") LocalDateTime fromTime);
    
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.createdAt < :cutoffDate AND tfa.verified = false")
    List<TwoFactorAuthEntity> findUnverifiedOld(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Security and monitoring queries
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.failedAttempts >= :maxAttempts")
    List<TwoFactorAuthEntity> findWithExcessiveFailures(@Param("maxAttempts") int maxAttempts);
    
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.failedAttempts >= :maxAttempts")
    Optional<TwoFactorAuthEntity> findWithExcessiveFailuresByUserId(@Param("userId") UUID userId, @Param("maxAttempts") int maxAttempts);
    
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.lastFailedAttemptAt >= :fromTime")
    List<TwoFactorAuthEntity> findWithRecentFailures(@Param("fromTime") LocalDateTime fromTime);

    // Backup code management
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE (tfa.backupCodes IS NULL OR tfa.backupCodes = '')")
    List<TwoFactorAuthEntity> findWithInsufficientBackupCodes(@Param("minBackupCodes") int minBackupCodes);
    
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.backupCodes IS NOT NULL AND tfa.backupCodes != ''")
    Optional<TwoFactorAuthEntity> findWithBackupCodesByUserId(@Param("userId") UUID userId);

    // Statistics and counts
    @Query("SELECT COUNT(tfa) FROM TwoFactorAuthEntity tfa WHERE tfa.enabled = true")
    long countEnabledTwoFactor();
    
    @Query("SELECT COUNT(tfa) FROM TwoFactorAuthEntity tfa WHERE tfa.verified = true")
    long countVerifiedTwoFactor();
    
    @Query("SELECT COUNT(tfa) FROM TwoFactorAuthEntity tfa WHERE tfa.method = :method")
    long countByMethod(@Param("method") String method);
    
    @Query("SELECT COUNT(tfa) FROM TwoFactorAuthEntity tfa WHERE tfa.lastVerifiedAt >= :fromDate")
    long countUsedSince(@Param("fromDate") LocalDateTime fromDate);

    // Update operations
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.enabled = :enabled WHERE tfa.user.id = :userId")
    int updateEnabledStatus(@Param("userId") UUID userId, @Param("enabled") boolean enabled);
    
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.verified = :verified, tfa.verifiedAt = :verifiedAt WHERE tfa.user.id = :userId")
    int updateVerificationStatus(@Param("userId") UUID userId, @Param("verified") boolean verified, @Param("verifiedAt") LocalDateTime verifiedAt);
    
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.lastVerifiedAt = :lastVerifiedAt WHERE tfa.user.id = :userId")
    int updateLastVerified(@Param("userId") UUID userId, @Param("lastVerifiedAt") LocalDateTime lastVerifiedAt);
    
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.failedAttempts = :failedAttempts, tfa.lastFailedAttemptAt = :lastFailedAttemptAt WHERE tfa.user.id = :userId")
    int updateFailedAttempts(@Param("userId") UUID userId, @Param("failedAttempts") int failedAttempts, @Param("lastFailedAttemptAt") LocalDateTime lastFailedAttemptAt);
    
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.failedAttempts = 0, tfa.lastFailedAttemptAt = NULL WHERE tfa.user.id = :userId")
    int resetFailedAttempts(@Param("userId") UUID userId);

    // Secret key management
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.secretKey = :newSecretKey WHERE tfa.user.id = :userId")
    int updateSecretKey(@Param("userId") UUID userId, @Param("newSecretKey") String newSecretKey);
    
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.method = :method WHERE tfa.user.id = :userId")
    int updateMethod(@Param("userId") UUID userId, @Param("method") String method);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM TwoFactorAuthEntity tfa WHERE tfa.createdAt < :cutoffDate AND tfa.verified = false")
    int deleteUnverifiedOld(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM TwoFactorAuthEntity tfa WHERE tfa.user.userType = 'GUEST'")
    int deleteGuestTwoFactorAuth();
    
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.backupCodes = NULL WHERE tfa.user.id = :userId")
    int clearBackupCodes(@Param("userId") UUID userId);

    // Advanced filtering
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE " +
           "(:enabled IS NULL OR tfa.enabled = :enabled) AND " +
           "(:verified IS NULL OR tfa.verified = :verified) AND " +
           "(:method IS NULL OR tfa.method = :method) AND " +
           "(:fromDate IS NULL OR tfa.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR tfa.createdAt <= :toDate)")
    Page<TwoFactorAuthEntity> findTwoFactorWithFilters(
        @Param("enabled") Boolean enabled,
        @Param("verified") Boolean verified,
        @Param("method") String method,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );

    // Validation helpers
    @Query("SELECT CASE WHEN COUNT(tfa) > 0 THEN true ELSE false END " +
           "FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.enabled = true AND tfa.method = :method")
    boolean isMethodEnabledForUser(@Param("userId") UUID userId, @Param("method") String method);
    
    @Query("SELECT CASE WHEN tfa.failedAttempts < :maxAttempts THEN true ELSE false END " +
           "FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId")
    Boolean canAttemptVerification(@Param("userId") UUID userId, @Param("maxAttempts") int maxAttempts);

    // Recovery operations
    @Query("SELECT tfa FROM TwoFactorAuthEntity tfa WHERE tfa.user.id = :userId AND tfa.backupCodes IS NOT NULL AND tfa.backupCodes != ''")
    Optional<TwoFactorAuthEntity> findForRecovery(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE TwoFactorAuthEntity tfa SET tfa.enabled = false, tfa.verified = false WHERE tfa.user.id = :userId")
    int disableTwoFactorForUser(@Param("userId") UUID userId);
}