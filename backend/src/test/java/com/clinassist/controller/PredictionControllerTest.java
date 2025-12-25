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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        testPredictionDTO.setScore(0.75);
        testPredictionDTO.setConfidence(0.85);
        testPredictionDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /predictions should return page of predictions")
    void getAllPredictions_ShouldReturnPageOfPredictions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PredictionDTO> predictionPage = new PageImpl<>(Arrays.asList(testPredictionDTO), pageable, 1);
        
        when(predictionService.getAllPredictions(any(Pageable.class))).thenReturn(predictionPage);

        ResponseEntity<Page<PredictionDTO>> response = predictionController.getAllPredictions(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("GET /predictions/{id} should return prediction by ID")
    void getPredictionById_ShouldReturnPrediction() {
        when(predictionService.getPredictionById(1L)).thenReturn(testPredictionDTO);

        ResponseEntity<PredictionDTO> response = predictionController.getPredictionById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0.75, response.getBody().getScore());
    }

    @Test
    @DisplayName("GET /predictions/patient/{id} should return predictions by patient")
    void getPredictionsByPatient_ShouldReturnPredictions() {
        List<PredictionDTO> predictions = Arrays.asList(testPredictionDTO);
        when(predictionService.getPredictionsByPatient(1L)).thenReturn(predictions);

        ResponseEntity<List<PredictionDTO>> response = predictionController.getPredictionsByPatient(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("GET /predictions/high-risk should return high risk predictions")
    void getHighRiskPredictions_ShouldReturnPredictions() {
        List<PredictionDTO> predictions = Arrays.asList(testPredictionDTO);
        when(predictionService.getHighRiskPredictions(0.7)).thenReturn(predictions);

        ResponseEntity<List<PredictionDTO>> response = predictionController.getHighRiskPredictions(0.7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("POST /predictions/generate/{patientId} should generate prediction")
    void generatePrediction_ShouldReturnNewPrediction() {
        when(predictionService.generatePrediction(1L, Prediction.PredictionType.DROPOUT_RISK))
            .thenReturn(testPredictionDTO);

        ResponseEntity<PredictionDTO> response = predictionController.generatePrediction(
            1L, Prediction.PredictionType.DROPOUT_RISK);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /predictions/patient/{id}/latest should return latest prediction")
    void getLatestPrediction_ShouldReturnLatestPrediction() {
        when(predictionService.getLatestPredictionForPatient(1L, Prediction.PredictionType.DROPOUT_RISK))
            .thenReturn(Optional.of(testPredictionDTO));

        ResponseEntity<PredictionDTO> response = predictionController.getLatestPrediction(
            1L, Prediction.PredictionType.DROPOUT_RISK);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
