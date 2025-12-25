package com.clinassist.controller;

import com.clinassist.dto.PredictionDTO;
import com.clinassist.entity.Prediction;
import com.clinassist.security.JwtTokenProvider;
import com.clinassist.service.PredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour PredictionController
 */
@WebMvcTest(PredictionController.class)
@DisplayName("PredictionController Integration Tests")
class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PredictionService predictionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private PredictionDTO testPrediction;

    @BeforeEach
    void setUp() {
        testPrediction = PredictionDTO.builder()
                .id(1L)
                .patientId(1L)
                .type(Prediction.PredictionType.DROPOUT_RISK)
                .riskLevel(75)
                .riskCategory(Prediction.RiskCategory.HIGH)
                .confidenceScore(0.85)
                .build();
    }

    @Test
    @DisplayName("GET /predictions/patient/{patientId} - Should return patient predictions")
    @WithMockUser(roles = "THERAPEUTE")
    void getPatientPredictions_ShouldReturnList() throws Exception {
        when(predictionService.getPatientPredictions(1L)).thenReturn(Arrays.asList(testPrediction));

        mockMvc.perform(get("/predictions/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].riskLevel").value(75));
    }

    @Test
    @DisplayName("GET /predictions/patient/{patientId}/latest - Should return latest predictions")
    @WithMockUser(roles = "THERAPEUTE")
    void getLatestPredictions_ShouldReturnList() throws Exception {
        when(predictionService.getLatestPredictions(1L, 5)).thenReturn(Arrays.asList(testPrediction));

        mockMvc.perform(get("/predictions/patient/1/latest").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /predictions/high-risk - Should return high risk predictions")
    @WithMockUser(roles = "ADMIN")
    void getHighRiskPredictions_ShouldReturnList() throws Exception {
        when(predictionService.getHighRiskPredictions(70)).thenReturn(Arrays.asList(testPrediction));

        mockMvc.perform(get("/predictions/high-risk").param("minRisk", "70"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].riskLevel").value(75));
    }

    @Test
    @DisplayName("POST /predictions/patient/{patientId}/dropout-risk - Should generate dropout prediction")
    @WithMockUser(roles = "THERAPEUTE")
    void generateDropoutRiskPrediction_ShouldReturnPrediction() throws Exception {
        when(predictionService.generateDropoutRiskPrediction(1L)).thenReturn(testPrediction);

        mockMvc.perform(post("/predictions/patient/1/dropout-risk").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DROPOUT_RISK"));
    }

    @Test
    @DisplayName("POST /predictions/patient/{patientId}/next-session - Should generate next session prediction")
    @WithMockUser(roles = "THERAPEUTE")
    void generateNextSessionPrediction_ShouldReturnPrediction() throws Exception {
        testPrediction.setType(Prediction.PredictionType.NEXT_SESSION_NEEDED);
        when(predictionService.generateNextSessionPrediction(1L)).thenReturn(testPrediction);

        mockMvc.perform(post("/predictions/patient/1/next-session").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /predictions/stats - Should return prediction stats")
    @WithMockUser(roles = "ADMIN")
    void getPredictionStats_ShouldReturnStats() throws Exception {
        mockMvc.perform(get("/predictions/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accuracy").value(87.5));
    }

    @Test
    @DisplayName("PATCH /predictions/{id}/reviewed - Should mark as reviewed")
    @WithMockUser(roles = "THERAPEUTE")
    void markAsReviewed_ShouldReturnUpdated() throws Exception {
        when(predictionService.markAsReviewed(1L)).thenReturn(testPrediction);

        mockMvc.perform(patch("/predictions/1/reviewed").with(csrf()))
                .andExpect(status().isOk());
    }
}
