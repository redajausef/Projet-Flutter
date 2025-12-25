package com.clinassist.service;

import com.clinassist.dto.DashboardStatsDTO;
import com.clinassist.dto.PatientDTO;
import com.clinassist.dto.PredictionDTO;
import com.clinassist.dto.SeanceDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Prediction;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import com.clinassist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour DashboardService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceTest {

        @Mock
        private PatientRepository patientRepository;

        @Mock
        private TherapeuteRepository therapeuteRepository;

        @Mock
        private SeanceRepository seanceRepository;

        @Mock
        private PredictionRepository predictionRepository;

        @Mock
        private PatientService patientService;

        @Mock
        private SeanceService seanceService;

        @Mock
        private PredictionService predictionService;

        @InjectMocks
        private DashboardService dashboardService;

        private Patient testPatient;
        private Seance testSeance;
        private Therapeute testTherapeute;
        private Prediction testPrediction;

        @BeforeEach
        void setUp() {
                testPatient = Patient.builder()
                                .id(1L)
                                .patientCode("PAT-001")
                                .status(Patient.PatientStatus.ACTIVE)
                                .riskScore(45)
                                .createdAt(LocalDateTime.now().minusDays(5))
                                .build();

                testTherapeute = Therapeute.builder()
                                .id(1L)
                                .therapeuteCode("THER-001")
                                .status(Therapeute.TherapeuteStatus.AVAILABLE)
                                .build();

                testSeance = Seance.builder()
                                .id(1L)
                                .seanceCode("SEANCE-001")
                                .status(Seance.SeanceStatus.SCHEDULED)
                                .type(Seance.SeanceType.IN_PERSON)
                                .scheduledAt(LocalDateTime.now().plusHours(2))
                                .patient(testPatient)
                                .therapeute(testTherapeute)
                                .build();

                testPrediction = Prediction.builder()
                                .id(1L)
                                .patient(testPatient)
                                .confidenceScore(0.85)
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        @Test
        @DisplayName("Should return dashboard stats with all metrics")
        void getDashboardStats_ShouldReturnCompleteStats() {
                // Given
                when(patientRepository.count()).thenReturn(50L);
                when(patientRepository.countByStatus(Patient.PatientStatus.ACTIVE)).thenReturn(40L);
                when(patientRepository.findAll()).thenReturn(Arrays.asList(testPatient));
                when(patientRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Arrays.asList(testPatient)));
                when(patientRepository.findHighRiskPatients(70)).thenReturn(Arrays.asList(testPatient));

                when(therapeuteRepository.count()).thenReturn(10L);
                when(therapeuteRepository.findByStatus(Therapeute.TherapeuteStatus.AVAILABLE))
                                .thenReturn(Arrays.asList(testTherapeute));

                when(seanceRepository.count()).thenReturn(100L);
                when(seanceRepository.findByScheduledAtBetween(any(), any()))
                                .thenReturn(Arrays.asList(testSeance));
                when(seanceRepository.findUpcomingSeances(any())).thenReturn(Arrays.asList(testSeance));
                when(seanceRepository.findAll()).thenReturn(Arrays.asList(testSeance));

                when(predictionRepository.count()).thenReturn(200L);
                when(predictionRepository.countAccuratePredictions()).thenReturn(180L);
                when(predictionRepository.countEvaluatedPredictions()).thenReturn(200L);
                when(predictionRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Arrays.asList(testPrediction)));

                PatientDTO patientDTO = PatientDTO.builder().id(1L).patientCode("PAT-001").build();
                SeanceDTO seanceDTO = SeanceDTO.builder().id(1L).seanceCode("SEANCE-001").build();
                PredictionDTO predictionDTO = PredictionDTO.builder().id(1L).confidenceScore(0.85).build();

                when(patientService.getPatientById(1L)).thenReturn(patientDTO);
                when(seanceService.getSeanceById(1L)).thenReturn(seanceDTO);
                when(predictionService.getLatestPredictions(1L, 1)).thenReturn(Arrays.asList(predictionDTO));

                // When
                DashboardStatsDTO result = dashboardService.getDashboardStats();

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getTotalPatients()).isEqualTo(50L);
                assertThat(result.getActivePatients()).isEqualTo(40L);
                assertThat(result.getTotalTherapeutes()).isEqualTo(10L);
                assertThat(result.getAvailableTherapeutes()).isEqualTo(1L);
                assertThat(result.getTotalSeances()).isEqualTo(100L);
                assertThat(result.getTotalPredictions()).isEqualTo(200L);
                assertThat(result.getPredictionAccuracy()).isEqualTo(90.0);
        }

        @Test
        @DisplayName("Should handle empty data gracefully")
        void getDashboardStats_ShouldHandleEmptyData() {
                // Given
                when(patientRepository.count()).thenReturn(0L);
                when(patientRepository.countByStatus(any())).thenReturn(0L);
                when(patientRepository.findAll()).thenReturn(Collections.emptyList());
                when(patientRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(patientRepository.findHighRiskPatients(70)).thenReturn(Collections.emptyList());

                when(therapeuteRepository.count()).thenReturn(0L);
                when(therapeuteRepository.findByStatus(any())).thenReturn(Collections.emptyList());

                when(seanceRepository.count()).thenReturn(0L);
                when(seanceRepository.findByScheduledAtBetween(any(), any()))
                                .thenReturn(Collections.emptyList());
                when(seanceRepository.findUpcomingSeances(any())).thenReturn(Collections.emptyList());
                when(seanceRepository.findAll()).thenReturn(Collections.emptyList());

                when(predictionRepository.count()).thenReturn(0L);
                when(predictionRepository.countAccuratePredictions()).thenReturn(0L);
                when(predictionRepository.countEvaluatedPredictions()).thenReturn(0L);
                when(predictionRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Collections.emptyList()));

                // When
                DashboardStatsDTO result = dashboardService.getDashboardStats();

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getTotalPatients()).isEqualTo(0L);
                assertThat(result.getTotalTherapeutes()).isEqualTo(0L);
                assertThat(result.getTotalSeances()).isEqualTo(0L);
                assertThat(result.getPredictionAccuracy()).isEqualTo(0.0);
                assertThat(result.getSeancesTrend()).hasSize(7);
        }

        @Test
        @DisplayName("Should calculate patient growth correctly")
        void getDashboardStats_ShouldCalculatePatientGrowth() {
                // Given - simulate new patient this month
                Patient newPatient = Patient.builder()
                                .id(2L)
                                .status(Patient.PatientStatus.ACTIVE)
                                .createdAt(LocalDateTime.now().minusDays(1))
                                .build();

                when(patientRepository.count()).thenReturn(2L);
                when(patientRepository.countByStatus(any())).thenReturn(2L);
                when(patientRepository.findAll()).thenReturn(Arrays.asList(testPatient, newPatient));
                when(patientRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(patientRepository.findHighRiskPatients(70)).thenReturn(Collections.emptyList());

                when(therapeuteRepository.count()).thenReturn(0L);
                when(therapeuteRepository.findByStatus(any())).thenReturn(Collections.emptyList());

                when(seanceRepository.count()).thenReturn(0L);
                when(seanceRepository.findByScheduledAtBetween(any(), any()))
                                .thenReturn(Collections.emptyList());
                when(seanceRepository.findUpcomingSeances(any())).thenReturn(Collections.emptyList());
                when(seanceRepository.findAll()).thenReturn(Collections.emptyList());

                when(predictionRepository.count()).thenReturn(0L);
                when(predictionRepository.countAccuratePredictions()).thenReturn(0L);
                when(predictionRepository.countEvaluatedPredictions()).thenReturn(0L);
                when(predictionRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Collections.emptyList()));

                // When
                DashboardStatsDTO result = dashboardService.getDashboardStats();

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getNewPatientsThisMonth()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should calculate seance completion rate correctly")
        void getDashboardStats_ShouldCalculateCompletionRate() {
                // Given
                Seance completedSeance = Seance.builder()
                                .id(2L)
                                .status(Seance.SeanceStatus.COMPLETED)
                                .type(Seance.SeanceType.IN_PERSON)
                                .scheduledAt(LocalDateTime.now().minusDays(1))
                                .build();

                when(patientRepository.count()).thenReturn(0L);
                when(patientRepository.countByStatus(any())).thenReturn(0L);
                when(patientRepository.findAll()).thenReturn(Collections.emptyList());
                when(patientRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(patientRepository.findHighRiskPatients(70)).thenReturn(Collections.emptyList());

                when(therapeuteRepository.count()).thenReturn(0L);
                when(therapeuteRepository.findByStatus(any())).thenReturn(Collections.emptyList());

                when(seanceRepository.count()).thenReturn(2L);
                when(seanceRepository.findByScheduledAtBetween(any(), any()))
                                .thenReturn(Arrays.asList(testSeance, completedSeance));
                when(seanceRepository.findUpcomingSeances(any())).thenReturn(Arrays.asList(testSeance));
                when(seanceRepository.findAll()).thenReturn(Arrays.asList(testSeance, completedSeance));

                when(predictionRepository.count()).thenReturn(0L);
                when(predictionRepository.countAccuratePredictions()).thenReturn(0L);
                when(predictionRepository.countEvaluatedPredictions()).thenReturn(0L);
                when(predictionRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Collections.emptyList()));

                SeanceDTO seanceDTO = SeanceDTO.builder().id(1L).build();
                when(seanceService.getSeanceById(1L)).thenReturn(seanceDTO);

                // When
                DashboardStatsDTO result = dashboardService.getDashboardStats();

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getTodaySeances()).isEqualTo(2L);
                assertThat(result.getSeanceCompletionRate()).isGreaterThanOrEqualTo(0.0);
        }
}
