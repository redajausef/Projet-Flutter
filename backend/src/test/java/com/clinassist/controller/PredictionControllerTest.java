package com.clinassist.controller;

import com.clinassist.dto.PredictionDTO;
import com.clinassist.entity.Prediction;
import com.clinassist.service.PredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PredictionController Unit Tests")
class PredictionControllerTest {

    @Mock
    private PredictionService predictionService;

    @InjectMocks
    private PredictionController predictionController;

    private PredictionDTO testPredictionDTO;

    @BeforeEach
    void setUp() {
        testPredictionDTO = new PredictionDTO();
        testPredictionDTO.setId(1L);
        testPredictionDTO.setPatientId(1L);
        testPredictionDTO.setPatientName("Jean Dupont");
        testPredictionDTO.setType(Prediction.PredictionType.DROPOUT_RISK);
        testPredictionDTO.setConfidenceScore(0.85);
        testPredictionDTO.setRiskLevel(75);
        testPredictionDTO.setRiskCategory(Prediction.RiskCategory.HIGH);
    }

    @Test
    @DisplayName("GET /predictions/patient/{id} should return patient predictions")
    void getPatientPredictions_ShouldReturnPredictions() {
        List<PredictionDTO> predictions = Arrays.asList(testPredictionDTO);
        when(predictionService.getPatientPredictions(1L)).thenReturn(predictions);

        ResponseEntity<List<PredictionDTO>> response = predictionController.getPatientPredictions(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("GET /predictions/high-risk should return high risk predictions")
    void getHighRiskPredictions_ShouldReturnPredictions() {
        List<PredictionDTO> predictions = Arrays.asList(testPredictionDTO);
        when(predictionService.getHighRiskPredictions(70)).thenReturn(predictions);

        ResponseEntity<List<PredictionDTO>> response = predictionController.getHighRiskPredictions(70);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("POST /predictions/patient/{id}/dropout-risk should generate prediction")
    void generateDropoutRiskPrediction_ShouldReturnPrediction() {
        when(predictionService.generateDropoutRiskPrediction(1L)).thenReturn(testPredictionDTO);

        ResponseEntity<PredictionDTO> response = predictionController.generateDropoutRiskPrediction(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("PATCH /predictions/{id}/reviewed should mark as reviewed")
    void markAsReviewed_ShouldReturnUpdatedPrediction() {
        when(predictionService.markAsReviewed(1L)).thenReturn(testPredictionDTO);

        ResponseEntity<PredictionDTO> response = predictionController.markAsReviewed(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(predictionService, times(1)).markAsReviewed(1L);
    }

    @Test
    @DisplayName("GET /predictions/patient/{id}/latest should return latest predictions")
    void getLatestPredictions_ShouldReturnPredictions() {
        List<PredictionDTO> predictions = Arrays.asList(testPredictionDTO);
        when(predictionService.getLatestPredictions(1L, 5)).thenReturn(predictions);

        ResponseEntity<List<PredictionDTO>> response = predictionController.getLatestPredictions(1L, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /predictions/stats should return stats")
    void getPredictionStats_ShouldReturnStats() {
        ResponseEntity<java.util.Map<String, Object>> response = predictionController.getPredictionStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
