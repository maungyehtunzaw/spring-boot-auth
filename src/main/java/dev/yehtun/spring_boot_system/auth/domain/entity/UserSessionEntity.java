package dev.yehtun.spring_boot_system.auth.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.yehtun.spring_boot_system.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * User session entity for managing user sessions and JWT tokens.
 * 
 * Features:
 * - JWT token management
 * - Session expiration tracking
 * - Device association
 * - IP and user agent tracking
 * - Session lifecycle management
 */
@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserSessionEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "session_token", length = 500, nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "refresh_token", length = 500, nullable = false, unique = true)
    private String refreshToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private UserDeviceEntity device;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // Business Logic Methods

    /**
     * Create a new session for a user
     */
    public static UserSessionEntity createForUser(UserEntity user, UserDeviceEntity device, 
            String ipAddress, String userAgent, int sessionHours) {
        return UserSessionEntity.builder()
                .user(user)
                .device(device)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .sessionToken(generateToken())
                .refreshToken(generateToken())
                .expiresAt(LocalDateTime.now().plusHours(sessionHours))
                .refreshTokenExpiresAt(LocalDateTime.now().plusDays(7)) // Default 7 days for refresh token
                .lastAccessedAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    /**
     * Generate a secure token
     */
    private static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Check if the session is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the session is valid (active and not expired)
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }

    /**
     * Update last accessed time
     */
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * Refresh the session (extend expiration)
     */
    public void refresh(int sessionHours) {
        this.expiresAt = LocalDateTime.now().plusHours(sessionHours);
        this.refreshToken = generateToken();
        this.refreshTokenExpiresAt = LocalDateTime.now().plusDays(7); // Extend refresh token
        updateLastAccessed();
    }

    /**
     * Invalidate this session
     */
    public void invalidate() {
        this.isActive = false;
    }

    /**
     * Check if session matches the provided tokens
     */
    public boolean matchesTokens(String sessionToken, String refreshToken) {
        return this.sessionToken.equals(sessionToken) && 
               this.refreshToken.equals(refreshToken);
    }

    /**
     * Get session duration in minutes since creation
     */
    public long getSessionDurationMinutes() {
        if (lastAccessedAt != null) {
            // Use the entity creation time by approximating from ID timestamp or use current approach
            LocalDateTime sessionStart = expiresAt.minusHours(24); // Approximate session start
            return java.time.Duration.between(sessionStart, lastAccessedAt).toMinutes();
        }
        return 0;
    }

    /**
     * Get remaining session time in minutes
     */
    public long getRemainingMinutes() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
    }

    /**
     * Check if the session is from the same device
     */
    public boolean isFromSameDevice(UserDeviceEntity otherDevice) {
        return device != null && device.equals(otherDevice);
    }

    /**
     * Check if the session is from the same IP
     */
    public boolean isFromSameIP(String otherIP) {
        return ipAddress != null && ipAddress.equals(otherIP);
    }

    // Convenience methods for compatibility with different naming conventions
    
    /**
     * Alias for isActive for compatibility with repository queries
     */
    public Boolean getActive() {
        return isActive;
    }
    
    /**
     * Alias for setIsActive for compatibility
     */
    public void setActive(Boolean active) {
        this.isActive = active;
    }

    /**
     * Alias for lastAccessedAt for compatibility
     */
    public LocalDateTime getLastActivityAt() {
        return lastAccessedAt;
    }

    /**
     * Alias for setLastAccessedAt for compatibility
     */
    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastAccessedAt = lastActivityAt;
    }
}