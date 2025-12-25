package com.clinassist.service;

import com.clinassist.dto.TherapeuteDTO;
import com.clinassist.entity.Therapeute;
import com.clinassist.entity.User;
import com.clinassist.exception.ResourceNotFoundException;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TherapeuteService Unit Tests")
class TherapeuteServiceTest {

    @Mock
    private TherapeuteRepository therapeuteRepository;

    @InjectMocks
    private TherapeuteService therapeuteService;

    private Therapeute testTherapeute;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Dr Sophie");
        testUser.setLastName("Martin");
        testUser.setEmail("sophie.martin@clinassist.com");

        testTherapeute = new Therapeute();
        testTherapeute.setId(1L);
        testTherapeute.setTherapeuteCode("TH-001");
        testTherapeute.setUser(testUser);
        testTherapeute.setSpecialization("Psychologie");
        testTherapeute.setYearsOfExperience(10);
        testTherapeute.setStatus(Therapeute.TherapeuteStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should return all therapeutes with pagination")
    void getAllTherapeutes_ShouldReturnPageOfTherapeutes() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Therapeute> therapeutes = Arrays.asList(testTherapeute);
        Page<Therapeute> therapeutePage = new PageImpl<>(therapeutes, pageable, 1);
        
        when(therapeuteRepository.findAll(pageable)).thenReturn(therapeutePage);

        Page<TherapeuteDTO> result = therapeuteService.getAllTherapeutes(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(therapeuteRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return therapeute by ID when exists")
    void getTherapeuteById_WhenExists_ShouldReturnTherapeute() {
        when(therapeuteRepository.findById(1L)).thenReturn(Optional.of(testTherapeute));

        TherapeuteDTO result = therapeuteService.getTherapeuteById(1L);

        assertNotNull(result);
        assertEquals("TH-001", result.getTherapeuteCode());
        verify(therapeuteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when therapeute not found")
    void getTherapeuteById_WhenNotExists_ShouldThrowException() {
        when(therapeuteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            therapeuteService.getTherapeuteById(99L);
        });
    }

    @Test
    @DisplayName("Should return therapeute by user ID")
    void getTherapeuteByUserId_WhenExists_ShouldReturnTherapeute() {
        when(therapeuteRepository.findByUserId(1L)).thenReturn(Optional.of(testTherapeute));

        TherapeuteDTO result = therapeuteService.getTherapeuteByUserId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should return available therapeutes")
    void getAvailableTherapeutes_ShouldReturnTherapeutes() {
        List<Therapeute> therapeutes = Arrays.asList(testTherapeute);
        when(therapeuteRepository.findByStatus(Therapeute.TherapeuteStatus.AVAILABLE)).thenReturn(therapeutes);

        List<TherapeuteDTO> result = therapeuteService.getAvailableTherapeutes();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should update therapeute status")
    void updateTherapeuteStatus_ShouldUpdateStatus() {
        when(therapeuteRepository.findById(1L)).thenReturn(Optional.of(testTherapeute));
        when(therapeuteRepository.save(any(Therapeute.class))).thenReturn(testTherapeute);

        TherapeuteDTO result = therapeuteService.updateTherapeuteStatus(1L, Therapeute.TherapeuteStatus.BUSY);

        assertNotNull(result);
        verify(therapeuteRepository, times(1)).save(any(Therapeute.class));
    }
}
