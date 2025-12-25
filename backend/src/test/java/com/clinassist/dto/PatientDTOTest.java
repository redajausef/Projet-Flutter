package com.clinassist.dto;

import com.clinassist.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PatientDTO Tests")
class PatientDTOTest {

    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setPatientCode("PAT-001");
        patientDTO.setFirstName("Jean");
        patientDTO.setLastName("Dupont");
        patientDTO.setEmail("jean.dupont@test.com");
        patientDTO.setStatus(Patient.PatientStatus.ACTIVE);
        patientDTO.setRiskScore(50);
    }

    @Test
    @DisplayName("Should create PatientDTO with valid data")
    void createPatientDTO_WithValidData_ShouldSucceed() {
        assertNotNull(patientDTO);
        assertEquals("PAT-001", patientDTO.getPatientCode());
        assertEquals("Jean", patientDTO.getFirstName());
    }

    @Test
    @DisplayName("Should get full name")
    void getFullName_ShouldReturnCorrectName() {
        String fullName = patientDTO.getFirstName() + " " + patientDTO.getLastName();
        assertEquals("Jean Dupont", fullName);
    }

    @Test
    @DisplayName("Should set status correctly")
    void setStatus_ShouldUpdateStatus() {
        patientDTO.setStatus(Patient.PatientStatus.INACTIVE);
        assertEquals(Patient.PatientStatus.INACTIVE, patientDTO.getStatus());
    }

    @Test
    @DisplayName("Should set risk score")
    void setRiskScore_ShouldUpdateScore() {
        patientDTO.setRiskScore(80);
        assertEquals(80, patientDTO.getRiskScore());
    }

    @Test
    @DisplayName("Should set phone number")
    void setPhoneNumber_ShouldUpdatePhone() {
        patientDTO.setPhoneNumber("0612345678");
        assertEquals("0612345678", patientDTO.getPhoneNumber());
    }

    @Test
    @DisplayName("Should set date of birth")
    void setDateOfBirth_ShouldUpdateDob() {
        LocalDate dob = LocalDate.of(1990, 5, 15);
        patientDTO.setDateOfBirth(dob);
        assertEquals(dob, patientDTO.getDateOfBirth());
    }

    @Test
    @DisplayName("Should set address")
    void setAddress_ShouldUpdateAddress() {
        patientDTO.setAddress("123 Rue de Paris");
        assertEquals("123 Rue de Paris", patientDTO.getAddress());
    }

    @Test
    @DisplayName("Should set therapist ID")
    void setTherapeuteId_ShouldUpdateTherapeutId() {
        patientDTO.setAssignedTherapeuteId(5L);
        assertEquals(5L, patientDTO.getAssignedTherapeuteId());
    }
}
