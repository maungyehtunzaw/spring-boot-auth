/**
 * Shared Module for Spring Boot System
 * 
 * This module contains cross-cutting concerns shared across all modules:
 * - Global configuration classes
 * - Common domain objects and events
 * - Shared infrastructure components
 * - Global exception handling
 * - Utility classes and validators
 * - Web layer shared components
 * 
 * This module should not depend on any other application modules.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Shared Module"
)
package dev.yehtun.spring_boot_system.shared;