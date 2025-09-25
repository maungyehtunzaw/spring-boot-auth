# Spring Boot System - Folder Structure Guide

## Overview
This document defines the standard folder structure for the Spring Boot modular system following Spring Modulith best practices and clean architecture principles.

## Root Project Structure
```
spring-boot-system/
├── .github/
│   └── copilot-instructions.md           # AI coding guidelines
├── docs/                                 # Project documentation
│   ├── auth.md                          # Authentication module docs
│   ├── api.md                           # API documentation
│   └── deployment.md                    # Deployment guides
├── docker/                              # Docker configuration files
│   ├── mysql/
│   │   └── init/                        # Database initialization scripts
│   └── scripts/                         # Docker utility scripts
├── gradle/                              # Gradle wrapper files
├── src/
│   ├── main/
│   │   ├── java/dev/yehtun/spring_boot_system/
│   │   ├── proto/                       # gRPC protobuf definitions
│   │   └── resources/
│   │       ├── application.properties   # Main configuration
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── db/changelog/            # Liquibase migrations
│   │       ├── static/                  # Static web resources
│   │       └── templates/               # Template files
│   └── test/
│       ├── java/dev/yehtun/spring_boot_system/
│       └── resources/
├── build.gradle                         # Build configuration
├── compose.yaml                         # Docker Compose services
├── settings.gradle                      # Gradle settings
└── README.md                           # Project overview
```

## Source Code Structure (Spring Modulith)
```
src/main/java/dev/yehtun/spring_boot_system/
├── SpringBootSystemApplication.java     # Main application class
├── shared/                              # Cross-cutting concerns
│   ├── config/                         # Global configuration
│   │   ├── SecurityConfig.java
│   │   ├── JpaConfig.java
│   │   └── RedisConfig.java
│   ├── domain/                         # Shared domain objects
│   │   ├── AuditableEntity.java
│   │   ├── BaseEntity.java
│   │   └── events/                     # Domain events
│   ├── infrastructure/                 # Shared infrastructure
│   │   ├── exceptions/                 # Global exception handling
│   │   ├── utils/                      # Utility classes
│   │   └── validation/                 # Custom validators
│   └── web/                           # Web layer shared components
│       ├── GlobalExceptionHandler.java
│       ├── ResponseWrapper.java
│       └── filters/                    # Global filters
├── auth/                               # Authentication Module
│   ├── api/                           # REST API layer
│   │   ├── controllers/
│   │   │   ├── AuthController.java
│   │   │   ├── PasswordController.java
│   │   │   ├── TwoFactorAuthController.java
│   │   │   ├── SessionController.java
│   │   │   └── DeviceController.java
│   │   ├── dto/                       # Data Transfer Objects
│   │   │   ├── request/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── RefreshTokenRequest.java
│   │   │   │   └── ResetPasswordRequest.java
│   │   │   └── response/
│   │   │       ├── AuthResponse.java
│   │   │       ├── UserSessionResponse.java
│   │   │       └── TwoFactorSetupResponse.java
│   │   └── validation/                # API validation
│   │       ├── ValidEmail.java
│   │       └── StrongPassword.java
│   ├── domain/                        # Business logic layer
│   │   ├── entities/                  # Domain entities
│   │   │   ├── UserEntity.java
│   │   │   ├── RoleEntity.java
│   │   │   ├── PermissionEntity.java
│   │   │   ├── UserSessionEntity.java
│   │   │   ├── UserDeviceEntity.java
│   │   │   ├── TwoFactorAuthEntity.java
│   │   │   └── PasswordResetTokenEntity.java
│   │   ├── services/                  # Domain services
│   │   │   ├── AuthService.java
│   │   │   ├── AuthServiceImpl.java
│   │   │   ├── PasswordService.java
│   │   │   ├── PasswordServiceImpl.java
│   │   │   ├── TwoFactorAuthService.java
│   │   │   ├── TwoFactorAuthServiceImpl.java
│   │   │   ├── SessionService.java
│   │   │   └── DeviceService.java
│   │   ├── events/                    # Domain events
│   │   │   ├── UserRegisteredEvent.java
│   │   │   ├── UserLoggedInEvent.java
│   │   │   ├── PasswordResetRequestedEvent.java
│   │   │   └── TwoFactorEnabledEvent.java
│   │   ├── exceptions/                # Domain exceptions
│   │   │   ├── InvalidCredentialsException.java
│   │   │   ├── AccountLockedException.java
│   │   │   ├── EmailNotVerifiedException.java
│   │   │   └── TwoFactorRequiredException.java
│   │   └── enums/                     # Domain enums
│   │       ├── UserType.java
│   │       ├── UserStatus.java
│   │       └── DeviceType.java
│   ├── infrastructure/                # Data & external access
│   │   ├── repositories/              # JPA repositories
│   │   │   ├── UserRepository.java
│   │   │   ├── RoleRepository.java
│   │   │   ├── PermissionRepository.java
│   │   │   ├── UserSessionRepository.java
│   │   │   ├── UserDeviceRepository.java
│   │   │   ├── TwoFactorAuthRepository.java
│   │   │   └── PasswordResetTokenRepository.java
│   │   ├── mappers/                   # Entity-DTO mappers
│   │   │   ├── UserMapper.java
│   │   │   ├── AuthMapper.java
│   │   │   ├── SessionMapper.java
│   │   │   └── DeviceMapper.java
│   │   ├── config/                    # Module configuration
│   │   │   ├── AuthModuleConfig.java
│   │   │   ├── JwtConfig.java
│   │   │   └── MapperConfig.java
│   │   └── adapters/                  # External service adapters
│   │       ├── EmailAdapter.java
│   │       ├── RedisAdapter.java
│   │       └── TotpAdapter.java
│   └── security/                      # Security configuration
│       ├── filters/                   # Security filters
│       │   ├── JwtAuthenticationFilter.java
│       │   └── RateLimitingFilter.java
│       ├── providers/                 # Authentication providers
│       │   ├── JwtAuthenticationProvider.java
│       │   └── TwoFactorAuthProvider.java
│       └── handlers/                  # Security handlers
│           ├── AuthenticationEntryPointImpl.java
│           ├── AccessDeniedHandlerImpl.java
│           └── LogoutSuccessHandlerImpl.java
├── user/                              # User Management Module
│   ├── api/                          # User API endpoints
│   ├── domain/                       # User business logic
│   ├── infrastructure/               # User data access
│   └── events/                       # User events
├── notification/                      # Notification Module
│   ├── api/                          # Notification API
│   ├── domain/                       # Notification logic
│   ├── infrastructure/               # Email/SMS adapters
│   └── templates/                    # Message templates
└── admin/                            # Admin Management Module
    ├── api/                          # Admin API endpoints
    ├── domain/                       # Admin business logic
    └── infrastructure/               # Admin data access
```

## Test Structure
```
src/test/java/dev/yehtun/spring_boot_system/
├── SpringBootSystemApplicationTests.java
├── shared/
│   ├── config/
│   └── utils/
├── auth/
│   ├── api/
│   │   ├── controllers/
│   │   │   ├── AuthControllerTest.java
│   │   │   ├── PasswordControllerTest.java
│   │   │   └── TwoFactorAuthControllerTest.java
│   │   └── AuthModuleIntegrationTest.java
│   ├── domain/
│   │   ├── services/
│   │   │   ├── AuthServiceTest.java
│   │   │   ├── PasswordServiceTest.java
│   │   │   └── TwoFactorAuthServiceTest.java
│   │   └── entities/
│   │       ├── UserEntityTest.java
│   │       └── UserSessionEntityTest.java
│   ├── infrastructure/
│   │   └── repositories/
│   │       ├── UserRepositoryTest.java
│   │       └── UserSessionRepositoryTest.java
│   └── security/
│       ├── filters/
│       │   └── JwtAuthenticationFilterTest.java
│       └── providers/
│           └── JwtAuthenticationProviderTest.java
└── testutils/                        # Test utilities
    ├── TestDataBuilder.java
    ├── TestContainersConfig.java
    └── SecurityTestUtils.java
```

## Database Migration Structure
```
src/main/resources/db/changelog/
├── db.changelog-master.xml             # Master changelog file
├── changes/
│   ├── 001-initial-schema.xml         # Initial database schema
│   ├── 002-auth-tables.xml            # Authentication tables
│   ├── 003-user-tables.xml            # User management tables
│   ├── 004-notification-tables.xml    # Notification tables
│   ├── 005-admin-tables.xml           # Admin tables
│   └── 006-add-indexes.xml            # Performance indexes
└── data/
    ├── 001-default-roles.xml          # Default roles and permissions
    ├── 002-admin-user.xml             # Default admin user
    └── 003-test-data.xml              # Test data for development
```

## Configuration Files Structure
```
src/main/resources/
├── application.properties              # Main configuration
├── application-dev.properties          # Development environment
├── application-test.properties         # Test environment
├── application-prod.properties         # Production environment
├── logback-spring.xml                  # Logging configuration
└── messages/                           # Internationalization
    ├── messages.properties             # Default messages
    ├── messages_en.properties          # English messages
    └── messages_es.properties          # Spanish messages
```

## Docker Configuration Structure
```
docker/
├── mysql/
│   ├── init/
│   │   ├── 001-init-database.sql      # Database initialization
│   │   └── 002-create-users.sql       # User creation
│   └── conf/
│       └── my.cnf                     # MySQL configuration
├── redis/
│   └── redis.conf                     # Redis configuration
└── scripts/
    ├── start-dev.sh                   # Development startup script
    ├── stop-dev.sh                    # Development stop script
    └── reset-db.sh                    # Database reset script
```

## Module Boundaries (Spring Modulith)

### Auth Module Dependencies
- **Depends on**: `shared` module only
- **Exposes**: Authentication APIs, User principal, Security configuration
- **Events Published**: UserRegisteredEvent, UserLoggedInEvent, PasswordResetRequestedEvent
- **Events Consumed**: None (root module)

### User Module Dependencies
- **Depends on**: `shared`, `auth` modules
- **Exposes**: User management APIs, User profile management
- **Events Published**: UserProfileUpdatedEvent, UserDeactivatedEvent
- **Events Consumed**: UserRegisteredEvent (from auth)

### Notification Module Dependencies
- **Depends on**: `shared` module only
- **Exposes**: Notification APIs, Email/SMS services
- **Events Published**: NotificationSentEvent, NotificationFailedEvent
- **Events Consumed**: UserRegisteredEvent, PasswordResetRequestedEvent, TwoFactorEnabledEvent

### Admin Module Dependencies
- **Depends on**: `shared`, `auth`, `user` modules
- **Exposes**: Admin management APIs, System monitoring
- **Events Published**: AdminActionPerformedEvent
- **Events Consumed**: All events (for monitoring and auditing)

## Naming Conventions

### Java Classes
- **Entities**: `UserEntity`, `RoleEntity` (suffix with Entity)
- **DTOs**: `LoginRequest`, `AuthResponse` (Request/Response suffixes)
- **Services**: `AuthService` (interface), `AuthServiceImpl` (implementation)
- **Controllers**: `AuthController`, `PasswordController`
- **Repositories**: `UserRepository`, `RoleRepository`
- **Exceptions**: `InvalidCredentialsException`, `AccountLockedException`

### Database Tables
- **Table names**: `users`, `roles`, `user_sessions` (snake_case, plural)
- **Column names**: `user_id`, `created_at`, `email_verified` (snake_case)
- **Foreign keys**: `user_id`, `role_id` (table_name + _id)
- **Indexes**: `idx_users_email`, `idx_sessions_token` (idx_ prefix)

### REST Endpoints
- **Base path**: `/api/v1/{module}`
- **Auth endpoints**: `/api/v1/auth/login`, `/api/v1/auth/register`
- **Resource endpoints**: `/api/v1/users/{id}`, `/api/v1/users/{id}/sessions`

## Development Guidelines

### Module Creation Checklist
1. Create module package structure following the template above
2. Define module boundaries in `@ApplicationModule` annotation
3. Create domain entities with proper JPA annotations
4. Implement service layer with interfaces and implementations
5. Create API controllers with proper validation
6. Add repository layer with custom query methods
7. Create Liquibase migration for database schema
8. Write comprehensive tests for each layer
9. Document API endpoints and module interactions
10. Update this folder structure guide

### File Naming Rules
- Use descriptive names that indicate the file's purpose
- Follow Java naming conventions (PascalCase for classes, camelCase for methods)
- Use consistent suffixes for similar types of classes
- Keep package names lowercase and avoid abbreviations
- Use meaningful names for test methods (`should_authenticate_user_with_valid_credentials`)

This structure ensures clear separation of concerns, maintainable code organization, and adherence to Spring Modulith best practices.