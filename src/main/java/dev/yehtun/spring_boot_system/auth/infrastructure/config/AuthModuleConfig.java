package dev.yehtun.spring_boot_system.auth.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for the Authentication module.
 * 
 * This class configures:
 * - Component scanning for auth module packages
 * - JPA repositories scanning
 * - Transaction management
 */
@Configuration
@ComponentScan(basePackages = "dev.yehtun.spring_boot_system.auth")
@EnableJpaRepositories(basePackages = "dev.yehtun.spring_boot_system.auth.infrastructure.repositories")
@EnableTransactionManagement
public class AuthModuleConfig {
    
    // Module-specific beans will be defined here as needed
    
}