package dev.yehtun.spring_boot_system.auth.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 * Test class for UserType enum.
 * 
 * Tests:
 * - All user types are available
 * - Proper enum behavior
 * - Guest user handling
 */
class UserTypeTest {

    @Test
    void should_have_all_required_user_types() {
        // Given & When & Then
        assertThat(UserType.values()).hasSize(4);
        assertThat(UserType.GUEST).isNotNull();
        assertThat(UserType.USER).isNotNull();
        assertThat(UserType.ADMIN).isNotNull();
        assertThat(UserType.SYSTEM).isNotNull();
    }

    @Test
    void should_identify_guest_users() {
        // Given & When & Then
        assertThat(UserType.GUEST.isGuest()).isTrue();
        assertThat(UserType.USER.isGuest()).isFalse();
        assertThat(UserType.ADMIN.isGuest()).isFalse();
        assertThat(UserType.SYSTEM.isGuest()).isFalse();
    }

    @Test
    void should_identify_privileged_users() {
        // Given & When & Then
        assertThat(UserType.ADMIN.isPrivileged()).isTrue();
        assertThat(UserType.SYSTEM.isPrivileged()).isTrue();
        assertThat(UserType.USER.isPrivileged()).isFalse();
        assertThat(UserType.GUEST.isPrivileged()).isFalse();
    }

    @Test
    void should_have_proper_descriptions() {
        // Given & When & Then
        assertThat(UserType.GUEST.getDescription()).isEqualTo("Guest User");
        assertThat(UserType.USER.getDescription()).isEqualTo("Regular User");
        assertThat(UserType.ADMIN.getDescription()).isEqualTo("Administrator");
        assertThat(UserType.SYSTEM.getDescription()).isEqualTo("System User");
    }

    @Test
    void should_convert_from_string() {
        // Given & When & Then
        assertThat(UserType.fromString("GUEST")).isEqualTo(UserType.GUEST);
        assertThat(UserType.fromString("USER")).isEqualTo(UserType.USER);
        assertThat(UserType.fromString("ADMIN")).isEqualTo(UserType.ADMIN);
        assertThat(UserType.fromString("SYSTEM")).isEqualTo(UserType.SYSTEM);
    }

    @Test
    void should_handle_invalid_string_conversion() {
        // Given & When & Then
        assertThatThrownBy(() -> UserType.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid UserType: INVALID");
    }

    @Test
    void should_handle_null_string_conversion() {
        // Given & When & Then
        assertThatThrownBy(() -> UserType.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserType cannot be null or empty");
    }
}