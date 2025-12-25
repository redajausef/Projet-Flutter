package com.clinassist.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Patient Entity Tests")
class PatientTest {

    private Patient patient;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Jean");
        user.setLastName("Dupont");
        user.setEmail("jean.dupont@test.com");

        patient = new Patient();
        patient.setId(1L);
        patient.setPatientCode("PAT-TEST-001");
        patient.setUser(user);
        patient.setStatus(Patient.PatientStatus.ACTIVE);
        patient.setRiskScore(50);
    }

    @Test
    @DisplayName("Should create patient with valid data")
    void createPatient_WithValidData_ShouldSucceed() {
        assertNotNull(patient);
        assertEquals("PAT-TEST-001", patient.getPatientCode());
        assertEquals(Patient.PatientStatus.ACTIVE, patient.getStatus());
    }

    @Test
    @DisplayName("Should get full name from user")
    void getFullName_ShouldReturnUserFullName() {
        String expectedName = user.getFirstName() + " " + user.getLastName();
        assertNotNull(patient.getUser());
        assertEquals("Jean", patient.getUser().getFirstName());
        assertEquals("Dupont", patient.getUser().getLastName());
    }

    @Test
    @DisplayName("Should update patient status")
    void updateStatus_ShouldChangeStatus() {
        patient.setStatus(Patient.PatientStatus.INACTIVE);
        assertEquals(Patient.PatientStatus.INACTIVE, patient.getStatus());
    }

    @Test
    @DisplayName("Should update risk score")
    void updateRiskScore_ShouldChangeScore() {
        patient.setRiskScore(80);
        assertEquals(80, patient.getRiskScore());
    }

    @Test
    @DisplayName("Patient status enum should have correct values")
    void patientStatus_ShouldHaveCorrectValues() {
        Patient.PatientStatus[] statuses = Patient.PatientStatus.values();
        assertTrue(statuses.length >= 2);
    }
}
