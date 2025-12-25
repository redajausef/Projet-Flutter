package com.clinassist.controller;

import com.clinassist.dto.CreateSeanceRequest;
import com.clinassist.dto.SeanceDTO;
import com.clinassist.entity.Seance;
import com.clinassist.security.JwtTokenProvider;
import com.clinassist.service.SeanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour SeanceController
 */
@WebMvcTest(SeanceController.class)
@DisplayName("SeanceController Integration Tests")
class SeanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeanceService seanceService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private SeanceDTO testSeance;

    @BeforeEach
    void setUp() {
        testSeance = SeanceDTO.builder()
                .id(1L)
                .seanceCode("SEA-001")
                .status(Seance.SeanceStatus.SCHEDULED)
                .type(Seance.SeanceType.IN_PERSON)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    @DisplayName("GET /seances - Should return all seances")
    @WithMockUser(roles = "ADMIN")
    void getAllSeances_ShouldReturnPage() throws Exception {
        Page<SeanceDTO> page = new PageImpl<>(Arrays.asList(testSeance), PageRequest.of(0, 10), 1);
        when(seanceService.getAllSeances(any())).thenReturn(page);

        mockMvc.perform(get("/seances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].seanceCode").value("SEA-001"));
    }

    @Test
    @DisplayName("GET /seances/{id} - Should return seance by ID")
    @WithMockUser(roles = "ADMIN")
    void getSeanceById_ShouldReturnSeance() throws Exception {
        when(seanceService.getSeanceById(1L)).thenReturn(testSeance);

        mockMvc.perform(get("/seances/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /seances/patient/{patientId} - Should return seances by patient")
    @WithMockUser(roles = "ADMIN")
    void getSeancesByPatient_ShouldReturnList() throws Exception {
        when(seanceService.getSeancesByPatient(1L)).thenReturn(Arrays.asList(testSeance));

        mockMvc.perform(get("/seances/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seanceCode").value("SEA-001"));
    }

    @Test
    @DisplayName("GET /seances/therapeute/{therapeuteId} - Should return seances by therapeute")
    @WithMockUser(roles = "THERAPEUTE")
    void getSeancesByTherapeute_ShouldReturnList() throws Exception {
        when(seanceService.getSeancesByTherapeute(1L)).thenReturn(Arrays.asList(testSeance));

        mockMvc.perform(get("/seances/therapeute/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seanceCode").value("SEA-001"));
    }

    @Test
    @DisplayName("GET /seances/today - Should return today's seances")
    @WithMockUser(roles = "ADMIN")
    void getTodaySeances_ShouldReturnList() throws Exception {
        when(seanceService.getTodaySeances()).thenReturn(Arrays.asList(testSeance));

        mockMvc.perform(get("/seances/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seanceCode").value("SEA-001"));
    }

    @Test
    @DisplayName("GET /seances/upcoming - Should return upcoming seances")
    @WithMockUser(roles = "ADMIN")
    void getUpcomingSeances_ShouldReturnList() throws Exception {
        when(seanceService.getUpcomingSeances()).thenReturn(Arrays.asList(testSeance));

        mockMvc.perform(get("/seances/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seanceCode").value("SEA-001"));
    }

    @Test
    @DisplayName("POST /seances - Should create seance")
    @WithMockUser(roles = "THERAPEUTE")
    void createSeance_ShouldReturnCreated() throws Exception {
        CreateSeanceRequest request = new CreateSeanceRequest();
        request.setPatientId(1L);
        request.setTherapeuteId(1L);
        request.setScheduledAt(LocalDateTime.now().plusDays(1));

        when(seanceService.createSeance(any(CreateSeanceRequest.class))).thenReturn(testSeance);

        mockMvc.perform(post("/seances")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seanceCode").value("SEA-001"));
    }

    @Test
    @DisplayName("PATCH /seances/{id}/status - Should update status")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_ShouldReturnUpdated() throws Exception {
        when(seanceService.updateSeanceStatus(eq(1L), any(Seance.SeanceStatus.class))).thenReturn(testSeance);

        mockMvc.perform(patch("/seances/1/status")
                .with(csrf())
                .param("status", "COMPLETED"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /seances/{id}/cancel - Should cancel seance")
    @WithMockUser(roles = "ADMIN")
    void cancelSeance_ShouldReturnUpdated() throws Exception {
        when(seanceService.cancelSeance(eq(1L), anyString())).thenReturn(testSeance);

        mockMvc.perform(patch("/seances/1/cancel")
                .with(csrf())
                .param("reason", "Patient request"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /seances/check-conflict - Should check conflicts")
    @WithMockUser(roles = "ADMIN")
    void checkConflict_ShouldReturnBoolean() throws Exception {
        when(seanceService.hasConflict(anyLong(), any(), anyInt())).thenReturn(false);

        mockMvc.perform(get("/seances/check-conflict")
                .param("therapeuteId", "1")
                .param("scheduledAt", LocalDateTime.now().plusDays(1).toString())
                .param("durationMinutes", "60"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
