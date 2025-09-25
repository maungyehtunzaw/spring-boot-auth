package dev.yehtun.spring_boot_system.shared.domain;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for BaseEntity.
 * 
 * Tests the basic functionality of the base entity including:
 * - UUID primary key generation
 * - Equality and hash code implementations
 * - toString implementation
 */
class BaseEntityTest {

    private TestEntity entity1;
    private TestEntity entity2;
    private TestEntity entityWithSameId;

    // Test implementation of BaseEntity for testing purposes
    private static class TestEntity extends BaseEntity {
        private String name;
        
        public TestEntity() {}
        
        public TestEntity(String name) {
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
        entity1 = new TestEntity("Entity 1");
        entity2 = new TestEntity("Entity 2");
        entityWithSameId = new TestEntity("Different Name");
        entityWithSameId.setId(entity1.getId());
    }

    @Test
    void should_generate_uuid_on_creation() {
        // Given & When
        TestEntity entity = new TestEntity("Test");
        
        // Then
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getId()).isInstanceOf(UUID.class);
    }

    @Test
    void should_have_different_ids_for_different_instances() {
        // Given & When & Then
        assertThat(entity1.getId()).isNotEqualTo(entity2.getId());
    }

    @Test
    void should_be_equal_when_ids_are_same() {
        // Given & When & Then
        assertThat(entity1).isEqualTo(entityWithSameId);
        assertThat(entityWithSameId).isEqualTo(entity1);
    }

    @Test
    void should_not_be_equal_when_ids_are_different() {
        // Given & When & Then
        assertThat(entity1).isNotEqualTo(entity2);
        assertThat(entity2).isNotEqualTo(entity1);
    }

    @Test
    void should_not_be_equal_to_null() {
        // Given & When & Then
        assertThat(entity1).isNotEqualTo(null);
    }

    @Test
    void should_not_be_equal_to_different_class() {
        // Given & When & Then
        assertThat(entity1).isNotEqualTo("string");
    }

    @Test
    void should_have_same_hashcode_when_ids_are_same() {
        // Given & When & Then
        assertThat(entity1.hashCode()).isEqualTo(entityWithSameId.hashCode());
    }

    @Test
    void should_have_different_hashcode_when_ids_are_different() {
        // Given & When & Then
        assertThat(entity1.hashCode()).isNotEqualTo(entity2.hashCode());
    }

    @Test
    void should_include_id_in_toString() {
        // Given & When
        String toString = entity1.toString();
        
        // Then
        assertThat(toString).contains(entity1.getId().toString());
        assertThat(toString).contains("TestEntity");
    }
}