package com.clinassist.controller;

import com.clinassist.dto.TherapeuteDTO;
import com.clinassist.entity.Therapeute;
import com.clinassist.security.JwtTokenProvider;
import com.clinassist.service.TherapeuteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour TherapeuteController
 */
@WebMvcTest(TherapeuteController.class)
@DisplayName("TherapeuteController Integration Tests")
class TherapeuteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TherapeuteService therapeuteService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private TherapeuteDTO testTherapeute;

    @BeforeEach
    void setUp() {
        testTherapeute = TherapeuteDTO.builder()
                .id(1L)
                .therapeuteCode("THER-001")
                .firstName("Jean")
                .lastName("Martin")
                .specialization("Psychologie")
                .status(Therapeute.TherapeuteStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("GET /therapeutes - Should return all therapeutes")
    @WithMockUser(roles = "ADMIN")
    void getAllTherapeutes_ShouldReturnPage() throws Exception {
        Page<TherapeuteDTO> page = new PageImpl<>(Arrays.asList(testTherapeute), PageRequest.of(0, 10), 1);
        when(therapeuteService.getAllTherapeutes(any())).thenReturn(page);

        mockMvc.perform(get("/therapeutes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].therapeuteCode").value("THER-001"));
    }

    @Test
    @DisplayName("GET /therapeutes/{id} - Should return therapeute by ID")
    @WithMockUser(roles = "ADMIN")
    void getTherapeuteById_ShouldReturnTherapeute() throws Exception {
        when(therapeuteService.getTherapeuteById(1L)).thenReturn(testTherapeute);

        mockMvc.perform(get("/therapeutes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jean"));
    }

    @Test
    @DisplayName("GET /therapeutes/user/{userId} - Should return therapeute by user ID")
    @WithMockUser(roles = "ADMIN")
    void getTherapeuteByUserId_ShouldReturnTherapeute() throws Exception {
        when(therapeuteService.getTherapeuteByUserId(1L)).thenReturn(testTherapeute);

        mockMvc.perform(get("/therapeutes/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.therapeuteCode").value("THER-001"));
    }

    @Test
    @DisplayName("GET /therapeutes/available - Should return available therapeutes")
    @WithMockUser(roles = "ADMIN")
    void getAvailableTherapeutes_ShouldReturnList() throws Exception {
        when(therapeuteService.getAvailableTherapeutes()).thenReturn(Arrays.asList(testTherapeute));

        mockMvc.perform(get("/therapeutes/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].therapeuteCode").value("THER-001"));
    }

    @Test
    @DisplayName("GET /therapeutes/search - Should search therapeutes")
    @WithMockUser(roles = "ADMIN")
    void searchTherapeutes_ShouldReturnResults() throws Exception {
        Page<TherapeuteDTO> page = new PageImpl<>(Arrays.asList(testTherapeute), PageRequest.of(0, 10), 1);
        when(therapeuteService.searchTherapeutes(eq("Martin"), any())).thenReturn(page);

        mockMvc.perform(get("/therapeutes/search").param("q", "Martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lastName").value("Martin"));
    }

    @Test
    @DisplayName("GET /therapeutes/me - Should return current therapeute")
    @WithMockUser(username = "dr.martin", roles = "THERAPEUTE")
    void getCurrentTherapeute_ShouldReturnTherapeute() throws Exception {
        when(therapeuteService.getTherapeuteByUsername("dr.martin")).thenReturn(testTherapeute);

        mockMvc.perform(get("/therapeutes/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.therapeuteCode").value("THER-001"));
    }

    @Test
    @DisplayName("PATCH /therapeutes/{id}/availability - Should update availability")
    @WithMockUser(roles = "ADMIN")
    void updateAvailability_ShouldReturnUpdated() throws Exception {
        when(therapeuteService.updateAvailability(eq(1L), anyBoolean())).thenReturn(testTherapeute);

        mockMvc.perform(patch("/therapeutes/1/availability")
                .with(csrf())
                .param("available", "true"))
                .andExpect(status().isOk());
    }
}
