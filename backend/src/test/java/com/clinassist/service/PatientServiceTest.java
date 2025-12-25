package com.clinassist.service;

import com.clinassist.dto.PatientDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.User;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
import com.clinassist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires simples pour PatientService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Unit Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TherapeuteRepository therapeuteRepository;

    @Mock
    private SeanceRepository seanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("patient.test");
        testUser.setFirstName("Test");
        testUser.setLastName("Patient");
        testUser.setEmail("test@example.com");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setUser(testUser);
        testPatient.setPatientCode("PAT-123");
        testPatient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        testPatient.setRiskScore(35);
        testPatient.setRiskCategory("MODERATE");
        testPatient.setStatus(Patient.PatientStatus.ACTIVE);
    }

    @Nested
    @DisplayName("getAllPatients Tests")
    class GetAllPatientsTests {

        @Test
        @DisplayName("Should return paginated patients")
        void getAllPatients_ShouldReturnPage() {
            // Given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Patient> patientPage = new PageImpl<>(Arrays.asList(testPatient), pageable, 1);
            when(patientRepository.findAll(pageable)).thenReturn(patientPage);
            when(seanceRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
            when(seanceRepository.findUpcomingSeancesByPatient(any(), any())).thenReturn(Collections.emptyList());

            // When
            Page<PatientDTO> result = patientService.getAllPatients(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(patientRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no patients")
        void getAllPatients_ShouldReturnEmptyPage() {
            // Given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Patient> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(patientRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<PatientDTO> result = patientService.getAllPatients(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPatientById Tests")
    class GetPatientByIdTests {

        @Test
        @DisplayName("Should return patient when found")
        void getPatientById_ShouldReturnPatient() {
            // Given
            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(seanceRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
            when(seanceRepository.findUpcomingSeancesByPatient(any(), any())).thenReturn(Collections.emptyList());

            // When
            PatientDTO result = patientService.getPatientById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when patient not found")
        void getPatientById_ShouldThrowWhenNotFound() {
            // Given
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> patientService.getPatientById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getHighRiskPatients Tests")
    class GetHighRiskPatientsTests {

        @Test
        @DisplayName("Should return high risk patients")
        void getHighRiskPatients_ShouldReturnFiltered() {
            // Given
            testPatient.setRiskScore(75);
            when(patientRepository.findHighRiskPatients(50))
                    .thenReturn(Arrays.asList(testPatient));
            when(seanceRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
            when(seanceRepository.findUpcomingSeancesByPatient(any(), any())).thenReturn(Collections.emptyList());

            // When
            List<PatientDTO> result = patientService.getHighRiskPatients(50);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRiskScore()).isEqualTo(75);
        }
    }

    @Nested
    @DisplayName("countByStatus Tests")
    class CountByStatusTests {

        @Test
        @DisplayName("Should return correct count")
        void countByStatus_ShouldReturnCount() {
            // Given
            when(patientRepository.countByStatus(Patient.PatientStatus.ACTIVE)).thenReturn(10L);

            // When
            long result = patientService.countByStatus(Patient.PatientStatus.ACTIVE);

            // Then
            assertThat(result).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("searchPatients Tests")
    class SearchPatientsTests {

        @Test
        @DisplayName("Should search patients by term")
        void searchPatients_ShouldReturnMatchingPatients() {
            // Given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Patient> patientPage = new PageImpl<>(Arrays.asList(testPatient), pageable, 1);
            when(patientRepository.searchPatients("test", pageable)).thenReturn(patientPage);
            when(seanceRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
            when(seanceRepository.findUpcomingSeancesByPatient(any(), any())).thenReturn(Collections.emptyList());

            // When
            Page<PatientDTO> result = patientService.searchPatients("test", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("createPatient Tests")
    class CreatePatientTests {

        @Test
        @DisplayName("Should create patient successfully")
        void createPatient_ShouldCreateAndReturnPatient() {
            // Given
            com.clinassist.dto.PatientCreateRequest request = new com.clinassist.dto.PatientCreateRequest();
            request.setEmail("newpatient@example.com");
            request.setFirstName("New");
            request.setLastName("Patient");
            request.setPhoneNumber("0612345678");

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
            when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
                Patient p = invocation.getArgument(0);
                p.setId(1L);
                p.setUser(testUser);
                return p;
            });
            when(seanceRepository.findByPatientId(any())).thenReturn(Collections.emptyList());
            when(seanceRepository.findUpcomingSeancesByPatient(any(), any())).thenReturn(Collections.emptyList());

            // When
            PatientDTO result = patientService.createPatient(request);

            // Then
            assertThat(result).isNotNull();
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should throw exception when email exists")
        void createPatient_ShouldThrow_WhenEmailExists() {
            // Given
            com.clinassist.dto.PatientCreateRequest request = new com.clinassist.dto.PatientCreateRequest();
            request.setEmail("existing@example.com");
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> patientService.createPatient(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already exists");
        }
    }

    @Nested
    @DisplayName("updatePatient Tests")
    class UpdatePatientTests {

        @Test
        @DisplayName("Should update patient successfully")
        void updatePatient_ShouldUpdateFields() {
            // Given
            PatientDTO updateDTO = PatientDTO.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .address("New Address")
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
            when(seanceRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
            when(seanceRepository.findUpcomingSeancesByPatient(any(), any())).thenReturn(Collections.emptyList());

            // When
            PatientDTO result = patientService.updatePatient(1L, updateDTO);

            // Then
            assertThat(result).isNotNull();
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should throw when patient not found")
        void updatePatient_ShouldThrow_WhenNotFound() {
            // Given
            PatientDTO updateDTO = PatientDTO.builder().firstName("Test").build();
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> patientService.updatePatient(999L, updateDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("assignTherapeute Tests")
    class AssignTherapeuteTests {

        @Test
        @DisplayName("Should assign therapeute to patient")
        void assignTherapeute_ShouldAssignSuccessfully() {
            // Given
            com.clinassist.entity.Therapeute therapeute = new com.clinassist.entity.Therapeute();
            therapeute.setId(1L);
            therapeute.setUser(testUser);

            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(therapeuteRepository.findById(1L)).thenReturn(Optional.of(therapeute));
            when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
            when(seanceRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
            when(seanceRepository.findUpcomingSeancesByPatient(any(), any())).thenReturn(Collections.emptyList());

            // When
            PatientDTO result = patientService.assignTherapeute(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should throw when patient not found")
        void assignTherapeute_ShouldThrow_WhenPatientNotFound() {
            // Given
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> patientService.assignTherapeute(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when therapeute not found")
        void assignTherapeute_ShouldThrow_WhenTherapeuteNotFound() {
            // Given
            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(therapeuteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> patientService.assignTherapeute(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updatePatientStatus Tests")
    class UpdatePatientStatusTests {

        @Test
        @DisplayName("Should update patient status")
        void updatePatientStatus_ShouldUpdateStatus() {
            // Given
            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
            when(seanceRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
            when(seanceRepository.findUpcomingSeancesByPatient(any(), any())).thenReturn(Collections.emptyList());

            // When
            PatientDTO result = patientService.updatePatientStatus(1L, Patient.PatientStatus.INACTIVE);

            // Then
            assertThat(result).isNotNull();
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should throw when patient not found")
        void updatePatientStatus_ShouldThrow_WhenNotFound() {
            // Given
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> patientService.updatePatientStatus(999L, Patient.PatientStatus.ACTIVE))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
