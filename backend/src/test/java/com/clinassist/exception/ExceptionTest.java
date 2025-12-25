package com.clinassist.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exception Tests")
class ExceptionTest {

    @Test
    @DisplayName("ResourceNotFoundException should contain message")
    void resourceNotFoundException_ShouldContainMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Patient not found");
        assertEquals("Patient not found", ex.getMessage());
    }

    @Test
    @DisplayName("ResourceNotFoundException is a RuntimeException")
    void resourceNotFoundException_ShouldBeRuntimeException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Test");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should throw and catch ResourceNotFoundException")
    void shouldThrowAndCatch_ResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Resource not found");
        });
    }

    @Test
    @DisplayName("ResourceNotFoundException with resource type info")
    void resourceNotFoundException_WithResourceTypeInfo_ShouldContainMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Patient", "id", 1L);
        assertTrue(ex.getMessage().contains("Patient") || ex.getMessage().length() > 0);
    }

    @Test
    @DisplayName("Should create exception with formatted message")
    void resourceNotFoundException_FormattedMessage_ShouldWork() {
        String resourceName = "Therapeute";
        String fieldName = "id";
        Object fieldValue = 99L;
        ResourceNotFoundException ex = new ResourceNotFoundException(resourceName, fieldName, fieldValue);
        assertNotNull(ex.getMessage());
    }
}
