package dev.yehtun.spring_boot_system.auth.domain.entity;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import dev.yehtun.spring_boot_system.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Two-factor authentication entity for TOTP and backup codes.
 * 
 * Features:
 * - TOTP (Time-based One-Time Password) support
 * - Backup codes generation and validation
 * - 2FA enablement/disablement
 * - Usage tracking and security
 */
@Entity
@Table(name = "two_factor_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwoFactorAuthEntity extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "secret_key", nullable = false, length = 500)
    private String secretKey;

    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "backup_codes_used_count", nullable = false)
    @Builder.Default
    private Integer backupCodesUsedCount = 0;

    @Column(name = "total_usage_count", nullable = false)
    @Builder.Default
    private Integer totalUsageCount = 0;

    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;

    @Column(name = "last_failed_attempt_at")
    private LocalDateTime lastFailedAttemptAt;

    @Column(name = "method", nullable = false, length = 20)
    private String method = "TOTP"; // Default to TOTP

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    // Constructors
    
    // Business Logic Methods

    /**
     * Generate a new TOTP secret key
     */
    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Generate backup codes
     */
    public static List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < count; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
            codes.add(code.toString());
        }
        return codes;
    }

    /**
     * Create 2FA setup for a user
     */
    public static TwoFactorAuthEntity createForUser(UserEntity user) {
        return TwoFactorAuthEntity.builder()
                .user(user)
                .secretKey(generateSecretKey())
                .backupCodes(String.join(",", generateBackupCodes(10)))
                .enabled(false)
                .build();
    }

    /**
     * Enable 2FA for the user
     */
    public void enable() {
        this.enabled = true;
        this.enabledAt = LocalDateTime.now();
        updateUsage();
    }

    /**
     * Disable 2FA for the user
     */
    public void disable() {
        this.enabled = false;
        this.enabledAt = null;
    }

    /**
     * Update usage statistics
     */
    public void updateUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.totalUsageCount++;
    }

    /**
     * Get backup codes as a list
     */
    public List<String> getBackupCodesList() {
        if (backupCodes == null || backupCodes.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(backupCodes.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Set backup codes from a list
     */
    public void setBackupCodesList(List<String> codes) {
        this.backupCodes = String.join(",", codes);
    }

    /**
     * Use a backup code (removes it from available codes)
     */
    public boolean useBackupCode(String code) {
        List<String> codes = getBackupCodesList();
        if (codes.remove(code.trim().toUpperCase())) {
            setBackupCodesList(codes);
            this.backupCodesUsedCount++;
            updateUsage();
            return true;
        }
        return false;
    }

    /**
     * Check if a backup code is valid
     */
    public boolean isValidBackupCode(String code) {
        return getBackupCodesList().contains(code.trim().toUpperCase());
    }

    /**
     * Get remaining backup codes count
     */
    public int getRemainingBackupCodesCount() {
        return getBackupCodesList().size();
    }

    /**
     * Regenerate backup codes
     */
    public List<String> regenerateBackupCodes() {
        List<String> newCodes = generateBackupCodes(10);
        setBackupCodesList(newCodes);
        this.backupCodesUsedCount = 0;
        return newCodes;
    }

    /**
     * Check if 2FA needs attention (low backup codes)
     */
    public boolean needsAttention() {
        return enabled && getRemainingBackupCodesCount() <= 2;
    }

    /**
     * Get 2FA status description
     */
    public String getStatusDescription() {
        if (!enabled) {
            return "Disabled";
        }
        
        int remainingCodes = getRemainingBackupCodesCount();
        if (remainingCodes <= 2) {
            return "Enabled (Low backup codes: " + remainingCodes + ")";
        }
        
        return "Enabled (" + remainingCodes + " backup codes remaining)";
    }

    /**
     * Check if 2FA has been used recently
     */
    public boolean isRecentlyUsed(int hours) {
        if (lastUsedAt == null) {
            return false;
        }
        return lastUsedAt.isAfter(LocalDateTime.now().minusHours(hours));
    }

    /**
     * Get the Google Authenticator compatible URI for QR code generation
     */
    public String getAuthenticatorUri(String issuer, String accountName) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s",
            issuer,
            accountName,
            secretKey.replaceAll("\\s", ""),
            issuer
        );
    }
}