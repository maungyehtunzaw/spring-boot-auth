# Auth Module Architecture & Implementation Guide

## Overview
This document outlines the complete implementation strategy for a production-ready authentication module using Spring Boot 3.5.6, Spring Modulith, and modern security practices.

## Module Architecture

### Core Components
```
auth/
├── api/                    # REST API layer
│   ├── controllers/        # REST endpoints
│   ├── dto/               # Request/Response DTOs
│   └── validation/        # Custom validators
├── domain/                # Business logic layer
│   ├── entities/          # Domain entities
│   ├── services/          # Business services
│   ├── events/            # Domain events
│   └── exceptions/        # Domain exceptions
├── infrastructure/        # Data & external access
│   ├── repositories/      # JPA repositories
│   ├── config/           # Module configuration
│   └── adapters/         # External service adapters
└── security/             # Security configuration
    ├── filters/          # Custom security filters
    ├── providers/        # Authentication providers
    └── handlers/         # Security handlers
```

## Entity Design

### Core Entities

#### UserEntity
```java
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private UserType userType; // ADMIN, CLIENT, SYSTEM
    
    @Enumerated(EnumType.STRING)
    private UserStatus status; // ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
    
    private boolean emailVerified = false;
    private boolean twoFactorEnabled = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt; // Soft delete
    
    // Relationships
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles")
    private Set<RoleEntity> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserSessionEntity> sessions = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserDeviceEntity> devices = new HashSet<>();
}
```

#### RoleEntity
```java
@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String name; // ROLE_ADMIN, ROLE_USER, ROLE_CLIENT_ADMIN
    
    private String description;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permissions")
    private Set<PermissionEntity> permissions = new HashSet<>();
}
```

#### PermissionEntity
```java
@Entity
@Table(name = "permissions")
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String name; // READ_USERS, WRITE_USERS, DELETE_USERS
    
    private String description;
    private String resource; // users, orders, reports
    private String action;   // read, write, delete, admin
}
```

#### UserSessionEntity
```java
@Entity
@Table(name = "user_sessions")
public class UserSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @Column(unique = true, nullable = false)
    private String sessionToken;
    
    @Column(unique = true, nullable = false)
    private String refreshToken;
    
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private UserDeviceEntity device;
    
    private String ipAddress;
    private String userAgent;
    private boolean active = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    private LocalDateTime lastAccessedAt;
}
```

#### UserDeviceEntity
```java
@Entity
@Table(name = "user_devices")
public class UserDeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    private String deviceId; // Client-generated unique identifier
    private String deviceName; // "John's iPhone", "Office Laptop"
    private String deviceType; // MOBILE, DESKTOP, TABLET, WEB
    private String operatingSystem;
    private String browser;
    
    private boolean trusted = false;
    private LocalDateTime lastSeenAt;
    
    @CreatedDate
    private LocalDateTime createdAt;
}
```

#### TwoFactorAuthEntity
```java
@Entity
@Table(name = "two_factor_auth")
public class TwoFactorAuthEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    private String secretKey; // Encrypted TOTP secret
    private String backupCodes; // Encrypted JSON array of backup codes
    private boolean enabled = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    private LocalDateTime enabledAt;
}
```

#### PasswordResetTokenEntity
```java
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    private LocalDateTime expiresAt;
    private boolean used = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
}
```

## API Endpoints

### Authentication Endpoints
```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    // User Registration
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request);
    
    // User Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);
    
    // Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request);
    
    // Logout
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request);
    
    // Logout All Sessions
    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logoutAll();
    
    // Email Verification
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailVerificationRequest request);
    
    // Resend Email Verification
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request);
}
```

### Password Management Endpoints
```java
@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordController {
    
    // Forgot Password
    @PostMapping("/forgot")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);
    
    // Reset Password
    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request);
    
    // Change Password (Authenticated)
    @PostMapping("/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request);
}
```

### Two-Factor Authentication Endpoints
```java
@RestController
@RequestMapping("/api/v1/auth/2fa")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TwoFactorAuthController {
    
    // Setup 2FA
    @PostMapping("/setup")
    public ResponseEntity<TwoFactorSetupResponse> setup2FA();
    
    // Enable 2FA
    @PostMapping("/enable")
    public ResponseEntity<TwoFactorBackupCodesResponse> enable2FA(@Valid @RequestBody Enable2FARequest request);
    
    // Disable 2FA
    @PostMapping("/disable")
    public ResponseEntity<Void> disable2FA(@Valid @RequestBody Disable2FARequest request);
    
    // Verify 2FA Code
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify2FA(@Valid @RequestBody Verify2FARequest request);
    
    // Generate New Backup Codes
    @PostMapping("/backup-codes/regenerate")
    public ResponseEntity<TwoFactorBackupCodesResponse> regenerateBackupCodes();
}
```

### Session Management Endpoints
```java
@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SessionController {
    
    // Get Active Sessions
    @GetMapping
    public ResponseEntity<List<UserSessionResponse>> getActiveSessions();
    
    // Revoke Session
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> revokeSession(@PathVariable UUID sessionId);
    
    // Revoke All Other Sessions
    @DeleteMapping("/others")
    public ResponseEntity<Void> revokeOtherSessions();
}
```

### Device Management Endpoints
```java
@RestController
@RequestMapping("/api/v1/auth/devices")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeviceController {
    
    // Get User Devices
    @GetMapping
    public ResponseEntity<List<UserDeviceResponse>> getDevices();
    
    // Trust Device
    @PostMapping("/{deviceId}/trust")
    public ResponseEntity<Void> trustDevice(@PathVariable UUID deviceId);
    
    // Remove Device
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> removeDevice(@PathVariable UUID deviceId);
    
    // Rename Device
    @PutMapping("/{deviceId}/name")
    public ResponseEntity<Void> renameDevice(
        @PathVariable UUID deviceId, 
        @Valid @RequestBody RenameDeviceRequest request
    );
}
```

## Security Configuration

### JWT Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", 
                               "/api/v1/auth/refresh", "/api/v1/auth/forgot-password",
                               "/api/v1/auth/reset-password", "/api/v1/auth/verify-email").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            .build();
    }
}
```

## Service Layer Architecture

### AuthService Interface
```java
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String sessionToken);
    void logoutAll(UUID userId);
    void verifyEmail(String token);
    void resendEmailVerification(String email);
}
```

### Password Service Interface
```java
public interface PasswordService {
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    void changePassword(UUID userId, String currentPassword, String newPassword);
}
```

### TwoFactorAuthService Interface
```java
public interface TwoFactorAuthService {
    TwoFactorSetupResponse setup2FA(UUID userId);
    TwoFactorBackupCodesResponse enable2FA(UUID userId, String totpCode);
    void disable2FA(UUID userId, String password);
    AuthResponse verify2FA(String sessionToken, String code);
    TwoFactorBackupCodesResponse regenerateBackupCodes(UUID userId);
}
```

## Database Schema Migration

### Initial Migration (001-initial-auth-schema.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">
    
    <changeSet id="001-create-users-table" author="system">
        <createTable tableName="users">
            <column name="id" type="BINARY(16)">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="email" type="VARCHAR(255)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="username" type="VARCHAR(50)">
                <constraints unique="true"/>
            </column>
            <column name="password_hash" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="user_type" type="VARCHAR(20)" defaultValue="CLIENT">
                <constraints nullable="false"/>
            </column>
            <column name="status" type="VARCHAR(20)" defaultValue="PENDING_VERIFICATION">
                <constraints nullable="false"/>
            </column>
            <column name="email_verified" type="BOOLEAN" defaultValue="false">
                <constraints nullable="false"/>
            </column>
            <column name="two_factor_enabled" type="BOOLEAN" defaultValue="false">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="deleted_at" type="TIMESTAMP"/>
        </createTable>
    </changeSet>
    
    <!-- Additional tables for roles, permissions, sessions, devices, etc. -->
    
</databaseChangeLog>
```

## Environment Configuration

### application.yml
```yaml
spring:
  application:
    name: spring-boot-system
  
  datasource:
    url: jdbc:mysql://localhost:3306/spring_boot_system
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect
  
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
  
  security:
    jwt:
      secret: ${JWT_SECRET:your-secret-key}
      expiration: 86400000 # 24 hours
      refresh-expiration: 604800000 # 7 days

# Application specific settings
app:
  auth:
    max-sessions-per-user: 5
    session-timeout: 1800 # 30 minutes
    password-reset-token-expiration: 3600 # 1 hour
    email-verification-token-expiration: 86400 # 24 hours
    max-failed-login-attempts: 5
    account-lockout-duration: 900 # 15 minutes
```

## Testing Strategy

### Integration Tests
```java
@ApplicationModuleTest
class AuthModuleIntegrationTest {
    
    @Test
    void should_register_user_successfully() {
        // Test user registration flow
    }
    
    @Test
    void should_authenticate_user_with_valid_credentials() {
        // Test login flow
    }
    
    @Test
    void should_handle_2fa_flow_correctly() {
        // Test 2FA setup and verification
    }
}
```

### Repository Tests
```java
@DataJpaTest
class UserRepositoryTest {
    
    @Test
    void should_find_user_by_email() {
        // Test repository methods
    }
    
    @Test
    void should_handle_soft_delete() {
        // Test soft delete functionality
    }
}
```

## Next Steps

1. **Implementation Order**:
   - Set up database schema and entities
   - Implement basic authentication (register/login)
   - Add JWT token management
   - Implement session management
   - Add 2FA functionality
   - Implement password reset flow
   - Add device management
   - Implement role-based access control

2. **Security Considerations**:
   - Rate limiting for authentication endpoints
   - Account lockout mechanisms
   - Secure token storage
   - Audit logging for security events
   - Input validation and sanitization

3. **Performance Optimizations**:
   - Database indexing strategy
   - Caching for frequently accessed data
   - Connection pooling
   - Lazy loading optimization

This architecture provides a solid foundation for a production-ready authentication system with modern security practices and clean separation of concerns.
