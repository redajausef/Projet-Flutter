package com.clinassist.service;

import com.clinassist.dto.PatientDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.User;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Unit Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TherapeuteRepository therapeuteRepository;

    @Mock
    private SeanceRepository seanceRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Jean");
        testUser.setLastName("Dupont");
        testUser.setEmail("jean.dupont@test.com");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setPatientCode("PAT-001");
        testPatient.setUser(testUser);
        testPatient.setStatus(Patient.PatientStatus.ACTIVE);
        testPatient.setRiskScore(25);
    }

    @Test
    @DisplayName("Should return all patients with pagination")
    void getAllPatients_ShouldReturnPageOfPatients() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Patient> patients = Arrays.asList(testPatient);
        Page<Patient> patientPage = new PageImpl<>(patients, pageable, 1);
        
        when(patientRepository.findAll(pageable)).thenReturn(patientPage);

        // Act
        Page<PatientDTO> result = patientService.getAllPatients(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(patientRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return patient by ID when exists")
    void getPatientById_WhenExists_ShouldReturnPatient() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act
        PatientDTO result = patientService.getPatientById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("PAT-001", result.getPatientCode());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when patient not found")
    void getPatientById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.getPatientById(99L);
        });
    }

    @Test
    @DisplayName("Should return patient by code when exists")
    void getPatientByCode_WhenExists_ShouldReturnPatient() {
        // Arrange
        when(patientRepository.findByPatientCode("PAT-001")).thenReturn(Optional.of(testPatient));

        // Act
        PatientDTO result = patientService.getPatientByCode("PAT-001");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should return high risk patients")
    void getHighRiskPatients_ShouldReturnPatientsWithHighRisk() {
        // Arrange
        testPatient.setRiskScore(80);
        List<Patient> highRiskPatients = Arrays.asList(testPatient);
        when(patientRepository.findByRiskScoreGreaterThanEqual(50)).thenReturn(highRiskPatients);

        // Act
        List<PatientDTO> result = patientService.getHighRiskPatients(50);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(patientRepository, times(1)).findByRiskScoreGreaterThanEqual(50);
    }

    @Test
    @DisplayName("Should count patients by status")
    void countByStatus_ShouldReturnCorrectCount() {
        // Arrange
        when(patientRepository.countByStatus(Patient.PatientStatus.ACTIVE)).thenReturn(5L);

        // Act
        long count = patientService.countByStatus(Patient.PatientStatus.ACTIVE);

        // Assert
        assertEquals(5L, count);
    }
}
