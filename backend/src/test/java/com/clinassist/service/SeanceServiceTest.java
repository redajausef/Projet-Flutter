package com.clinassist.service;

import com.clinassist.dto.SeanceDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import com.clinassist.entity.User;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires simples pour SeanceService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeanceService Unit Tests")
class SeanceServiceTest {

    @Mock
    private SeanceRepository seanceRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TherapeuteRepository therapeuteRepository;

    @InjectMocks
    private SeanceService seanceService;

    private Seance testSeance;
    private Patient testPatient;
    private Therapeute testTherapeute;

    @BeforeEach
    void setUp() {
        // Setup User
        User patientUser = new User();
        patientUser.setId(1L);
        patientUser.setFirstName("John");
        patientUser.setLastName("Doe");

        User therapeuteUser = new User();
        therapeuteUser.setId(2L);
        therapeuteUser.setFirstName("Dr");
        therapeuteUser.setLastName("Martin");

        // Setup Patient
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setUser(patientUser);
        testPatient.setPatientCode("PAT-001");

        // Setup Therapeute
        testTherapeute = new Therapeute();
        testTherapeute.setId(1L);
        testTherapeute.setUser(therapeuteUser);

        // Setup Seance
        testSeance = new Seance();
        testSeance.setId(1L);
        testSeance.setSeanceCode("SEANCE-001");
        testSeance.setPatient(testPatient);
        testSeance.setTherapeute(testTherapeute);
        testSeance.setScheduledAt(LocalDateTime.now().plusDays(1));
        testSeance.setDurationMinutes(60);
        testSeance.setType(Seance.SeanceType.IN_PERSON);
        testSeance.setStatus(Seance.SeanceStatus.SCHEDULED);
    }

    @Nested
    @DisplayName("getAllSeances Tests")
    class GetAllSeancesTests {

        @Test
        @DisplayName("Should return paginated seances")
        void getAllSeances_ShouldReturnPage() {
            // Given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Seance> seancePage = new PageImpl<>(Arrays.asList(testSeance), pageable, 1);
            when(seanceRepository.findAll(pageable)).thenReturn(seancePage);

            // When
            Page<SeanceDTO> result = seanceService.getAllSeances(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getSeanceById Tests")
    class GetSeanceByIdTests {

        @Test
        @DisplayName("Should return seance when found")
        void getSeanceById_ShouldReturnSeance() {
            // Given
            when(seanceRepository.findById(1L)).thenReturn(Optional.of(testSeance));

            // When
            SeanceDTO result = seanceService.getSeanceById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getSeanceById_ShouldThrowWhenNotFound() {
            // Given
            when(seanceRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> seanceService.getSeanceById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getSeancesByPatient Tests")
    class GetSeancesByPatientTests {

        @Test
        @DisplayName("Should return patient seances")
        void getSeancesByPatient_ShouldReturnList() {
            // Given
            when(seanceRepository.findByPatientId(1L)).thenReturn(Arrays.asList(testSeance));

            // When
            List<SeanceDTO> result = seanceService.getSeancesByPatient(1L);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getTodaySeances Tests")
    class GetTodaySeancesTests {

        @Test
        @DisplayName("Should return today seances")
        void getTodaySeances_ShouldReturnList() {
            // Given
            when(seanceRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(Arrays.asList(testSeance));

            // When
            List<SeanceDTO> result = seanceService.getTodaySeances();

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getUpcomingSeances Tests")
    class GetUpcomingSeancesTests {

        @Test
        @DisplayName("Should return upcoming seances")
        void getUpcomingSeances_ShouldReturnList() {
            // Given
            when(seanceRepository.findUpcomingSeances(any()))
                    .thenReturn(Arrays.asList(testSeance));

            // When
            List<SeanceDTO> result = seanceService.getUpcomingSeances();

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("countByStatus Tests")
    class CountByStatusTests {

        @Test
        @DisplayName("Should return correct count")
        void countByStatus_ShouldReturnCount() {
            // Given
            when(seanceRepository.countByStatus(Seance.SeanceStatus.SCHEDULED)).thenReturn(5L);

            // When
            long result = seanceService.countByStatus(Seance.SeanceStatus.SCHEDULED);

            // Then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hasConflict Tests")
    class HasConflictTests {

        @Test
        @DisplayName("Should detect no conflict")
        void hasConflict_ShouldReturnFalse() {
            // Given
            when(seanceRepository.findByTherapeuteAndDateRange(anyLong(), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            boolean result = seanceService.hasConflict(1L, LocalDateTime.now().plusDays(1), 60);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should detect conflict")
        void hasConflict_ShouldReturnTrue() {
            // Given
            when(seanceRepository.findByTherapeuteAndDateRange(anyLong(), any(), any()))
                    .thenReturn(Arrays.asList(testSeance));

            // When
            boolean result = seanceService.hasConflict(1L, LocalDateTime.now().plusDays(1), 60);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("createSeance Tests")
    class CreateSeanceTests {

        @Test
        @DisplayName("Should create seance successfully")
        void createSeance_ShouldReturnCreatedSeance() {
            // Given
            com.clinassist.dto.CreateSeanceRequest request = new com.clinassist.dto.CreateSeanceRequest();
            request.setPatientId(1L);
            request.setTherapeuteId(1L);
            request.setScheduledAt(LocalDateTime.now().plusDays(1));
            request.setDurationMinutes(60);
            request.setType(Seance.SeanceType.IN_PERSON);

            when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
            when(therapeuteRepository.findById(1L)).thenReturn(Optional.of(testTherapeute));
            when(seanceRepository.findByTherapeuteAndDateRange(anyLong(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.save(any(Seance.class))).thenReturn(testSeance);

            // When
            SeanceDTO result = seanceService.createSeance(request);

            // Then
            assertThat(result).isNotNull();
            verify(seanceRepository).save(any(Seance.class));
        }

        @Test
        @DisplayName("Should throw when patient not found")
        void createSeance_ShouldThrowWhenPatientNotFound() {
            // Given
            com.clinassist.dto.CreateSeanceRequest request = new com.clinassist.dto.CreateSeanceRequest();
            request.setPatientId(999L);
            request.setTherapeuteId(1L);
            request.setScheduledAt(LocalDateTime.now().plusDays(1));

            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> seanceService.createSeance(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateSeanceStatus Tests")
    class UpdateSeanceStatusTests {

        @Test
        @DisplayName("Should update status successfully")
        void updateSeanceStatus_ShouldReturnUpdated() {
            // Given
            when(seanceRepository.findById(1L)).thenReturn(Optional.of(testSeance));
            when(seanceRepository.save(any(Seance.class))).thenReturn(testSeance);

            // When
            SeanceDTO result = seanceService.updateSeanceStatus(1L, Seance.SeanceStatus.IN_PROGRESS);

            // Then
            assertThat(result).isNotNull();
            verify(seanceRepository).save(any(Seance.class));
        }
    }

    @Nested
    @DisplayName("cancelSeance Tests")
    class CancelSeanceTests {

        @Test
        @DisplayName("Should cancel seance successfully")
        void cancelSeance_ShouldReturnCancelled() {
            // Given
            when(seanceRepository.findById(1L)).thenReturn(Optional.of(testSeance));
            when(seanceRepository.save(any(Seance.class))).thenReturn(testSeance);

            // When
            SeanceDTO result = seanceService.cancelSeance(1L, "Patient request");

            // Then
            assertThat(result).isNotNull();
            verify(seanceRepository).save(any(Seance.class));
        }
    }

    @Nested
    @DisplayName("rescheduleSeance Tests")
    class RescheduleSeanceTests {

        @Test
        @DisplayName("Should reschedule seance successfully")
        void rescheduleSeance_ShouldReturnRescheduled() {
            // Given
            LocalDateTime newDateTime = LocalDateTime.now().plusDays(2);
            when(seanceRepository.findById(1L)).thenReturn(Optional.of(testSeance));
            when(seanceRepository.findByTherapeuteAndDateRange(anyLong(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.save(any(Seance.class))).thenReturn(testSeance);

            // When
            SeanceDTO result = seanceService.rescheduleSeance(1L, newDateTime);

            // Then
            assertThat(result).isNotNull();
            verify(seanceRepository).save(any(Seance.class));
        }
    }

    @Nested
    @DisplayName("addSessionNotes Tests")
    class AddSessionNotesTests {

        @Test
        @DisplayName("Should add notes successfully")
        void addSessionNotes_ShouldReturnUpdated() {
            // Given
            when(seanceRepository.findById(1L)).thenReturn(Optional.of(testSeance));
            when(seanceRepository.save(any(Seance.class))).thenReturn(testSeance);

            // When
            SeanceDTO result = seanceService.addSessionNotes(1L, "Session notes", 4, 3, 5);

            // Then
            assertThat(result).isNotNull();
            verify(seanceRepository).save(any(Seance.class));
        }
    }

    @Nested
    @DisplayName("getSeancesByTherapeute Tests")
    class GetSeancesByTherapeuteTests {

        @Test
        @DisplayName("Should return therapeute seances")
        void getSeancesByTherapeute_ShouldReturnList() {
            // Given
            when(seanceRepository.findByTherapeuteId(1L)).thenReturn(Arrays.asList(testSeance));

            // When
            List<SeanceDTO> result = seanceService.getSeancesByTherapeute(1L);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getSeancesByDateRange Tests")
    class GetSeancesByDateRangeTests {

        @Test
        @DisplayName("Should return seances in date range")
        void getSeancesByDateRange_ShouldReturnList() {
            // Given
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusDays(7);
            when(seanceRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(Arrays.asList(testSeance));

            // When
            List<SeanceDTO> result = seanceService.getSeancesByDateRange(start, end);

            // Then
            assertThat(result).isNotNull();
        }
    }
}
