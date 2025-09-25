# Spring Boot System Makefile
# ============================

# Variables
GRADLE := ./gradlew
APP_NAME := spring-boot-system
DOCKER_COMPOSE := docker-compose
DB_HOST := localhost
DB_PORT := 3306
DB_USER := root
DB_PASS := yeyeyeye

# Default profile
PROFILE ?= dev

# Colors for output
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[1;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

.PHONY: help build clean test run stop dev prod test-env docker-up docker-down db-setup db-migrate db-rollback lint format check proto docs

# Help target
help: ## Show this help message
	@echo "$(BLUE)Spring Boot System - Available Commands$(NC)"
	@echo "========================================"
	@awk 'BEGIN {FS = ":.*##"; printf "\n"} /^[a-zA-Z_-]+:.*##/ { printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

# Build Commands
# ==============
build: ## Build the application
	@echo "$(YELLOW)Building application...$(NC)"
	$(GRADLE) build

build-dev: ## Build for development environment
	@echo "$(YELLOW)Building for development...$(NC)"
	$(GRADLE) build -Pprofile=dev

build-prod: ## Build for production environment
	@echo "$(YELLOW)Building for production...$(NC)"
	$(GRADLE) build -Pprofile=prod

clean: ## Clean build artifacts
	@echo "$(YELLOW)Cleaning build artifacts...$(NC)"
	$(GRADLE) clean

# Testing Commands
# ================
test: ## Run all tests
	@echo "$(YELLOW)Running all tests...$(NC)"
	$(GRADLE) test

test-unit: ## Run unit tests only
	@echo "$(YELLOW)Running unit tests...$(NC)"
	$(GRADLE) test --tests "*Test" --exclude-task integrationTest

test-integration: ## Run integration tests only
	@echo "$(YELLOW)Running integration tests...$(NC)"
	$(GRADLE) integrationTest

test-auth: ## Run auth module tests
	@echo "$(YELLOW)Running auth module tests...$(NC)"
	$(GRADLE) test --tests "dev.yehtun.spring_boot_system.auth.*"

test-coverage: ## Generate test coverage report
	@echo "$(YELLOW)Generating test coverage report...$(NC)"
	$(GRADLE) jacocoTestReport
	@echo "$(GREEN)Coverage report generated in build/reports/jacoco/test/html/index.html$(NC)"

# Running Commands
# ================
run: ## Run application with default profile (dev)
	@echo "$(YELLOW)Starting application ($(PROFILE) profile)...$(NC)"
	$(GRADLE) bootRun --args='--spring.profiles.active=$(PROFILE)'

dev: ## Run application in development mode
	@echo "$(YELLOW)Starting application in development mode...$(NC)"
	$(GRADLE) bootRun --args='--spring.profiles.active=dev'

prod: ## Run application in production mode
	@echo "$(YELLOW)Starting application in production mode...$(NC)"
	$(GRADLE) bootRun --args='--spring.profiles.active=prod'

test-env: ## Run application in test environment
	@echo "$(YELLOW)Starting application in test environment...$(NC)"
	$(GRADLE) bootRun --args='--spring.profiles.active=test'

stop: ## Stop running application (kills Java processes)
	@echo "$(YELLOW)Stopping application...$(NC)"
	@pkill -f "spring-boot-system" || echo "$(RED)No running application found$(NC)"

# Database Commands
# =================
db-setup: ## Setup databases (dev and main)
	@echo "$(YELLOW)Setting up databases...$(NC)"
	@mysql -u $(DB_USER) -p$(DB_PASS) -e "CREATE DATABASE IF NOT EXISTS \`spring-boot-db\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" || echo "$(RED)Failed to create main database$(NC)"
	@mysql -u $(DB_USER) -p$(DB_PASS) -e "CREATE DATABASE IF NOT EXISTS \`spring-boot-db-dev\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" || echo "$(RED)Failed to create dev database$(NC)"
	@echo "$(GREEN)Databases created successfully$(NC)"

db-migrate: ## Run database migrations
	@echo "$(YELLOW)Running database migrations...$(NC)"
	$(GRADLE) update -PliquibaseRunList=main

db-rollback: ## Rollback last migration
	@echo "$(YELLOW)Rolling back last migration...$(NC)"
	$(GRADLE) rollbackCount -PliquibaseCommandValue=1 -PliquibaseRunList=main

db-status: ## Show migration status
	@echo "$(YELLOW)Checking migration status...$(NC)"
	$(GRADLE) status -PliquibaseRunList=main

db-reset: ## Reset database (drop and recreate)
	@echo "$(RED)WARNING: This will drop all data!$(NC)"
	@read -p "Are you sure? (y/N): " confirm && [ "$$confirm" = "y" ] || exit 1
	@mysql -u $(DB_USER) -p$(DB_PASS) -e "DROP DATABASE IF EXISTS \`spring-boot-db-dev\`;"
	@mysql -u $(DB_USER) -p$(DB_PASS) -e "CREATE DATABASE \`spring-boot-db-dev\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
	@echo "$(GREEN)Database reset complete$(NC)"

# Docker Commands
# ===============
docker-up: ## Start Docker services (MySQL, Redis, MailHog)
	@echo "$(YELLOW)Starting Docker services...$(NC)"
	$(DOCKER_COMPOSE) up -d
	@echo "$(GREEN)Docker services started$(NC)"

docker-down: ## Stop Docker services
	@echo "$(YELLOW)Stopping Docker services...$(NC)"
	$(DOCKER_COMPOSE) down
	@echo "$(GREEN)Docker services stopped$(NC)"

docker-logs: ## View Docker service logs
	@echo "$(YELLOW)Showing Docker service logs...$(NC)"
	$(DOCKER_COMPOSE) logs -f

docker-clean: ## Clean Docker containers and volumes
	@echo "$(YELLOW)Cleaning Docker resources...$(NC)"
	$(DOCKER_COMPOSE) down -v --remove-orphans
	@echo "$(GREEN)Docker resources cleaned$(NC)"

# Code Quality Commands
# =====================
lint: ## Run code linting
	@echo "$(YELLOW)Running code linting...$(NC)"
	$(GRADLE) checkstyleMain checkstyleTest

format: ## Format code
	@echo "$(YELLOW)Formatting code...$(NC)"
	$(GRADLE) spotlessApply

check: ## Run all quality checks
	@echo "$(YELLOW)Running quality checks...$(NC)"
	$(GRADLE) check

# Protobuf Commands
# =================
proto: ## Generate protobuf classes
	@echo "$(YELLOW)Generating protobuf classes...$(NC)"
	$(GRADLE) generateProto

# Documentation Commands
# ======================
docs: ## Generate documentation
	@echo "$(YELLOW)Generating documentation...$(NC)"
	$(GRADLE) javadoc
	@echo "$(GREEN)Documentation generated in build/docs/javadoc/index.html$(NC)"

# Health Check Commands
# =====================
health: ## Check application health
	@echo "$(YELLOW)Checking application health...$(NC)"
	@curl -s http://localhost:8080/actuator/health | jq . || echo "$(RED)Application not running or jq not installed$(NC)"

info: ## Show application info
	@echo "$(YELLOW)Getting application info...$(NC)"
	@curl -s http://localhost:8080/actuator/info | jq . || echo "$(RED)Application not running or jq not installed$(NC)"

metrics: ## Show application metrics
	@echo "$(YELLOW)Getting application metrics...$(NC)"
	@curl -s http://localhost:8080/actuator/metrics | jq . || echo "$(RED)Application not running or jq not installed$(NC)"

# Environment Setup Commands
# ===========================
setup: db-setup ## Complete environment setup
	@echo "$(GREEN)Environment setup complete!$(NC)"
	@echo "$(BLUE)Next steps:$(NC)"
	@echo "1. Run 'make dev' to start the application"
	@echo "2. Run 'make test' to run tests"
	@echo "3. Run 'make health' to check application health"

# Development Workflow Commands
# =============================
dev-start: db-setup dev ## Setup and start development environment

dev-test: test test-coverage ## Run tests with coverage

dev-check: clean build test lint ## Full development check

# Production Commands
# ===================
prod-build: clean build-prod ## Production build

prod-check: prod-build test ## Production readiness check

# Quick Commands
# ==============
up: dev ## Quick start (alias for dev)

down: stop ## Quick stop (alias for stop)

restart: stop dev ## Restart application

logs: ## Show application logs (if running in background)
	@echo "$(YELLOW)Showing application logs...$(NC)"
	@tail -f logs/spring-boot-system.log 2>/dev/null || echo "$(RED)No log file found$(NC)"