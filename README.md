# Spring Boot System

A modular Spring Boot 3.5.6 application implementing a comprehensive authentication system with modern security practices, built using Spring Modulith architecture.

## 🚀 Features

### Authentication & Security
- ✅ JWT-based authentication with refresh tokens
- ✅ User registration and email verification
- ✅ Password reset and forgot password flows
- ✅ Two-Factor Authentication (TOTP)
- ✅ Session management with device tracking
- ✅ Role-based access control (RBAC)
- ✅ Account lockout and rate limiting
- ✅ Secure password policies

### Architecture
- ✅ Spring Modulith for modular monolith design
- ✅ Clean Architecture with clear separation of concerns
- ✅ Domain-driven design patterns
- ✅ Event-driven communication between modules
- ✅ gRPC support for inter-service communication

### Database & Persistence
- ✅ MySQL database with connection pooling
- ✅ Liquibase for database migrations
- ✅ JPA/Hibernate with audit trail
- ✅ Redis for session storage and caching
- ✅ Soft delete and data archival

## 🛠️ Tech Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3.5.6** - Latest Spring Boot version
- **Spring Modulith** - Modular monolith architecture
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **MySQL 8.0** - Primary database
- **Redis** - Caching and session storage
- **Liquibase** - Database migration tool
- **Lombok** - Code generation
- **Gradle** - Build automation
- **Docker Compose** - Local development environment

## 🏁 Quick Start

### Prerequisites
- Java 21 or higher
- Docker and Docker Compose
- Git

### Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd spring-boot-system
```

2. **Start development services**
```bash
docker-compose up -d
```
This will start:
- MySQL database on port 3306
- Redis cache on port 6379
- MailHog for email testing on port 8025

3. **Run the application**
```bash
./gradlew bootRun
```

4. **Verify setup**
- Application: http://localhost:8080
- MailHog UI: http://localhost:8025
- Health check: http://localhost:8080/actuator/health

## 📁 Project Structure

```
spring-boot-system/
├── .github/copilot-instructions.md    # AI coding guidelines
├── docs/                              # Project documentation
├── src/main/java/dev/yehtun/spring_boot_system/
│   ├── auth/                         # Authentication module
│   ├── user/                         # User management module
│   ├── shared/                       # Cross-cutting concerns
│   └── SpringBootSystemApplication.java
├── src/main/resources/
│   ├── db/changelog/                 # Database migrations
│   └── application.properties        # Configuration
├── build.gradle                      # Build configuration
└── compose.yaml                      # Docker services
```

For detailed folder structure, see [docs/folder-structure.md](docs/folder-structure.md).

## 🔐 Authentication API

### Register User
```bash
POST /api/v1/auth/register
{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "SecurePassword123!"
}
```

### Login
```bash
POST /api/v1/auth/login
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

### Refresh Token
```bash
POST /api/v1/auth/refresh
{
  "refreshToken": "your-refresh-token"
}
```

For complete API documentation, see [docs/auth.md](docs/auth.md).

## 🗄️ Database Schema

The application uses MySQL with the following core tables:
- `users` - User accounts and profiles
- `roles` - User roles (ADMIN, USER, CLIENT)
- `permissions` - Granular permissions
- `user_sessions` - Active user sessions
- `user_devices` - Registered user devices
- `two_factor_auth` - 2FA settings and backup codes

## 🧪 Testing

### Run Tests
```bash
./gradlew test                    # Run all tests
./gradlew test --tests AuthControllerTest  # Run specific test
```

### Test Categories
- **Unit Tests** - Individual component testing
- **Integration Tests** - Module boundary testing
- **Repository Tests** - Database layer testing
- **Security Tests** - Authentication and authorization testing

## 🔧 Development

### Build Commands
```bash
./gradlew build                   # Full build
./gradlew bootRun                 # Run application
./gradlew test                    # Run tests
./gradlew generateProto           # Generate protobuf classes
```

### Database Commands
```bash
./gradlew bootRun                 # Auto-applies Liquibase migrations
docker-compose exec mysql mysql -u app_user -p spring_boot_system  # Connect to DB
```

### Docker Commands
```bash
docker-compose up -d              # Start services
docker-compose down               # Stop services
docker-compose logs -f app        # View application logs
```

## 📚 Documentation

- [Authentication Module Guide](docs/auth.md) - Detailed auth implementation
- [Folder Structure Guide](docs/folder-structure.md) - Project organization
- [AI Coding Instructions](.github/copilot-instructions.md) - Development guidelines

## 🤝 Contributing

1. Follow the established folder structure and naming conventions
2. Write tests for new features
3. Update documentation when adding new modules
4. Use conventional commit messages
5. Ensure all tests pass before submitting PRs

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🔗 Links

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Liquibase Documentation](https://docs.liquibase.com/)
