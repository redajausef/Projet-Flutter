package com.clinassist.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DashboardStatsDTO Tests")
class DashboardStatsDTOTest {

    private DashboardStatsDTO statsDTO;

    @BeforeEach
    void setUp() {
        statsDTO = new DashboardStatsDTO();
        statsDTO.setTotalPatients(100L);
        statsDTO.setActivePatients(80L);
        statsDTO.setTotalTherapeutes(10L);
        statsDTO.setTotalSeances(500L);
        statsDTO.setUpcomingSeances(5L);
        statsDTO.setHighRiskPatients(10L);
    }

    @Test
    @DisplayName("Should create DashboardStatsDTO with valid data")
    void createDashboardStatsDTO_WithValidData_ShouldSucceed() {
        assertNotNull(statsDTO);
        assertEquals(100L, statsDTO.getTotalPatients());
        assertEquals(80L, statsDTO.getActivePatients());
    }

    @Test
    @DisplayName("Should set total patients")
    void setTotalPatients_ShouldUpdateCount() {
        statsDTO.setTotalPatients(150L);
        assertEquals(150L, statsDTO.getTotalPatients());
    }

    @Test
    @DisplayName("Should set active patients")
    void setActivePatients_ShouldUpdateCount() {
        statsDTO.setActivePatients(120L);
        assertEquals(120L, statsDTO.getActivePatients());
    }

    @Test
    @DisplayName("Should set total therapeutes")
    void setTotalTherapeutes_ShouldUpdateCount() {
        statsDTO.setTotalTherapeutes(15L);
        assertEquals(15L, statsDTO.getTotalTherapeutes());
    }

    @Test
    @DisplayName("Should set total seances")
    void setTotalSeances_ShouldUpdateCount() {
        statsDTO.setTotalSeances(600L);
        assertEquals(600L, statsDTO.getTotalSeances());
    }

    @Test
    @DisplayName("Should set upcoming seances")
    void setUpcomingSeances_ShouldUpdateCount() {
        statsDTO.setUpcomingSeances(10L);
        assertEquals(10L, statsDTO.getUpcomingSeances());
    }

    @Test
    @DisplayName("Should set high risk patients")
    void setHighRiskPatients_ShouldUpdateCount() {
        statsDTO.setHighRiskPatients(20L);
        assertEquals(20L, statsDTO.getHighRiskPatients());
    }

    @Test
    @DisplayName("Active patients should be less than or equal to total")
    void activePatients_ShouldBeLessOrEqualToTotal() {
        assertTrue(statsDTO.getActivePatients() <= statsDTO.getTotalPatients());
    }
}
