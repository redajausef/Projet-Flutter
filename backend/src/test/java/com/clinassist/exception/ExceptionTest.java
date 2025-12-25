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
    @DisplayName("ResourceNotFoundException should contain resource type and id")
    void resourceNotFoundException_ShouldContainResourceInfo() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Patient", 1L);
        assertTrue(ex.getMessage().contains("Patient") || ex.getMessage().contains("1"));
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
    @DisplayName("ResourceNotFoundException with null message should not throw NPE")
    void resourceNotFoundException_WithNullMessage_ShouldNotThrowNPE() {
        assertDoesNotThrow(() -> {
            new ResourceNotFoundException((String) null);
        });
    }
}
