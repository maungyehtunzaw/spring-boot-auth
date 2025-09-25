package dev.yehtun.spring_boot_system.auth.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 * Test class for UserStatus enum.
 * 
 * Tests:
 * - All user statuses are available
 * - Status transition validation
 * - Active/inactive status checking
 */
class UserStatusTest {

    @Test
    void should_have_all_required_user_statuses() {
        // Given & When & Then
        assertThat(UserStatus.values()).hasSize(5);
        assertThat(UserStatus.ACTIVE).isNotNull();
        assertThat(UserStatus.INACTIVE).isNotNull();
        assertThat(UserStatus.SUSPENDED).isNotNull();
        assertThat(UserStatus.PENDING_VERIFICATION).isNotNull();
        assertThat(UserStatus.LOCKED).isNotNull();
    }

    @Test
    void should_identify_active_statuses() {
        // Given & When & Then
        assertThat(UserStatus.ACTIVE.isActive()).isTrue();
        assertThat(UserStatus.INACTIVE.isActive()).isFalse();
        assertThat(UserStatus.SUSPENDED.isActive()).isFalse();
        assertThat(UserStatus.PENDING_VERIFICATION.isActive()).isFalse();
        assertThat(UserStatus.LOCKED.isActive()).isFalse();
    }

    @Test
    void should_identify_login_allowed_statuses() {
        // Given & When & Then
        assertThat(UserStatus.ACTIVE.canLogin()).isTrue();
        assertThat(UserStatus.INACTIVE.canLogin()).isFalse();
        assertThat(UserStatus.SUSPENDED.canLogin()).isFalse();
        assertThat(UserStatus.PENDING_VERIFICATION.canLogin()).isFalse();
        assertThat(UserStatus.LOCKED.canLogin()).isFalse();
    }

    @Test
    void should_identify_verification_required_statuses() {
        // Given & When & Then
        assertThat(UserStatus.PENDING_VERIFICATION.requiresVerification()).isTrue();
        assertThat(UserStatus.ACTIVE.requiresVerification()).isFalse();
        assertThat(UserStatus.INACTIVE.requiresVerification()).isFalse();
        assertThat(UserStatus.SUSPENDED.requiresVerification()).isFalse();
        assertThat(UserStatus.LOCKED.requiresVerification()).isFalse();
    }

    @Test
    void should_have_proper_descriptions() {
        // Given & When & Then
        assertThat(UserStatus.ACTIVE.getDescription()).isEqualTo("Active User");
        assertThat(UserStatus.INACTIVE.getDescription()).isEqualTo("Inactive User");
        assertThat(UserStatus.SUSPENDED.getDescription()).isEqualTo("Suspended User");
        assertThat(UserStatus.PENDING_VERIFICATION.getDescription()).isEqualTo("Pending Email Verification");
        assertThat(UserStatus.LOCKED.getDescription()).isEqualTo("Account Locked");
    }

    @Test
    void should_validate_status_transitions() {
        // Given & When & Then
        // From PENDING_VERIFICATION
        assertThat(UserStatus.PENDING_VERIFICATION.canTransitionTo(UserStatus.ACTIVE)).isTrue();
        assertThat(UserStatus.PENDING_VERIFICATION.canTransitionTo(UserStatus.INACTIVE)).isTrue();
        assertThat(UserStatus.PENDING_VERIFICATION.canTransitionTo(UserStatus.SUSPENDED)).isFalse();
        
        // From ACTIVE
        assertThat(UserStatus.ACTIVE.canTransitionTo(UserStatus.INACTIVE)).isTrue();
        assertThat(UserStatus.ACTIVE.canTransitionTo(UserStatus.SUSPENDED)).isTrue();
        assertThat(UserStatus.ACTIVE.canTransitionTo(UserStatus.LOCKED)).isTrue();
        assertThat(UserStatus.ACTIVE.canTransitionTo(UserStatus.PENDING_VERIFICATION)).isFalse();
        
        // From LOCKED
        assertThat(UserStatus.LOCKED.canTransitionTo(UserStatus.ACTIVE)).isTrue();
        assertThat(UserStatus.LOCKED.canTransitionTo(UserStatus.INACTIVE)).isTrue();
        assertThat(UserStatus.LOCKED.canTransitionTo(UserStatus.SUSPENDED)).isTrue();
    }

    @Test
    void should_convert_from_string() {
        // Given & When & Then
        assertThat(UserStatus.fromString("ACTIVE")).isEqualTo(UserStatus.ACTIVE);
        assertThat(UserStatus.fromString("INACTIVE")).isEqualTo(UserStatus.INACTIVE);
        assertThat(UserStatus.fromString("SUSPENDED")).isEqualTo(UserStatus.SUSPENDED);
        assertThat(UserStatus.fromString("PENDING_VERIFICATION")).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(UserStatus.fromString("LOCKED")).isEqualTo(UserStatus.LOCKED);
    }

    @Test
    void should_handle_invalid_string_conversion() {
        // Given & When & Then
        assertThatThrownBy(() -> UserStatus.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid UserStatus: INVALID");
    }

    @Test
    void should_handle_null_string_conversion() {
        // Given & When & Then
        assertThatThrownBy(() -> UserStatus.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserStatus cannot be null or empty");
    }
}