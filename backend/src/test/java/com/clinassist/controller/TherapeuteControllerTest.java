package com.clinassist.controller;

import com.clinassist.dto.TherapeuteDTO;
import com.clinassist.entity.Therapeute;
import com.clinassist.service.TherapeuteService;
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
@DisplayName("TherapeuteController Unit Tests")
class TherapeuteControllerTest {

    @Mock
    private TherapeuteService therapeuteService;

    @InjectMocks
    private TherapeuteController therapeuteController;

    private TherapeuteDTO testTherapeuteDTO;

    @BeforeEach
    void setUp() {
        testTherapeuteDTO = new TherapeuteDTO();
        testTherapeuteDTO.setId(1L);
        testTherapeuteDTO.setTherapeuteCode("TH-001");
        testTherapeuteDTO.setFirstName("Dr Sophie");
        testTherapeuteDTO.setLastName("Martin");
        testTherapeuteDTO.setSpecialization("Psychologie");
        testTherapeuteDTO.setStatus(Therapeute.TherapeuteStatus.AVAILABLE);
    }

    @Test
    @DisplayName("GET /therapeutes should return page of therapeutes")
    void getAllTherapeutes_ShouldReturnPageOfTherapeutes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TherapeuteDTO> therapeutePage = new PageImpl<>(Arrays.asList(testTherapeuteDTO), pageable, 1);
        
        when(therapeuteService.getAllTherapeutes(any(Pageable.class))).thenReturn(therapeutePage);

        ResponseEntity<Page<TherapeuteDTO>> response = therapeuteController.getAllTherapeutes(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("GET /therapeutes/{id} should return therapeute by ID")
    void getTherapeuteById_ShouldReturnTherapeute() {
        when(therapeuteService.getTherapeuteById(1L)).thenReturn(testTherapeuteDTO);

        ResponseEntity<TherapeuteDTO> response = therapeuteController.getTherapeuteById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TH-001", response.getBody().getTherapeuteCode());
    }

    @Test
    @DisplayName("GET /therapeutes/available should return available therapeutes")
    void getAvailableTherapeutes_ShouldReturnTherapeutes() {
        List<TherapeuteDTO> therapeutes = Arrays.asList(testTherapeuteDTO);
        when(therapeuteService.getAvailableTherapeutes()).thenReturn(therapeutes);

        ResponseEntity<List<TherapeuteDTO>> response = therapeuteController.getAvailableTherapeutes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("PUT /therapeutes/{id}/status should update therapeute status")
    void updateTherapeuteStatus_ShouldReturnUpdatedTherapeute() {
        when(therapeuteService.updateTherapeuteStatus(1L, Therapeute.TherapeuteStatus.BUSY))
            .thenReturn(testTherapeuteDTO);

        ResponseEntity<TherapeuteDTO> response = therapeuteController.updateTherapeuteStatus(
            1L, Therapeute.TherapeuteStatus.BUSY);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(therapeuteService, times(1)).updateTherapeuteStatus(1L, Therapeute.TherapeuteStatus.BUSY);
    }

    @Test
    @DisplayName("GET /therapeutes/user/{userId} should return therapeute by user ID")
    void getTherapeuteByUserId_ShouldReturnTherapeute() {
        when(therapeuteService.getTherapeuteByUserId(1L)).thenReturn(testTherapeuteDTO);

        ResponseEntity<TherapeuteDTO> response = therapeuteController.getTherapeuteByUserId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }
}
