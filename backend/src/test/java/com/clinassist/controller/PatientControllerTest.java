package com.clinassist.controller;

import com.clinassist.dto.PatientCreateRequest;
import com.clinassist.dto.PatientDTO;
import com.clinassist.entity.Patient;
import com.clinassist.security.JwtTokenProvider;
import com.clinassist.service.PatientService;
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

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour PatientController
 */
@WebMvcTest(PatientController.class)
@DisplayName("PatientController Integration Tests")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private PatientDTO testPatient;

    @BeforeEach
    void setUp() {
        testPatient = PatientDTO.builder()
                .id(1L)
                .patientCode("PAT-001")
                .email("patient@example.com")
                .firstName("Jean")
                .lastName("Dupont")
                .status(Patient.PatientStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("GET /patients - Should return all patients")
    @WithMockUser(roles = "ADMIN")
    void getAllPatients_ShouldReturnPage() throws Exception {
        Page<PatientDTO> page = new PageImpl<>(Arrays.asList(testPatient), PageRequest.of(0, 10), 1);
        when(patientService.getAllPatients(any())).thenReturn(page);

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].patientCode").value("PAT-001"));
    }

    @Test
    @DisplayName("GET /patients/{id} - Should return patient by ID")
    @WithMockUser(roles = "ADMIN")
    void getPatientById_ShouldReturnPatient() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(testPatient);

        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jean"));
    }

    @Test
    @DisplayName("GET /patients/code/{code} - Should return patient by code")
    @WithMockUser(roles = "ADMIN")
    void getPatientByCode_ShouldReturnPatient() throws Exception {
        when(patientService.getPatientByCode("PAT-001")).thenReturn(testPatient);

        mockMvc.perform(get("/patients/code/PAT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientCode").value("PAT-001"));
    }

    @Test
    @DisplayName("GET /patients/search - Should search patients")
    @WithMockUser(roles = "ADMIN")
    void searchPatients_ShouldReturnResults() throws Exception {
        Page<PatientDTO> page = new PageImpl<>(Arrays.asList(testPatient), PageRequest.of(0, 10), 1);
        when(patientService.searchPatients(eq("Jean"), any())).thenReturn(page);

        mockMvc.perform(get("/patients/search").param("q", "Jean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Jean"));
    }

    @Test
    @DisplayName("GET /patients/high-risk - Should return high risk patients")
    @WithMockUser(roles = "THERAPEUTE")
    void getHighRiskPatients_ShouldReturnList() throws Exception {
        when(patientService.getHighRiskPatients(70)).thenReturn(Arrays.asList(testPatient));

        mockMvc.perform(get("/patients/high-risk").param("minRisk", "70"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientCode").value("PAT-001"));
    }

    @Test
    @DisplayName("GET /patients/therapeute/{therapeuteId} - Should return patients by therapeute")
    @WithMockUser(roles = "THERAPEUTE")
    void getPatientsByTherapeute_ShouldReturnList() throws Exception {
        when(patientService.getPatientsByTherapeute(1L)).thenReturn(Arrays.asList(testPatient));

        mockMvc.perform(get("/patients/therapeute/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientCode").value("PAT-001"));
    }

    @Test
    @DisplayName("POST /patients - Should create patient")
    @WithMockUser(roles = "ADMIN")
    void createPatient_ShouldReturnCreated() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest();
        request.setEmail("new@example.com");
        request.setFirstName("New");
        request.setLastName("Patient");

        when(patientService.createPatient(any(PatientCreateRequest.class))).thenReturn(testPatient);

        mockMvc.perform(post("/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientCode").value("PAT-001"));
    }

    @Test
    @DisplayName("PUT /patients/{id} - Should update patient")
    @WithMockUser(roles = "ADMIN")
    void updatePatient_ShouldReturnUpdated() throws Exception {
        when(patientService.updatePatient(eq(1L), any(PatientDTO.class))).thenReturn(testPatient);

        mockMvc.perform(put("/patients/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PATCH /patients/{id}/assign/{therapeuteId} - Should assign therapeute")
    @WithMockUser(roles = "ADMIN")
    void assignTherapeute_ShouldReturnUpdated() throws Exception {
        when(patientService.assignTherapeute(1L, 2L)).thenReturn(testPatient);

        mockMvc.perform(patch("/patients/1/assign/2").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /patients/{id}/status - Should update status")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_ShouldReturnUpdated() throws Exception {
        when(patientService.updatePatientStatus(eq(1L), any(Patient.PatientStatus.class))).thenReturn(testPatient);

        mockMvc.perform(patch("/patients/1/status")
                .with(csrf())
                .param("status", "INACTIVE"))
                .andExpect(status().isOk());
    }
}
