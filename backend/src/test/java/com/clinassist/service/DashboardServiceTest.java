package com.clinassist.service;

import com.clinassist.dto.DashboardStatsDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Seance;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should return dashboard stats")
    void getDashboardStats_ShouldReturnStats() {
        when(patientRepository.count()).thenReturn(100L);
        when(patientRepository.countByStatus(Patient.PatientStatus.ACTIVE)).thenReturn(80L);
        when(therapeuteRepository.count()).thenReturn(10L);
        when(seanceRepository.count()).thenReturn(500L);

        DashboardStatsDTO result = dashboardService.getDashboardStats();

        assertNotNull(result);
        assertEquals(100L, result.getTotalPatients());
        assertEquals(80L, result.getActivePatients());
        assertEquals(10L, result.getTotalTherapeutes());
    }

    @Test
    @DisplayName("Should return today's seances count")
    void getTodaysSeancesCount_ShouldReturnCount() {
        when(seanceRepository.countByScheduledAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(5L);

        long count = dashboardService.getTodaysSeancesCount();

        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Should return upcoming seances")
    void getUpcomingSeances_ShouldReturnSeances() {
        Seance seance = new Seance();
        seance.setId(1L);
        seance.setScheduledAt(LocalDateTime.now().plusHours(2));
        List<Seance> seances = Arrays.asList(seance);
        
        when(seanceRepository.findByStatusAndScheduledAtAfterOrderByScheduledAtAsc(
            eq(Seance.SeanceStatus.SCHEDULED), any(LocalDateTime.class)))
            .thenReturn(seances);

        var result = dashboardService.getUpcomingSeances(5);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return high risk patients count")
    void getHighRiskPatientsCount_ShouldReturnCount() {
        Patient patient = new Patient();
        patient.setRiskScore(80);
        List<Patient> patients = Arrays.asList(patient);
        
        when(patientRepository.findHighRiskPatients(70)).thenReturn(patients);

        long count = dashboardService.getHighRiskPatientsCount(70);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Should return weekly stats")
    void getWeeklyStats_ShouldReturnStats() {
        when(seanceRepository.countByScheduledAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(20L);
        when(seanceRepository.countByStatusAndScheduledAtBetween(
            eq(Seance.SeanceStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(18L);

        var result = dashboardService.getWeeklyStats();

        assertNotNull(result);
    }
}
