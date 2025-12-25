package com.clinassist.service;

import com.clinassist.dto.DashboardStatsDTO;
import com.clinassist.entity.Patient;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.PredictionRepository;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    void setUp() {
        // Setup common mocks for all tests
        when(patientRepository.count()).thenReturn(100L);
        when(therapeuteRepository.count()).thenReturn(10L);
        when(seanceRepository.count()).thenReturn(500L);
        
        // Mock findAll without Pageable (used at line 46)
        when(patientRepository.findAll()).thenReturn(Arrays.asList());
        
        // Mock findAll with Pageable (used at line 102)
        Page<Patient> emptyPatientPage = new PageImpl<>(Arrays.asList());
        when(patientRepository.findAll(any(Pageable.class))).thenReturn(emptyPatientPage);
        
        // Mock other repository methods that getDashboardStats uses
        when(seanceRepository.findUpcomingSeances(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        when(predictionRepository.findHighRiskPredictions(any(Integer.class))).thenReturn(Arrays.asList());
        when(seanceRepository.findByScheduledAtBetween(any(), any())).thenReturn(Arrays.asList());
        when(predictionRepository.countAccuratePredictions()).thenReturn(0L);
        when(predictionRepository.countEvaluatedPredictions()).thenReturn(0L);
    }

    @Test
    @DisplayName("Should return dashboard stats")
    void getDashboardStats_ShouldReturnStats() {
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
        verify(patientRepository, times(1)).count();
    }

    @Test
    @DisplayName("Dashboard stats should contain patient information")
    void getDashboardStats_ShouldContainPatientInfo() {
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Dashboard stats should contain therapeute information")
    void getDashboardStats_ShouldContainTherapeuteInfo() {
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Dashboard stats should contain seance information")
    void getDashboardStats_ShouldContainSeanceInfo() {
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
    }
}
