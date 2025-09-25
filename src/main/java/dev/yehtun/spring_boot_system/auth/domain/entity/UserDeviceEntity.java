package dev.yehtun.spring_boot_system.auth.domain.entity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.yehtun.spring_boot_system.shared.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * User device entity for managing user devices and device fingerprinting.
 * 
 * Features:
 * - Device fingerprinting for security
 * - Device trust management
 * - Session tracking per device
 * - Device metadata collection
 */
@Entity
@Table(name = "user_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserDeviceEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "device_os", length = 50)
    private String deviceOs;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "device_fingerprint", length = 500, nullable = false, unique = true)
    private String deviceFingerprint;

    @Column(name = "is_trusted", nullable = false)
    @Builder.Default
    private Boolean isTrusted = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "location")
    private String location;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserSessionEntity> sessions = new ArrayList<>();

    // Business Logic Methods

    /**
     * Create a device fingerprint from device information
     */
    public static String createFingerprint(String userAgent, String deviceType, 
            String deviceOs, String browser, String ipAddress) {
        String input = String.join("|", 
            Objects.toString(userAgent, ""),
            Objects.toString(deviceType, ""),
            Objects.toString(deviceOs, ""),
            Objects.toString(browser, ""),
            Objects.toString(ipAddress, "")
        );
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf(input.hashCode());
        }
    }

    /**
     * Create a new device for a user
     */
    public static UserDeviceEntity createForUser(UserEntity user, String userAgent, 
            String deviceType, String deviceOs, String browser, String ipAddress) {
        String fingerprint = createFingerprint(userAgent, deviceType, deviceOs, browser, ipAddress);
        return UserDeviceEntity.builder()
                .user(user)
                .deviceFingerprint(fingerprint)
                .deviceType(deviceType)
                .deviceOs(deviceOs)
                .browser(browser)
                .ipAddress(ipAddress)
                .lastUsedAt(LocalDateTime.now())
                .isTrusted(false)
                .build();
    }

    /**
     * Update last used timestamp
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Mark device as trusted
     */
    public void markAsTrusted() {
        this.isTrusted = true;
    }

    /**
     * Mark device as untrusted
     */
    public void markAsUntrusted() {
        this.isTrusted = false;
    }

    /**
     * Activate device
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate device
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Check if device has been used recently
     */
    public boolean isRecentlyUsed(int days) {
        if (lastUsedAt == null) {
            return false;
        }
        return lastUsedAt.isAfter(LocalDateTime.now().minusDays(days));
    }

    /**
     * Get active sessions for this device
     */
    public List<UserSessionEntity> getActiveSessions() {
        return sessions.stream()
                .filter(UserSessionEntity::isValid)
                .toList();
    }

    /**
     * Get total session count for this device
     */
    public int getSessionCount() {
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Get active session count for this device
     */
    public int getActiveSessionCount() {
        return getActiveSessions().size();
    }

    /**
     * Check if this device matches the given fingerprint
     */
    public boolean matchesFingerprint(String fingerprint) {
        return this.deviceFingerprint.equals(fingerprint);
    }

    /**
     * Get a display name for this device
     */
    public String getDisplayName() {
        if (deviceName != null && !deviceName.trim().isEmpty()) {
            return deviceName;
        }
        
        StringBuilder displayName = new StringBuilder();
        if (browser != null) {
            displayName.append(browser);
        }
        if (deviceOs != null) {
            if (displayName.length() > 0) {
                displayName.append(" on ");
            }
            displayName.append(deviceOs);
        }
        if (deviceType != null) {
            if (displayName.length() > 0) {
                displayName.append(" (").append(deviceType).append(")");
            } else {
                displayName.append(deviceType);
            }
        }
        
        return displayName.length() > 0 ? displayName.toString() : "Unknown Device";
    }

    /**
     * Check if device information indicates a mobile device
     */
    public boolean isMobileDevice() {
        if (deviceType != null) {
            String type = deviceType.toLowerCase();
            return type.contains("mobile") || type.contains("phone") || type.contains("tablet");
        }
        return false;
    }
}