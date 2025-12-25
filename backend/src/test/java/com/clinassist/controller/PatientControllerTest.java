package com.clinassist.controller;

import com.clinassist.dto.PatientDTO;
import com.clinassist.entity.Patient;
import com.clinassist.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientController Unit Tests")
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private PatientDTO testPatientDTO;

    @BeforeEach
    void setUp() {
        testPatientDTO = new PatientDTO();
        testPatientDTO.setId(1L);
        testPatientDTO.setPatientCode("PAT-001");
        testPatientDTO.setFirstName("Jean");
        testPatientDTO.setLastName("Dupont");
        testPatientDTO.setEmail("jean.dupont@test.com");
        testPatientDTO.setStatus(Patient.PatientStatus.ACTIVE);
    }

    @Test
    @DisplayName("GET /patients should return page of patients")
    void getAllPatients_ShouldReturnPageOfPatients() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PatientDTO> patientPage = new PageImpl<>(Arrays.asList(testPatientDTO), pageable, 1);
        
        when(patientService.getAllPatients(any(Pageable.class))).thenReturn(patientPage);

        ResponseEntity<Page<PatientDTO>> response = patientController.getAllPatients(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("GET /patients/{id} should return patient by ID")
    void getPatientById_ShouldReturnPatient() {
        when(patientService.getPatientById(1L)).thenReturn(testPatientDTO);

        ResponseEntity<PatientDTO> response = patientController.getPatientById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAT-001", response.getBody().getPatientCode());
    }

    @Test
    @DisplayName("GET /patients/code/{code} should return patient by code")
    void getPatientByCode_ShouldReturnPatient() {
        when(patientService.getPatientByCode("PAT-001")).thenReturn(testPatientDTO);

        ResponseEntity<PatientDTO> response = patientController.getPatientByCode("PAT-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    @DisplayName("GET /patients/high-risk should return high risk patients")
    void getHighRiskPatients_ShouldReturnPatients() {
        List<PatientDTO> patients = Arrays.asList(testPatientDTO);
        when(patientService.getHighRiskPatients(50)).thenReturn(patients);

        ResponseEntity<List<PatientDTO>> response = patientController.getHighRiskPatients(50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("GET /patients/therapeute/{id} should return patients by therapeute")
    void getPatientsByTherapeute_ShouldReturnPatients() {
        List<PatientDTO> patients = Arrays.asList(testPatientDTO);
        when(patientService.getPatientsByTherapeute(1L)).thenReturn(patients);

        ResponseEntity<List<PatientDTO>> response = patientController.getPatientsByTherapeute(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("DELETE /patients/{id} should delete patient")
    void deletePatient_ShouldReturnNoContent() {
        doNothing().when(patientService).deletePatient(1L);

        ResponseEntity<Void> response = patientController.deletePatient(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(patientService, times(1)).deletePatient(1L);
    }
}
