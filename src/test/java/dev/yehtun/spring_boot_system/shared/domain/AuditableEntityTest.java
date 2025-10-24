package dev.yehtun.spring_boot_system.shared.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for AuditableEntity.
 * 
 * Tests the auditing functionality including:
 * - Automatic timestamp generation on creation
 * - Automatic timestamp updates on modification
 * - User tracking for created/updated by
 * - Soft delete functionality
 */
class AuditableEntityTest {

    private TestAuditableEntity entity;
    private UUID testUserId;

    // Test implementation of AuditableEntity
    private static class TestAuditableEntity extends AuditableEntity {
        private String name;
        
        public TestAuditableEntity() {}
        
        public TestAuditableEntity(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }

    @BeforeEach
    void setUp() {
        entity = new TestAuditableEntity("Test Entity");
        entity.setId(UUID.randomUUID());
        testUserId = UUID.randomUUID();
    }

    @Test
    void should_have_created_at_timestamp_on_creation() {
        // Given & When
        TestAuditableEntity newEntity = new TestAuditableEntity("New Entity");
        
        // Then
        assertThat(newEntity.getCreatedAt()).isNotNull();
        assertThat(newEntity.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(newEntity.getCreatedAt()).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void should_have_updated_at_timestamp_on_creation() {
        // Given & When
        TestAuditableEntity newEntity = new TestAuditableEntity("New Entity");
        
        // Then
        assertThat(newEntity.getUpdatedAt()).isNotNull();
        assertThat(newEntity.getUpdatedAt()).isEqualTo(newEntity.getCreatedAt());
    }

    @Test
    void should_update_updated_at_timestamp_when_modified() {
        // Given
        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();
        
        // When
        try { Thread.sleep(10); } catch (InterruptedException e) { /* ignore */ }
        entity.onPreUpdate();
        
        // Then
        assertThat(entity.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void should_set_created_by_user() {
        // Given & When
        entity.setCreatedBy(testUserId);
        
        // Then
        assertThat(entity.getCreatedBy()).isEqualTo(testUserId);
    }

    @Test
    void should_track_updated_by_user() {
        // Given & When
        entity.setUpdatedBy(testUserId);
        
        // Then
        assertThat(entity.getUpdatedBy()).isEqualTo(testUserId);
    }

    @Test
    void should_be_active_by_default() {
        // Given & When & Then
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    void should_be_marked_as_deleted_when_soft_deleted() {
        // Given & When
        entity.softDelete();
        
        // Then
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void should_restore_from_soft_delete() {
        // Given
        entity.softDelete();
        assertThat(entity.isDeleted()).isTrue();
        
        // When
        entity.restore();
        
        // Then
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    void should_set_deleted_by_user_on_soft_delete() {
        // Given & When
        entity.softDelete(testUserId);
        
        // Then
        assertThat(entity.getDeletedBy()).isEqualTo(testUserId);
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    void should_maintain_base_entity_functionality() {
        // Given & When & Then
        assertThat(entity.getId()).isNotNull();
        assertThat(entity).isInstanceOf(BaseEntity.class);
    }
}