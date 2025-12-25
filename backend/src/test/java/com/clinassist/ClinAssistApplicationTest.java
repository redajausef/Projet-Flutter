package com.clinassist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ClinAssist Application Tests")
class ClinAssistApplicationTest {

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        // Test that the Spring context loads without errors
        assertDoesNotThrow(() -> {
            // Context loading is implicit
        });
    }
}
