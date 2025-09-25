package dev.yehtun.spring_boot_system.auth;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for the Authentication module.
 * 
 * This test verifies:
 * - Module can be loaded independently
 * - Spring Modulith boundaries are respected
 * - Module configuration is correct
 * - Dependencies are properly resolved
 */
@ApplicationModuleTest
@ActiveProfiles("test")
class AuthModuleIntegrationTest {

    @Test
    void contextLoads() {
        // This test verifies that the auth module context loads successfully
        // Spring Modulith will validate module boundaries and dependencies
    }
    
    @Test
    void moduleStructureIsValid() {
        // This test is automatically handled by @ApplicationModuleTest
        // It validates that the module structure follows Spring Modulith conventions
    }
    
}