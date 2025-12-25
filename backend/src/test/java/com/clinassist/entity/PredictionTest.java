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
        prediction.setConfidenceScore(0.85);
        prediction.setRiskLevel(75);
        prediction.setRiskCategory(Prediction.RiskCategory.HIGH);
        prediction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create prediction with valid data")
    void createPrediction_WithValidData_ShouldSucceed() {
        assertNotNull(prediction);
        assertEquals(0.85, prediction.getConfidenceScore());
        assertEquals(Prediction.PredictionType.DROPOUT_RISK, prediction.getType());
    }

    @Test
    @DisplayName("Should update prediction risk level")
    void updateRiskLevel_ShouldChangeRiskLevel() {
        prediction.setRiskLevel(90);
        assertEquals(90, prediction.getRiskLevel());
    }

    @Test
    @DisplayName("Prediction type enum should have correct values")
    void predictionType_ShouldHaveCorrectValues() {
        Prediction.PredictionType[] types = Prediction.PredictionType.values();
        assertTrue(types.length >= 1);
        assertEquals(Prediction.PredictionType.DROPOUT_RISK, Prediction.PredictionType.valueOf("DROPOUT_RISK"));
    }

    @Test
    @DisplayName("Should set confidence score value")
    void setConfidenceScore_ShouldUpdateConfidence() {
        prediction.setConfidenceScore(0.95);
        assertEquals(0.95, prediction.getConfidenceScore());
    }

    @Test
    @DisplayName("Risk category enum should have correct values")
    void riskCategory_ShouldHaveCorrectValues() {
        Prediction.RiskCategory[] categories = Prediction.RiskCategory.values();
        assertTrue(categories.length >= 2);
        assertEquals(Prediction.RiskCategory.HIGH, Prediction.RiskCategory.valueOf("HIGH"));
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

    @Test
    @DisplayName("Should get risk category color")
    void getRiskCategoryColor_ShouldReturnColor() {
        String color = prediction.getRiskCategoryColor();
        assertNotNull(color);
    }
}
