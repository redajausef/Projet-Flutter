package com.clinassist.service;

import com.clinassist.dto.PredictionDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Prediction;
import com.clinassist.entity.User;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.PredictionRepository;
import com.clinassist.repository.SeanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PredictionService Unit Tests")
class PredictionServiceTest {

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private SeanceRepository seanceRepository;

    @InjectMocks
    private PredictionService predictionService;

    private Prediction testPrediction;
    private Patient testPatient;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Jean");
        testUser.setLastName("Dupont");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setPatientCode("PAT-001");
        testPatient.setUser(testUser);
        testPatient.setRiskScore(50);

        testPrediction = new Prediction();
        testPrediction.setId(1L);
        testPrediction.setPatient(testPatient);
        testPrediction.setType(Prediction.PredictionType.DROPOUT_RISK);
        testPrediction.setConfidenceScore(0.85);
        testPrediction.setRiskLevel(75);
        testPrediction.setRiskCategory(Prediction.RiskCategory.HIGH);
        testPrediction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return predictions by patient")
    void getPatientPredictions_ShouldReturnPredictions() {
        List<Prediction> predictions = Arrays.asList(testPrediction);
        when(predictionRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(predictions);

        List<PredictionDTO> result = predictionService.getPatientPredictions(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(predictionRepository, times(1)).findByPatientIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Should return high risk predictions")
    void getHighRiskPredictions_ShouldReturnHighRiskPredictions() {
        testPrediction.setRiskLevel(80);
        List<Prediction> predictions = Arrays.asList(testPrediction);
        when(predictionRepository.findByRiskLevelGreaterThanEqualOrderByRiskLevelDesc(70)).thenReturn(predictions);

        List<PredictionDTO> result = predictionService.getHighRiskPredictions(70);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should mark prediction as reviewed")
    void markAsReviewed_ShouldUpdatePrediction() {
        when(predictionRepository.findById(1L)).thenReturn(Optional.of(testPrediction));
        when(predictionRepository.save(any(Prediction.class))).thenReturn(testPrediction);

        predictionService.markAsReviewed(1L);

        verify(predictionRepository, times(1)).save(any(Prediction.class));
    }

    @Test
    @DisplayName("Should generate dropout risk prediction")
    void generateDropoutRiskPrediction_ShouldReturnPrediction() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(seanceRepository.findByPatientIdOrderByScheduledAtDesc(1L)).thenReturn(Arrays.asList());
        when(predictionRepository.save(any(Prediction.class))).thenReturn(testPrediction);

        PredictionDTO result = predictionService.generateDropoutRiskPrediction(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return latest predictions")
    void getLatestPredictions_ShouldReturnLimitedPredictions() {
        List<Prediction> predictions = Arrays.asList(testPrediction);
        when(predictionRepository.findTopNByPatientIdOrderByCreatedAtDesc(1L, 5)).thenReturn(predictions);

        List<PredictionDTO> result = predictionService.getLatestPredictions(1L, 5);

        assertNotNull(result);
    }
}
