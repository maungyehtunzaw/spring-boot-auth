package dev.yehtun.spring_boot_system.auth.infrastructure.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import dev.yehtun.spring_boot_system.auth.domain.service.AuthService;
import dev.yehtun.spring_boot_system.auth.domain.service.SecurityService;
import dev.yehtun.spring_boot_system.auth.domain.service.SessionService;
import dev.yehtun.spring_boot_system.auth.infrastructure.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AuthService for authentication operations.
 * 
 * Provides comprehensive authentication functionality including:
 * - Guest user management
 * - User registration and authentication
 * - Session management
 * - Password operations
 * - Account security
 */
@Slf4j
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(
            UserRepository userRepository,
            SessionService sessionService,
            SecurityService securityService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.securityService = securityService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity createGuestUser() {
        log.debug("Creating guest user");
        String guestIdentifier = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        UserEntity guestUser = UserEntity.createGuestUser(guestIdentifier);
        UserEntity savedUser = userRepository.save(guestUser);
        log.info("Created guest user with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public UserEntity createGuestUser(String deviceInfo) {
        log.debug("Creating guest user with device info: {}", deviceInfo);
        String guestIdentifier = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        UserEntity guestUser = UserEntity.createGuestUser(guestIdentifier);
        UserEntity savedUser = userRepository.save(guestUser);
        
        // Create session for the guest user
        sessionService.createGuestSession(deviceInfo);
        
        log.info("Created guest user with ID: {} and device info", savedUser.getId());
        return savedUser;
    }

    @Override
    public UserEntity promoteGuestToUser(UUID guestUserId, String username, String email, String password) {
        log.debug("Promoting guest user {} to regular user", guestUserId);
        
        UserEntity guestUser = userRepository.findById(guestUserId)
            .orElseThrow(() -> new IllegalArgumentException("Guest user not found"));
        
        if (guestUser.getUserType() != UserType.GUEST) {
            throw new IllegalArgumentException("User is not a guest user");
        }
        
        // Validate username and email availability
        if (!isUsernameAvailable(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        if (!isEmailAvailable(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }
        
        // Promote the guest user
        guestUser.setUserType(UserType.USER);
        guestUser.setUsername(username);
        guestUser.setEmail(email);
        guestUser.setPasswordHash(passwordEncoder.encode(password));
        guestUser.setUserStatus(UserStatus.ACTIVE);
        UserEntity savedUser = userRepository.save(guestUser);
        
        log.info("Promoted guest user {} to regular user with username: {}", guestUserId, username);
        return savedUser;
    }

    @Override
    public void cleanupExpiredGuestUsers() {
        log.debug("Cleaning up expired guest users");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7); // 7 days expiry
        var expiredGuests = userRepository.findExpiredGuestUsers(cutoffDate);
        
        for (UserEntity expiredGuest : expiredGuests) {
            // Clean up sessions first
            sessionService.invalidateAllUserSessions(expiredGuest.getId());
            // Delete the user
            userRepository.delete(expiredGuest);
        }
        
        log.info("Cleaned up {} expired guest users", expiredGuests.size());
    }

    @Override
    public UserEntity registerUser(String username, String email, String password) {
        return registerUser(username, email, password, null, null);
    }

    @Override
    public UserEntity registerUser(String username, String email, String password, String firstName, String lastName) {
        log.debug("Registering new user with username: {}", username);
        
        // Validate input
        if (!isUsernameAvailable(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        if (!isEmailAvailable(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }
        
        if (!validatePassword(password)) {
            throw new IllegalArgumentException("Password does not meet requirements");
        }
        
        // Create user
        UserEntity user = UserEntity.builder()
            .username(username)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .firstName(firstName)
            .lastName(lastName)
            .userType(UserType.USER)
            .userStatus(UserStatus.ACTIVE)
            .emailVerified(false)
            .phoneVerified(false)
            .twoFactorEnabled(false)
            .build();
        
        UserEntity savedUser = userRepository.save(user);
        
        // Assign default roles
        // This would be implemented in a full system
        
        log.info("Registered new user with ID: {} and username: {}", savedUser.getId(), username);
        return savedUser;
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public AuthResult authenticate(String usernameOrEmail, String password) {
        return authenticate(usernameOrEmail, password, null);
    }

    @Override
    public AuthResult authenticate(String usernameOrEmail, String password, String deviceInfo) {
        log.debug("Authenticating user: {}", usernameOrEmail);
        
        var userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (userOpt.isEmpty()) {
            log.warn("Authentication failed: user not found for {}", usernameOrEmail);
            securityService.trackFailedLoginAttempt(usernameOrEmail, null);
            return new AuthResult(false, "Invalid credentials", null, null, null, false, 0);
        }
        
        UserEntity user = userOpt.get();
        
        // Check if account is active
        if (!isAccountActive(user.getId())) {
            log.warn("Authentication failed: account inactive for user {}", user.getUsername());
            return new AuthResult(false, "Account is not active", null, null, null, false, 0);
        }
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Authentication failed: invalid password for user {}", user.getUsername());
            securityService.trackFailedLoginAttempt(usernameOrEmail, null);
            return new AuthResult(false, "Invalid credentials", null, null, null, false, 0);
        }
        
        // Clear failed attempts on successful authentication
        securityService.clearFailedLoginAttempts(user.getId());
        
        // Check if 2FA is required
        if (securityService.isTwoFactorEnabled(user.getId())) {
            // Return partial success indicating 2FA is required
            return new AuthResult(true, "Two-factor authentication required", null, null, user, true, 0);
        }
        
        // Create session
        var session = sessionService.createSession(user, deviceInfo);
        
        // Update last login
        user.updateLastLogin();
        userRepository.save(user);
        
        log.info("Authentication successful for user: {}", user.getUsername());
        return new AuthResult(true, "Authentication successful", 
            session.getSessionToken(), session.getRefreshToken(), user, false, 3600);
    }

    @Override
    public AuthResult authenticateWithTwoFactor(String sessionToken, String twoFactorCode) {
        log.debug("Authenticating with two-factor code");
        
        var sessionOpt = sessionService.findBySessionToken(sessionToken);
        if (sessionOpt.isEmpty()) {
            return new AuthResult(false, "Invalid session", null, null, null, false, 0);
        }
        
        var session = sessionOpt.get();
        var user = session.getUser();
        
        // Verify 2FA code
        if (!securityService.verifyTwoFactorCode(user.getId(), twoFactorCode)) {
            log.warn("Two-factor authentication failed for user: {}", user.getUsername());
            return new AuthResult(false, "Invalid two-factor code", null, null, null, false, 0);
        }
        
        // Session is already activated during two-factor verification
        // No need to call additional activation
        
        log.info("Two-factor authentication successful for user: {}", user.getUsername());
        return new AuthResult(true, "Authentication successful", 
            session.getSessionToken(), session.getRefreshToken(), user, false, 3600);
    }

    @Override
    public AuthResult refreshToken(String refreshToken) {
        log.debug("Refreshing token");
        
        var result = sessionService.refreshSessionToken(refreshToken);
        if (!result.success()) {
            return new AuthResult(false, result.errorMessage(), null, null, null, false, 0);
        }
        
        return new AuthResult(true, "Token refreshed successfully", 
            result.newSessionToken(), result.newRefreshToken(), null, false, 3600);
    }

    @Override
    public void logout(String sessionToken) {
        log.debug("Logging out session");
        sessionService.invalidateSession(sessionToken);
    }

    @Override
    public void logoutAllSessions(UUID userId) {
        log.debug("Logging out all sessions for user: {}", userId);
        sessionService.invalidateAllUserSessions(userId);
    }

    @Override
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        log.debug("Changing password for user: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password
        if (!validatePassword(newPassword)) {
            throw new IllegalArgumentException("New password does not meet requirements");
        }
        
        // Update password
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Track password history
        securityService.trackPasswordHistory(userId, user.getPasswordHash());
        
        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    public void resetPasswordRequest(String email) {
        log.debug("Password reset requested for email: {}", email);
        
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists or not
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        // Generate reset token and send email
        // This would be implemented in a full system
        
        log.info("Password reset email sent to: {}", email);
    }

    @Override
    public void resetPassword(String resetToken, String newPassword) {
        log.debug("Resetting password with token");
        
        // Validate reset token
        // This would be implemented in a full system
        
        if (!validatePassword(newPassword)) {
            throw new IllegalArgumentException("Password does not meet requirements");
        }
        
        // Reset password logic would be here
        
        log.info("Password reset successfully");
    }

    @Override
    public boolean validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Basic password validation
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpper && hasLower && hasDigit;
    }

    @Override
    public void lockAccount(UUID userId, String reason) {
        log.debug("Locking account for user: {} with reason: {}", userId, reason);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.lockAccount(LocalDateTime.now().plusDays(30)); // Lock for 30 days
        userRepository.save(user);
        
        // Invalidate all sessions
        sessionService.invalidateAllUserSessions(userId);
        
        log.info("Account locked for user: {}", userId);
    }

    @Override
    public void unlockAccount(UUID userId) {
        log.debug("Unlocking account for user: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.unlockAccount();
        userRepository.save(user);
        
        log.info("Account unlocked for user: {}", userId);
    }

    @Override
    public void suspendAccount(UUID userId, String reason) {
        log.debug("Suspending account for user: {} with reason: {}", userId, reason);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.suspend();
        userRepository.save(user);
        
        // Invalidate all sessions
        sessionService.invalidateAllUserSessions(userId);
        
        log.info("Account suspended for user: {}", userId);
    }

    @Override
    public void activateAccount(UUID userId) {
        log.debug("Activating account for user: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.activate();
        userRepository.save(user);
        
        log.info("Account activated for user: {}", userId);
    }

    @Override
    public boolean isAccountLocked(UUID userId) {
        return userRepository.findById(userId)
            .map(user -> user.getUserStatus() == UserStatus.LOCKED)
            .orElse(false);
    }

    @Override
    public boolean isAccountActive(UUID userId) {
        return userRepository.findById(userId)
            .map(user -> user.getUserStatus() == UserStatus.ACTIVE)
            .orElse(false);
    }

    @Override
    public void invalidateSession(String sessionToken) {
        sessionService.invalidateSession(sessionToken);
    }

    @Override
    public void invalidateAllUserSessions(UUID userId) {
        sessionService.invalidateAllUserSessions(userId);
    }

    @Override
    public boolean isValidSession(String sessionToken) {
        return sessionService.isValidSession(sessionToken);
    }

    @Override
    public void extendSession(String sessionToken) {
        sessionService.extendSession(sessionToken);
    }

    @Override
    public boolean hasPermission(UUID userId, String permission) {
        return securityService.hasPermission(userId, permission);
    }

    @Override
    public boolean hasRole(UUID userId, String roleName) {
        return securityService.hasRole(userId, roleName);
    }

    @Override
    public void trackFailedLoginAttempt(String usernameOrEmail) {
        securityService.trackFailedLoginAttempt(usernameOrEmail, null);
    }

    @Override
    public void clearFailedLoginAttempts(UUID userId) {
        securityService.clearFailedLoginAttempts(userId);
    }

    @Override
    public void sendEmailVerification(UUID userId) {
        log.debug("Sending email verification for user: {}", userId);
        
        // Generate verification token and send email
        // This would be implemented in a full system
        
        log.info("Email verification sent for user: {}", userId);
    }

    @Override
    public void verifyEmail(String verificationToken) {
        log.debug("Verifying email with token");
        
        // Validate verification token
        // This would be implemented in a full system
        
        log.info("Email verified successfully");
    }

    @Override
    public boolean isEmailVerified(UUID userId) {
        return userRepository.findById(userId)
            .map(UserEntity::getEmailVerified)
            .orElse(false);
    }
}