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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    private User patientUser;
    private User therapeuteUser;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setId(1L);
        patientUser.setFirstName("Jean");
        patientUser.setLastName("Dupont");

        therapeuteUser = new User();
        therapeuteUser.setId(2L);
        therapeuteUser.setFirstName("Dr Sophie");
        therapeuteUser.setLastName("Martin");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setPatientCode("PAT-001");
        testPatient.setUser(patientUser);

        testTherapeute = new Therapeute();
        testTherapeute.setId(1L);
        testTherapeute.setUser(therapeuteUser);
        testTherapeute.setSpecialty("Psychologie");

        testSeance = new Seance();
        testSeance.setId(1L);
        testSeance.setPatient(testPatient);
        testSeance.setTherapeute(testTherapeute);
        testSeance.setScheduledAt(LocalDateTime.now().plusDays(1));
        testSeance.setDurationMinutes(45);
        testSeance.setType(Seance.SeanceType.CONSULTATION);
        testSeance.setStatus(Seance.SeanceStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should return all seances with pagination")
    void getAllSeances_ShouldReturnPageOfSeances() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Seance> seances = Arrays.asList(testSeance);
        Page<Seance> seancePage = new PageImpl<>(seances, pageable, 1);
        
        when(seanceRepository.findAll(pageable)).thenReturn(seancePage);

        // Act
        Page<SeanceDTO> result = seanceService.getAllSeances(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(seanceRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return seance by ID when exists")
    void getSeanceById_WhenExists_ShouldReturnSeance() {
        // Arrange
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(testSeance));

        // Act
        SeanceDTO result = seanceService.getSeanceById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(45, result.getDurationMinutes());
        verify(seanceRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when seance not found")
    void getSeanceById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(seanceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            seanceService.getSeanceById(99L);
        });
    }

    @Test
    @DisplayName("Should return seances by patient")
    void getSeancesByPatient_ShouldReturnSeances() {
        // Arrange
        List<Seance> seances = Arrays.asList(testSeance);
        when(seanceRepository.findByPatientId(1L)).thenReturn(seances);

        // Act
        List<SeanceDTO> result = seanceService.getSeancesByPatient(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(seanceRepository, times(1)).findByPatientId(1L);
    }

    @Test
    @DisplayName("Should update seance status")
    void updateSeanceStatus_ShouldUpdateStatus() {
        // Arrange
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(testSeance));
        when(seanceRepository.save(any(Seance.class))).thenReturn(testSeance);

        // Act
        SeanceDTO result = seanceService.updateSeanceStatus(1L, Seance.SeanceStatus.COMPLETED);

        // Assert
        assertNotNull(result);
        verify(seanceRepository, times(1)).save(any(Seance.class));
    }
}
