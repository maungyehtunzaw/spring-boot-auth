package dev.yehtun.spring_boot_system;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import dev.yehtun.spring_boot_system.auth.api.controller.AuthController;
import dev.yehtun.spring_boot_system.auth.domain.service.AuthService;

@SpringBootTest
@ActiveProfiles("test")
class SpringBootSystemApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AuthController authController;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    @DisplayName("Smoke test: application context boots")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Smoke test: critical auth beans are available")
    void smokeTestEssentialBeansPresent() {
        assertThat(authController).isNotNull();
        assertThat(authService).isNotNull();
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    @DisplayName("Smoke test: password encoder is configured")
    void passwordEncoderIsConfigured() {
        String rawPassword = "SmokePass123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotBlank();
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
    }
}
