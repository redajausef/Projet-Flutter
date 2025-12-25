package com.clinassist.controller;

import com.clinassist.dto.DashboardStatsDTO;
import com.clinassist.dto.SeanceDTO;
import com.clinassist.service.DashboardService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController Unit Tests")
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private DashboardStatsDTO testStatsDTO;

    @BeforeEach
    void setUp() {
        testStatsDTO = new DashboardStatsDTO();
        testStatsDTO.setTotalPatients(100L);
        testStatsDTO.setActivePatients(80L);
        testStatsDTO.setTotalTherapeutes(10L);
        testStatsDTO.setTotalSeances(500L);
        testStatsDTO.setTodaysSeances(5L);
        testStatsDTO.setHighRiskPatients(10L);
    }

    @Test
    @DisplayName("GET /dashboard/stats should return dashboard stats")
    void getDashboardStats_ShouldReturnStats() {
        when(dashboardService.getDashboardStats()).thenReturn(testStatsDTO);

        ResponseEntity<DashboardStatsDTO> response = dashboardController.getDashboardStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getTotalPatients());
        assertEquals(80L, response.getBody().getActivePatients());
    }

    @Test
    @DisplayName("GET /dashboard/upcoming-seances should return upcoming seances")
    void getUpcomingSeances_ShouldReturnSeances() {
        SeanceDTO seanceDTO = new SeanceDTO();
        seanceDTO.setId(1L);
        List<SeanceDTO> seances = Arrays.asList(seanceDTO);
        
        when(dashboardService.getUpcomingSeances(5)).thenReturn(seances);

        ResponseEntity<List<SeanceDTO>> response = dashboardController.getUpcomingSeances(5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("GET /dashboard/weekly-stats should return weekly stats")
    void getWeeklyStats_ShouldReturnStats() {
        Map<String, Object> weeklyStats = new HashMap<>();
        weeklyStats.put("totalSeances", 20);
        weeklyStats.put("completedSeances", 18);
        
        when(dashboardService.getWeeklyStats()).thenReturn(weeklyStats);

        ResponseEntity<Map<String, Object>> response = dashboardController.getWeeklyStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /dashboard/high-risk-count should return high risk patients count")
    void getHighRiskCount_ShouldReturnCount() {
        when(dashboardService.getHighRiskPatientsCount(70)).thenReturn(10L);

        ResponseEntity<Long> response = dashboardController.getHighRiskPatientsCount(70);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody());
    }
}
