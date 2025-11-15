package dev.yehtun.spring_boot_system.auth.infrastructure.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;
import dev.yehtun.spring_boot_system.auth.domain.service.AuthService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfigTest.ProtectedEndpointConfiguration.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("Public auth endpoints remain accessible without authentication")
    void publicLoginEndpointAccessibleWithoutAuthentication() throws Exception {
        UserEntity authenticatedUser = UserEntity.builder()
            .username("public-user")
            .email("public@example.com")
            .passwordHash("encoded")
            .userStatus(UserStatus.ACTIVE)
            .userType(UserType.USER)
            .build();
        authenticatedUser.setId(java.util.UUID.randomUUID());

        AuthService.AuthResult authResult = new AuthService.AuthResult(
            true,
            "Authentication successful",
            "session-token",
            "refresh-token",
            authenticatedUser,
            false,
            3600
        );

        when(authService.authenticate(eq("public@example.com"), eq("Password123")))
            .thenReturn(authResult);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usernameOrEmail\":\"public@example.com\",\"password\":\"Password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Protected endpoints require authentication by default")
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/internal/protected"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated users can access protected endpoints")
    @WithMockUser(username = "secured-user")
    void protectedEndpointAccessibleWithMockUser() throws Exception {
        mockMvc.perform(get("/internal/protected"))
            .andExpect(status().isOk())
            .andExpect(content().string("secured"));
    }

    @TestConfiguration
    static class ProtectedEndpointConfiguration {

        @Bean
        ProtectedController protectedController() {
            return new ProtectedController();
        }

        @RestController
        static class ProtectedController {

            @GetMapping("/internal/protected")
            public String secured() {
                return "secured";
            }
        }
    }
}
