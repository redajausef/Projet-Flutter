package com.clinassist.controller;

import com.clinassist.dto.SeanceDTO;
import com.clinassist.entity.Seance;
import com.clinassist.service.SeanceService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeanceController Unit Tests")
class SeanceControllerTest {

    @Mock
    private SeanceService seanceService;

    @InjectMocks
    private SeanceController seanceController;

    private SeanceDTO testSeanceDTO;

    @BeforeEach
    void setUp() {
        testSeanceDTO = new SeanceDTO();
        testSeanceDTO.setId(1L);
        testSeanceDTO.setSeanceCode("SEA-001");
        testSeanceDTO.setPatientId(1L);
        testSeanceDTO.setTherapeuteId(1L);
        testSeanceDTO.setScheduledAt(LocalDateTime.now().plusDays(1));
        testSeanceDTO.setDurationMinutes(45);
        testSeanceDTO.setStatus(Seance.SeanceStatus.SCHEDULED);
        testSeanceDTO.setType(Seance.SeanceType.IN_PERSON);
    }

    @Test
    @DisplayName("GET /seances should return page of seances")
    void getAllSeances_ShouldReturnPageOfSeances() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SeanceDTO> seancePage = new PageImpl<>(Arrays.asList(testSeanceDTO), pageable, 1);
        
        when(seanceService.getAllSeances(any(Pageable.class))).thenReturn(seancePage);

        ResponseEntity<Page<SeanceDTO>> response = seanceController.getAllSeances(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("GET /seances/{id} should return seance by ID")
    void getSeanceById_ShouldReturnSeance() {
        when(seanceService.getSeanceById(1L)).thenReturn(testSeanceDTO);

        ResponseEntity<SeanceDTO> response = seanceController.getSeanceById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SEA-001", response.getBody().getSeanceCode());
    }

    @Test
    @DisplayName("GET /seances/patient/{id} should return seances by patient")
    void getSeancesByPatient_ShouldReturnSeances() {
        List<SeanceDTO> seances = Arrays.asList(testSeanceDTO);
        when(seanceService.getSeancesByPatient(1L)).thenReturn(seances);

        ResponseEntity<List<SeanceDTO>> response = seanceController.getSeancesByPatient(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("GET /seances/therapeute/{id} should return seances by therapeute")
    void getSeancesByTherapeute_ShouldReturnSeances() {
        List<SeanceDTO> seances = Arrays.asList(testSeanceDTO);
        when(seanceService.getSeancesByTherapeute(1L)).thenReturn(seances);

        ResponseEntity<List<SeanceDTO>> response = seanceController.getSeancesByTherapeute(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("PATCH /seances/{id}/status should update seance status")
    void updateStatus_ShouldReturnUpdatedSeance() {
        when(seanceService.updateSeanceStatus(1L, Seance.SeanceStatus.COMPLETED)).thenReturn(testSeanceDTO);

        ResponseEntity<SeanceDTO> response = seanceController.updateStatus(1L, Seance.SeanceStatus.COMPLETED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(seanceService, times(1)).updateSeanceStatus(1L, Seance.SeanceStatus.COMPLETED);
    }

    @Test
    @DisplayName("PATCH /seances/{id}/cancel should cancel seance")
    void cancelSeance_ShouldReturnSeance() {
        when(seanceService.cancelSeance(1L, "Patient request")).thenReturn(testSeanceDTO);

        ResponseEntity<SeanceDTO> response = seanceController.cancelSeance(1L, "Patient request");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(seanceService, times(1)).cancelSeance(1L, "Patient request");
    }

    @Test
    @DisplayName("GET /seances/upcoming should return upcoming seances")
    void getUpcomingSeances_ShouldReturnSeances() {
        List<SeanceDTO> seances = Arrays.asList(testSeanceDTO);
        when(seanceService.getUpcomingSeances()).thenReturn(seances);

        ResponseEntity<List<SeanceDTO>> response = seanceController.getUpcomingSeances();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /seances/today should return today's seances")
    void getTodaySeances_ShouldReturnSeances() {
        List<SeanceDTO> seances = Arrays.asList(testSeanceDTO);
        when(seanceService.getTodaySeances()).thenReturn(seances);

        ResponseEntity<List<SeanceDTO>> response = seanceController.getTodaySeances();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
