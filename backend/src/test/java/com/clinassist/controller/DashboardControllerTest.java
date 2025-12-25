package com.clinassist.controller;

import com.clinassist.dto.DashboardStatsDTO;
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
        testStatsDTO.setUpcomingSeances(5L);
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
    @DisplayName("Dashboard stats should contain patient information")
    void getDashboardStats_ShouldContainPatientInfo() {
        when(dashboardService.getDashboardStats()).thenReturn(testStatsDTO);

        ResponseEntity<DashboardStatsDTO> response = dashboardController.getDashboardStats();

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTotalPatients());
    }

    @Test
    @DisplayName("Dashboard stats should contain therapeute information")
    void getDashboardStats_ShouldContainTherapeuteInfo() {
        when(dashboardService.getDashboardStats()).thenReturn(testStatsDTO);

        ResponseEntity<DashboardStatsDTO> response = dashboardController.getDashboardStats();

        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getTotalTherapeutes());
    }

    @Test
    @DisplayName("Dashboard stats should contain seance information")
    void getDashboardStats_ShouldContainSeanceInfo() {
        when(dashboardService.getDashboardStats()).thenReturn(testStatsDTO);

        ResponseEntity<DashboardStatsDTO> response = dashboardController.getDashboardStats();

        assertNotNull(response.getBody());
        assertEquals(500L, response.getBody().getTotalSeances());
    }

    @Test
    @DisplayName("Dashboard stats should contain high risk patients count")
    void getDashboardStats_ShouldContainHighRiskInfo() {
        when(dashboardService.getDashboardStats()).thenReturn(testStatsDTO);

        ResponseEntity<DashboardStatsDTO> response = dashboardController.getDashboardStats();

        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getHighRiskPatients());
    }
}
