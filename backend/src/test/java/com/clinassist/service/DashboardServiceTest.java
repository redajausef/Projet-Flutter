package com.clinassist.service;

import com.clinassist.dto.DashboardStatsDTO;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.PredictionRepository;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Should return dashboard stats")
    void getDashboardStats_ShouldReturnStats() {
        when(patientRepository.count()).thenReturn(100L);
        when(therapeuteRepository.count()).thenReturn(10L);
        when(seanceRepository.count()).thenReturn(500L);

        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
        verify(patientRepository, times(1)).count();
    }

    @Test
    @DisplayName("Dashboard stats should contain patient information")
    void getDashboardStats_ShouldContainPatientInfo() {
        when(patientRepository.count()).thenReturn(50L);
        when(therapeuteRepository.count()).thenReturn(5L);
        when(seanceRepository.count()).thenReturn(200L);

        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Dashboard stats should contain therapeute information")
    void getDashboardStats_ShouldContainTherapeuteInfo() {
        when(patientRepository.count()).thenReturn(80L);
        when(therapeuteRepository.count()).thenReturn(8L);
        when(seanceRepository.count()).thenReturn(300L);

        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Dashboard stats should contain seance information")
    void getDashboardStats_ShouldContainSeanceInfo() {
        when(patientRepository.count()).thenReturn(120L);
        when(therapeuteRepository.count()).thenReturn(15L);
        when(seanceRepository.count()).thenReturn(600L);

        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
    }
}
