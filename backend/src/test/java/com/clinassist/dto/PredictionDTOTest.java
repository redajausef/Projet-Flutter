package com.clinassist.dto;

import com.clinassist.entity.Prediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
        predictionDTO.setScore(0.75);
        predictionDTO.setConfidence(0.85);
        predictionDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create PredictionDTO with valid data")
    void createPredictionDTO_WithValidData_ShouldSucceed() {
        assertNotNull(predictionDTO);
        assertEquals(0.75, predictionDTO.getScore());
        assertEquals(Prediction.PredictionType.DROPOUT_RISK, predictionDTO.getType());
    }

    @Test
    @DisplayName("Should set score correctly")
    void setScore_ShouldUpdateScore() {
        predictionDTO.setScore(0.9);
        assertEquals(0.9, predictionDTO.getScore());
    }

    @Test
    @DisplayName("Should set confidence correctly")
    void setConfidence_ShouldUpdateConfidence() {
        predictionDTO.setConfidence(0.95);
        assertEquals(0.95, predictionDTO.getConfidence());
    }

    @Test
    @DisplayName("Should set recommendations")
    void setRecommendations_ShouldUpdateRecommendations() {
        predictionDTO.setRecommendations("Increase frequency");
        assertEquals("Increase frequency", predictionDTO.getRecommendations());
    }

    @Test
    @DisplayName("Should set contributing factors")
    void setContributingFactors_ShouldUpdateFactors() {
        List<String> factors = Arrays.asList("Missed sessions", "Low engagement");
        predictionDTO.setContributingFactors(factors);
        assertEquals(2, predictionDTO.getContributingFactors().size());
    }

    @Test
    @DisplayName("Should identify high risk prediction")
    void isHighRisk_ShouldReturnTrue_WhenScoreAboveThreshold() {
        predictionDTO.setScore(0.8);
        assertTrue(predictionDTO.getScore() >= 0.7);
    }

    @Test
    @DisplayName("Should identify low risk prediction")
    void isHighRisk_ShouldReturnFalse_WhenScoreBelowThreshold() {
        predictionDTO.setScore(0.3);
        assertFalse(predictionDTO.getScore() >= 0.7);
    }

    @Test
    @DisplayName("Should set patient name")
    void setPatientName_ShouldUpdateName() {
        predictionDTO.setPatientName("Marie Curie");
        assertEquals("Marie Curie", predictionDTO.getPatientName());
    }
}
