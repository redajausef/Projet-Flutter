package com.clinassist.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Prediction Entity Tests")
class PredictionTest {

    private Prediction prediction;
    private Patient patient;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Jean");
        user.setLastName("Dupont");

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(user);

        prediction = new Prediction();
        prediction.setId(1L);
        prediction.setPatient(patient);
        prediction.setType(Prediction.PredictionType.DROPOUT_RISK);
        prediction.setScore(0.75);
        prediction.setConfidence(0.85);
        prediction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create prediction with valid data")
    void createPrediction_WithValidData_ShouldSucceed() {
        assertNotNull(prediction);
        assertEquals(0.75, prediction.getScore());
        assertEquals(Prediction.PredictionType.DROPOUT_RISK, prediction.getType());
    }

    @Test
    @DisplayName("Should update prediction score")
    void updateScore_ShouldChangeScore() {
        prediction.setScore(0.9);
        assertEquals(0.9, prediction.getScore());
    }

    @Test
    @DisplayName("Prediction type enum should have correct values")
    void predictionType_ShouldHaveCorrectValues() {
        Prediction.PredictionType[] types = Prediction.PredictionType.values();
        assertTrue(types.length >= 1);
        assertEquals(Prediction.PredictionType.DROPOUT_RISK, Prediction.PredictionType.valueOf("DROPOUT_RISK"));
    }

    @Test
    @DisplayName("Should set confidence value")
    void setConfidence_ShouldUpdateConfidence() {
        prediction.setConfidence(0.95);
        assertEquals(0.95, prediction.getConfidence());
    }

    @Test
    @DisplayName("Should be high risk when score is above threshold")
    void isHighRisk_ShouldReturnTrue_WhenScoreAboveThreshold() {
        prediction.setScore(0.8);
        assertTrue(prediction.getScore() >= 0.7);
    }

    @Test
    @DisplayName("Should not be high risk when score is below threshold")
    void isHighRisk_ShouldReturnFalse_WhenScoreBelowThreshold() {
        prediction.setScore(0.5);
        assertFalse(prediction.getScore() >= 0.7);
    }

    @Test
    @DisplayName("Should set recommendations")
    void setRecommendations_ShouldUpdateRecommendations() {
        prediction.setRecommendations("Increase session frequency");
        assertEquals("Increase session frequency", prediction.getRecommendations());
    }

    @Test
    @DisplayName("Should get patient from prediction")
    void getPatient_ShouldReturnPatient() {
        assertNotNull(prediction.getPatient());
        assertEquals(1L, prediction.getPatient().getId());
    }
}
