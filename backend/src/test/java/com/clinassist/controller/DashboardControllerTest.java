package com.clinassist.controller;

import com.clinassist.dto.DashboardStatsDTO;
import com.clinassist.security.JwtTokenProvider;
import com.clinassist.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour DashboardController
 */
@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController Integration Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private DashboardStatsDTO dashboardStats;

    @BeforeEach
    void setUp() {
        dashboardStats = DashboardStatsDTO.builder()
                .totalPatients(100L)
                .activePatients(80L)
                .totalTherapeutes(10L)
                .availableTherapeutes(8L)
                .totalSeances(500L)
                .todaySeances(12L)
                .upcomingSeances(25L)
                .predictionAccuracy(92.5)
                .totalPredictions(300L)
                .seancesTrend(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("GET /dashboard/stats - Should return dashboard stats for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getDashboardStats_ShouldReturnStats_ForAdmin() throws Exception {
        when(dashboardService.getDashboardStats()).thenReturn(dashboardStats);

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(100))
                .andExpect(jsonPath("$.totalTherapeutes").value(10))
                .andExpect(jsonPath("$.predictionAccuracy").value(92.5));
    }

    @Test
    @DisplayName("GET /dashboard/stats - Should return dashboard stats for THERAPEUTE")
    @WithMockUser(roles = "THERAPEUTE")
    void getDashboardStats_ShouldReturnStats_ForTherapeute() throws Exception {
        when(dashboardService.getDashboardStats()).thenReturn(dashboardStats);

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(100));
    }

    @Test
    @DisplayName("GET /dashboard/stats - Should return dashboard stats for RECEPTIONIST")
    @WithMockUser(roles = "RECEPTIONIST")
    void getDashboardStats_ShouldReturnStats_ForReceptionist() throws Exception {
        when(dashboardService.getDashboardStats()).thenReturn(dashboardStats);

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todaySeances").value(12));
    }
}
