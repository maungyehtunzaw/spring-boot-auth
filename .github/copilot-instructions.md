# Spring Boot System - AI Coding Instructions

## Project Overview
This is a **Spring Boot 3.5.6** application using **Spring Modulith** for modular architecture with **Java 21**. The project implements a comprehensive authentication system with gRPC support, security, and database persistence.

## Key Technologies & Stack
- **Java 21** with Spring Boot 3.5.6
- **Spring Modulith** for modular monolith architecture
- **Spring Security** for authentication & authorization
- **Spring Data JPA** with Liquibase migrations
- **Spring gRPC** for service communication
- **Lombok** for code generation
- **MySQL** database (configured via Docker Compose)
- **Gradle** build system with protobuf plugin

## Architecture Patterns

### Modular Structure (Spring Modulith)
- Each module is a separate package under `dev.yehtun.spring_boot_system`
- Modules communicate through well-defined interfaces
- Use `@ApplicationModuleTest` for module integration tests
- Follow module boundaries: `auth`, `user`, `notification`, etc.

### Package Structure Convention
```
src/main/java/dev/yehtun/spring_boot_system/
├── auth/                    # Authentication module
│   ├── api/                # REST controllers & DTOs
│   ├── domain/             # Domain entities & services
│   ├── infrastructure/     # JPA repositories & configs
│   └── security/           # Security configurations
├── user/                   # User management module
├── shared/                 # Cross-cutting concerns
└── SpringBootSystemApplication.java
```

## Development Workflows

### Build & Run
```bash
./gradlew bootRun                    # Start application
./gradlew build                      # Full build
./gradlew test                       # Run tests
./gradlew generateProto              # Generate protobuf classes
```

### Database Management
- Use **Liquibase** for schema migrations in `src/main/resources/db/changelog/`
- Follow naming: `001-initial-schema.xml`, `002-add-auth-tables.xml`
- Database configuration via `application.properties` or Docker Compose

### Docker Development
- Configure services in `compose.yaml` (currently empty)
- Use `spring.docker.compose.enabled=true` for auto-discovery
- Standard services: MySQL, Redis (for sessions), MailHog (for testing)

## Code Conventions

### Entity Design
- Use `@Entity` with JPA annotations
- Implement `equals()`, `hashCode()` properly for entities
- Use `@CreatedDate`, `@LastModifiedDate` for auditing
- Follow naming: `UserEntity`, `RoleEntity` (not just `User`, `Role`)

### Service Layer
- Interface-based services: `AuthService` interface, `AuthServiceImpl` implementation  
- Use `@Transactional` appropriately
- Domain services in `domain` package, infrastructure in `infrastructure`

### Security Implementation
- JWT-based authentication with refresh tokens
- Method-level security with `@PreAuthorize`
- Custom authentication providers for different user types
- Session management with device tracking

### API Design
- RESTful endpoints following Spring Web conventions
- Use `@RestController` with proper HTTP methods
- DTOs for request/response in `api` package
- Global exception handling with `@ControllerAdvice`

## Module-Specific Patterns

### Auth Module Requirements
- **Entities**: User, Role, Permission, UserSession, UserDevice, TwoFactorAuth
- **Endpoints**: `/auth/login`, `/auth/register`, `/auth/refresh`, `/auth/logout`
- **Features**: 2FA, password reset, session management, device tracking
- **Security**: BCrypt passwords, JWT tokens, rate limiting

### Database Schema Conventions
- Use UUIDs for primary keys: `@Id @GeneratedValue(strategy = GenerationType.UUID)`
- Soft deletes with `deleted_at` timestamp
- Audit fields: `created_at`, `updated_at`, `created_by`, `updated_by`
- Foreign key naming: `user_id`, `role_id` (snake_case)

## Testing Strategy
- `@ApplicationModuleTest` for module boundaries
- `@DataJpaTest` for repository testing  
- `@WebMvcTest` for controller testing
- Integration tests with `@SpringBootTest`
- Use TestContainers for database integration tests

## Key Files to Reference
- `build.gradle` - Dependencies and protobuf configuration
- `src/main/resources/application.properties` - Configuration
- `compose.yaml` - Local development services
- `src/main/resources/db/changelog/` - Database migrations

## Common Gotchas
- Package names use underscore: `spring_boot_system` (not kebab-case)
- Spring Modulith requires proper module boundaries - avoid circular dependencies
- gRPC services need protobuf definitions in `src/main/proto/`
- Liquibase changesets must be ordered and immutable

## Development Priorities
1. **Modular Design** - Respect module boundaries, use interfaces
2. **Security First** - Implement proper authentication/authorization 
3. **Database Integrity** - Use migrations, proper constraints
4. **Testing** - Write tests for each module boundary
5. **Documentation** - Update this file as patterns evolve