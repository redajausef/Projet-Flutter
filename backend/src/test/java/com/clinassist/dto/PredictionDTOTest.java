package com.clinassist.dto;

import com.clinassist.entity.Prediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PredictionDTO Tests")
class PredictionDTOTest {

    private PredictionDTO predictionDTO;

    @BeforeEach
    void setUp() {
        predictionDTO = new PredictionDTO();
        predictionDTO.setId(1L);
        predictionDTO.setPatientId(1L);
        predictionDTO.setPatientName("Jean Dupont");
        predictionDTO.setType(Prediction.PredictionType.DROPOUT_RISK);
        predictionDTO.setConfidenceScore(0.85);
        predictionDTO.setRiskLevel(75);
        predictionDTO.setRiskCategory(Prediction.RiskCategory.HIGH);
        predictionDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create PredictionDTO with valid data")
    void createPredictionDTO_WithValidData_ShouldSucceed() {
        assertNotNull(predictionDTO);
        assertEquals(0.85, predictionDTO.getConfidenceScore());
        assertEquals(Prediction.PredictionType.DROPOUT_RISK, predictionDTO.getType());
    }

    @Test
    @DisplayName("Should set risk level correctly")
    void setRiskLevel_ShouldUpdateRiskLevel() {
        predictionDTO.setRiskLevel(90);
        assertEquals(90, predictionDTO.getRiskLevel());
    }

    @Test
    @DisplayName("Should set confidence score correctly")
    void setConfidenceScore_ShouldUpdateConfidenceScore() {
        predictionDTO.setConfidenceScore(0.95);
        assertEquals(0.95, predictionDTO.getConfidenceScore());
    }

    @Test
    @DisplayName("Should set recommendations")
    void setRecommendations_ShouldUpdateRecommendations() {
        predictionDTO.setRecommendations("Increase frequency");
        assertEquals("Increase frequency", predictionDTO.getRecommendations());
    }

    @Test
    @DisplayName("Should set factors map")
    void setFactors_ShouldUpdateFactors() {
        Map<String, Double> factors = new HashMap<>();
        factors.put("attendance", 0.8);
        factors.put("engagement", 0.7);
        predictionDTO.setFactors(factors);
        assertEquals(2, predictionDTO.getFactors().size());
    }

    @Test
    @DisplayName("Should identify high risk prediction")
    void isHighRisk_ShouldReturnTrue_WhenRiskLevelAboveThreshold() {
        predictionDTO.setRiskLevel(80);
        assertTrue(predictionDTO.getRiskLevel() >= 70);
    }

    @Test
    @DisplayName("Should set patient name")
    void setPatientName_ShouldUpdateName() {
        predictionDTO.setPatientName("Marie Curie");
        assertEquals("Marie Curie", predictionDTO.getPatientName());
    }

    @Test
    @DisplayName("Should set risk category")
    void setRiskCategory_ShouldUpdateCategory() {
        predictionDTO.setRiskCategory(Prediction.RiskCategory.CRITICAL);
        assertEquals(Prediction.RiskCategory.CRITICAL, predictionDTO.getRiskCategory());
    }
}
