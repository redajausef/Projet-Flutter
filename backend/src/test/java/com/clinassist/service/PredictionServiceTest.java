package com.clinassist.service;

import com.clinassist.dto.PredictionDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Prediction;
import com.clinassist.entity.User;
import com.clinassist.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Mock
    private MLPredictionClient mlPredictionClient;

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
        testPrediction.setScore(0.75);
        testPrediction.setConfidence(0.85);
        testPrediction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return all predictions with pagination")
    void getAllPredictions_ShouldReturnPageOfPredictions() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Prediction> predictions = Arrays.asList(testPrediction);
        Page<Prediction> predictionPage = new PageImpl<>(predictions, pageable, 1);
        
        when(predictionRepository.findAll(pageable)).thenReturn(predictionPage);

        Page<PredictionDTO> result = predictionService.getAllPredictions(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(predictionRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return prediction by ID when exists")
    void getPredictionById_WhenExists_ShouldReturnPrediction() {
        when(predictionRepository.findById(1L)).thenReturn(Optional.of(testPrediction));

        PredictionDTO result = predictionService.getPredictionById(1L);

        assertNotNull(result);
        assertEquals(0.75, result.getScore());
        verify(predictionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when prediction not found")
    void getPredictionById_WhenNotExists_ShouldThrowException() {
        when(predictionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            predictionService.getPredictionById(99L);
        });
    }

    @Test
    @DisplayName("Should return predictions by patient")
    void getPredictionsByPatient_ShouldReturnPredictions() {
        List<Prediction> predictions = Arrays.asList(testPrediction);
        when(predictionRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(predictions);

        List<PredictionDTO> result = predictionService.getPredictionsByPatient(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return latest prediction for patient")
    void getLatestPredictionForPatient_ShouldReturnLatestPrediction() {
        when(predictionRepository.findTopByPatientIdAndTypeOrderByCreatedAtDesc(
            1L, Prediction.PredictionType.DROPOUT_RISK))
            .thenReturn(Optional.of(testPrediction));

        Optional<PredictionDTO> result = predictionService.getLatestPredictionForPatient(
            1L, Prediction.PredictionType.DROPOUT_RISK);

        assertTrue(result.isPresent());
        assertEquals(0.75, result.get().getScore());
    }

    @Test
    @DisplayName("Should return high risk predictions")
    void getHighRiskPredictions_ShouldReturnHighRiskPredictions() {
        testPrediction.setScore(0.8);
        List<Prediction> predictions = Arrays.asList(testPrediction);
        when(predictionRepository.findByTypeAndScoreGreaterThanEqual(
            Prediction.PredictionType.DROPOUT_RISK, 0.7)).thenReturn(predictions);

        List<PredictionDTO> result = predictionService.getHighRiskPredictions(0.7);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
