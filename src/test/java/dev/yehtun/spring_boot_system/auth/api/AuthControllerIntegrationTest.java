package dev.yehtun.spring_boot_system.auth.api;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import dev.yehtun.spring_boot_system.auth.api.dto.AuthRequestDto;
import dev.yehtun.spring_boot_system.auth.api.dto.AuthResponseDto;
import dev.yehtun.spring_boot_system.auth.api.dto.RefreshTokenRequestDto;
import dev.yehtun.spring_boot_system.auth.api.dto.RegisterRequestDto;
import dev.yehtun.spring_boot_system.auth.infrastructure.repository.UserRepository;
import dev.yehtun.spring_boot_system.auth.infrastructure.repository.UserSessionRepository;

/**
 * End-to-end integration tests for the authentication REST API using a running
 * Spring Boot context and the in-memory H2 database configured for tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository sessionRepository;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private <T> HttpEntity<T> jsonEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    @DisplayName("User registration, login and token refresh work together")
    void registrationLoginAndRefreshFlow() {
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "integrationUser", "integration@example.com", "Password123", "Integration", "User");

        ResponseEntity<AuthResponseDto> registerResponse = restTemplate.exchange(
            url("/api/auth/register"), HttpMethod.POST, jsonEntity(registerRequest), AuthResponseDto.class);

        assertThat(registerResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().success()).isTrue();
        assertThat(registerResponse.getBody().userId()).isNotNull();
        assertThat(userRepository.existsByEmail("integration@example.com")).isTrue();

        AuthRequestDto loginRequest = new AuthRequestDto("integrationUser", "Password123");

        ResponseEntity<AuthResponseDto> loginResponse = restTemplate.exchange(
            url("/api/auth/login"), HttpMethod.POST, jsonEntity(loginRequest), AuthResponseDto.class);

        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().success()).isTrue();
        assertThat(loginResponse.getBody().sessionToken()).isNotBlank();
        assertThat(loginResponse.getBody().refreshToken()).isNotBlank();

        assertThat(sessionRepository.findAll()).hasSize(1);

        RefreshTokenRequestDto refreshRequest = new RefreshTokenRequestDto(
            loginResponse.getBody().refreshToken());

        ResponseEntity<AuthResponseDto> refreshResponse = restTemplate.exchange(
            url("/api/auth/refresh"), HttpMethod.POST, jsonEntity(refreshRequest), AuthResponseDto.class);

        assertThat(refreshResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(refreshResponse.getBody()).isNotNull();
        assertThat(refreshResponse.getBody().success()).isTrue();
        assertThat(refreshResponse.getBody().sessionToken()).isNotBlank();
        assertThat(refreshResponse.getBody().refreshToken()).isNotBlank();
        assertThat(refreshResponse.getBody().sessionToken())
            .isNotEqualTo(loginResponse.getBody().sessionToken());
    }

    @Test
    @DisplayName("Login returns 401 for unknown users")
    void loginFailsForUnknownUser() {
        AuthRequestDto loginRequest = new AuthRequestDto("missing@example.com", "Password123");

        ResponseEntity<AuthResponseDto> response = restTemplate.exchange(
            url("/api/auth/login"), HttpMethod.POST, jsonEntity(loginRequest), AuthResponseDto.class);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains("Invalid credentials");
    }
}
