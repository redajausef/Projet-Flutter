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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PredictionService avec mock ML Client
 */
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
    private MLPredictionClient mlClient;

    @InjectMocks
    private PredictionService predictionService;

    private Patient testPatient;
    private Prediction testPrediction;

    @BeforeEach
    void setUp() {
        // Setup test patient
        User testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Test");
        testUser.setLastName("Patient");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setUser(testUser);
        testPatient.setPatientCode("PAT-123");
        testPatient.setDateOfBirth(LocalDate.of(1990, 5, 15));

        // Setup test prediction
        testPrediction = new Prediction();
        testPrediction.setId(1L);
        testPrediction.setPatient(testPatient);
        testPrediction.setType(Prediction.PredictionType.DROPOUT_RISK);
        testPrediction.setRiskLevel(35);
        testPrediction.setRiskCategory(Prediction.RiskCategory.MODERATE);
        testPrediction.setConfidenceScore(0.85);
        testPrediction.setAlgorithmUsed("RandomForest");
        testPrediction.setIsActive(true);
    }

    @Nested
    @DisplayName("getPatientPredictions Tests")
    class GetPatientPredictionsTests {

        @Test
        @DisplayName("Should return patient predictions")
        void getPatientPredictions_ShouldReturnPredictions() {
            when(predictionRepository.findByPatientId(1L))
                    .thenReturn(Arrays.asList(testPrediction));

            List<PredictionDTO> result = predictionService.getPatientPredictions(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRiskLevel()).isEqualTo(35);
            verify(predictionRepository).findByPatientId(1L);
        }

        @Test
        @DisplayName("Should return empty list when no predictions")
        void getPatientPredictions_ShouldReturnEmptyList() {
            when(predictionRepository.findByPatientId(999L))
                    .thenReturn(Collections.emptyList());

            List<PredictionDTO> result = predictionService.getPatientPredictions(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLatestPredictions Tests")
    class GetLatestPredictionsTests {

        @Test
        @DisplayName("Should return latest predictions")
        void getLatestPredictions_ShouldReturnLimited() {
            when(predictionRepository.findLatestPredictions(anyLong(), any(PageRequest.class)))
                    .thenReturn(Arrays.asList(testPrediction));

            List<PredictionDTO> result = predictionService.getLatestPredictions(1L, 5);

            assertThat(result).hasSize(1);
            verify(predictionRepository).findLatestPredictions(eq(1L), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("getHighRiskPredictions Tests")
    class GetHighRiskPredictionsTests {

        @Test
        @DisplayName("Should return high risk predictions")
        void getHighRiskPredictions_ShouldReturnFiltered() {
            Prediction highRisk = new Prediction();
            highRisk.setId(2L);
            highRisk.setPatient(testPatient);
            highRisk.setRiskLevel(75);
            highRisk.setRiskCategory(Prediction.RiskCategory.HIGH);
            highRisk.setType(Prediction.PredictionType.DROPOUT_RISK);

            when(predictionRepository.findHighRiskPredictions(50))
                    .thenReturn(Arrays.asList(highRisk));

            List<PredictionDTO> result = predictionService.getHighRiskPredictions(50);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRiskLevel()).isEqualTo(75);
        }
    }

    @Nested
    @DisplayName("markAsReviewed Tests")
    class MarkAsReviewedTests {

        @Test
        @DisplayName("Should mark prediction as reviewed")
        void markAsReviewed_ShouldDeactivatePrediction() {
            when(predictionRepository.findById(1L)).thenReturn(Optional.of(testPrediction));
            when(predictionRepository.save(any(Prediction.class))).thenReturn(testPrediction);

            PredictionDTO result = predictionService.markAsReviewed(1L);

            assertThat(result).isNotNull();
            verify(predictionRepository).save(any(Prediction.class));
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void markAsReviewed_ShouldThrowWhenNotFound() {
            when(predictionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> predictionService.markAsReviewed(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("generateDropoutRiskPrediction Tests")
    class GenerateDropoutRiskPredictionTests {

        @Test
        @DisplayName("Should generate dropout risk prediction")
        void generateDropoutRiskPrediction_ShouldReturnPrediction() {
            // Given
            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(seanceRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());

            MLPredictionClient.MLPredictionResult mlResult = new MLPredictionClient.MLPredictionResult();
            mlResult.setScore(35.0);
            mlResult.setConfidence(0.85);
            mlResult.setCategory("MODERATE");
            mlResult.setAlgorithm("RandomForest");

            // Using lenient() to avoid UnnecessaryStubbingException in case of failure
            lenient()
                    .when(mlClient.predictDropoutRisk(anyDouble(), anyDouble(), anyInt(), anyInt(), anyDouble(),
                            anyInt()))
                    .thenReturn(mlResult);
            when(predictionRepository.save(any(Prediction.class))).thenReturn(testPrediction);

            // When
            PredictionDTO result = predictionService.generateDropoutRiskPrediction(1L);

            // Then
            assertThat(result).isNotNull();
            verify(mlClient).predictDropoutRisk(anyDouble(), anyDouble(), anyInt(), anyInt(), anyDouble(), anyInt());
        }

        @Test
        @DisplayName("Should throw when patient not found")
        void generateDropoutRiskPrediction_ShouldThrowWhenPatientNotFound() {
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> predictionService.generateDropoutRiskPrediction(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("generateNextSessionPrediction Tests")
    class GenerateNextSessionPredictionTests {

        @Test
        @DisplayName("Should generate next session prediction")
        void generateNextSessionPrediction_ShouldReturnPrediction() {
            // Given
            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(seanceRepository.findCompletedSeancesByPatient(1L)).thenReturn(Collections.emptyList());
            when(predictionRepository.save(any(Prediction.class))).thenReturn(testPrediction);

            // When
            PredictionDTO result = predictionService.generateNextSessionPrediction(1L);

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("generateTreatmentProgressPrediction Tests")
    class GenerateTreatmentProgressPredictionTests {

        @Test
        @DisplayName("Should generate treatment progress prediction")
        void generateTreatmentProgressPrediction_ShouldReturnPrediction() {
            // Given
            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(seanceRepository.findCompletedSeancesByPatient(1L)).thenReturn(Collections.emptyList());
            lenient().when(seanceRepository.getAverageProgressRating(1L)).thenReturn(4.5);
            when(predictionRepository.save(any(Prediction.class))).thenReturn(testPrediction);

            // When
            PredictionDTO result = predictionService.generateTreatmentProgressPrediction(1L);

            // Then
            assertThat(result).isNotNull();
        }
    }
}
