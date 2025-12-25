package com.clinassist.service;

import com.clinassist.dto.TherapeuteDTO;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import com.clinassist.entity.User;
import com.clinassist.exception.ResourceNotFoundException;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour TherapeuteService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TherapeuteService Unit Tests")
class TherapeuteServiceTest {

    @Mock
    private TherapeuteRepository therapeuteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SeanceRepository seanceRepository;

    @InjectMocks
    private TherapeuteService therapeuteService;

    private Therapeute testTherapeute;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("dr.martin")
                .email("dr.martin@clinic.com")
                .firstName("Jean")
                .lastName("Martin")
                .role(User.Role.THERAPEUTE)
                .isActive(true)
                .build();

        testTherapeute = Therapeute.builder()
                .id(1L)
                .therapeuteCode("THER-001")
                .user(testUser)
                .specialization("Psychologie")
                .licenseNumber("PSY-12345")
                .yearsOfExperience(10)
                .status(Therapeute.TherapeuteStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("getAllTherapeutes Tests")
    class GetAllTherapeutesTests {

        @Test
        @DisplayName("Should return paginated therapeutes")
        void getAllTherapeutes_ShouldReturnPage() {
            // Given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Therapeute> therapeutePage = new PageImpl<>(Arrays.asList(testTherapeute), pageable, 1);
            when(therapeuteRepository.findAll(pageable)).thenReturn(therapeutePage);
            when(seanceRepository.findByTherapeuteIdAndScheduledAtBetween(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.findByTherapeuteIdAndScheduledAtAfter(any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            Page<TherapeuteDTO> result = therapeuteService.getAllTherapeutes(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(therapeuteRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no therapeutes")
        void getAllTherapeutes_ShouldReturnEmptyPage() {
            // Given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Therapeute> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(therapeuteRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<TherapeuteDTO> result = therapeuteService.getAllTherapeutes(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTherapeuteById Tests")
    class GetTherapeuteByIdTests {

        @Test
        @DisplayName("Should return therapeute when found")
        void getTherapeuteById_ShouldReturnTherapeute() {
            // Given
            when(therapeuteRepository.findById(1L)).thenReturn(Optional.of(testTherapeute));
            when(seanceRepository.findByTherapeuteIdAndScheduledAtBetween(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.findByTherapeuteIdAndScheduledAtAfter(any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            TherapeuteDTO result = therapeuteService.getTherapeuteById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSpecialization()).isEqualTo("Psychologie");
        }

        @Test
        @DisplayName("Should throw exception when therapeute not found")
        void getTherapeuteById_ShouldThrowWhenNotFound() {
            // Given
            when(therapeuteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> therapeuteService.getTherapeuteById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Therapeute not found");
        }
    }

    @Nested
    @DisplayName("getTherapeuteByUserId Tests")
    class GetTherapeuteByUserIdTests {

        @Test
        @DisplayName("Should return therapeute by user id")
        void getTherapeuteByUserId_ShouldReturnTherapeute() {
            // Given
            when(therapeuteRepository.findByUserId(1L)).thenReturn(Optional.of(testTherapeute));
            when(seanceRepository.findByTherapeuteIdAndScheduledAtBetween(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.findByTherapeuteIdAndScheduledAtAfter(any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            TherapeuteDTO result = therapeuteService.getTherapeuteByUserId(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getTherapeuteByUserId_ShouldThrowWhenNotFound() {
            // Given
            when(therapeuteRepository.findByUserId(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> therapeuteService.getTherapeuteByUserId(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getTherapeuteByUsername Tests")
    class GetTherapeuteByUsernameTests {

        @Test
        @DisplayName("Should return therapeute by username")
        void getTherapeuteByUsername_ShouldReturnTherapeute() {
            // Given
            when(userRepository.findByUsername("dr.martin")).thenReturn(Optional.of(testUser));
            when(therapeuteRepository.findByUserId(1L)).thenReturn(Optional.of(testTherapeute));
            when(seanceRepository.findByTherapeuteIdAndScheduledAtBetween(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.findByTherapeuteIdAndScheduledAtAfter(any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            TherapeuteDTO result = therapeuteService.getTherapeuteByUsername("dr.martin");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("dr.martin");
        }

        @Test
        @DisplayName("Should throw exception when username not found")
        void getTherapeuteByUsername_ShouldThrowWhenNotFound() {
            // Given
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> therapeuteService.getTherapeuteByUsername("unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getAvailableTherapeutes Tests")
    class GetAvailableTherapeutesTests {

        @Test
        @DisplayName("Should return available therapeutes")
        void getAvailableTherapeutes_ShouldReturnList() {
            // Given
            when(therapeuteRepository.findAvailableTherapeutes())
                    .thenReturn(Arrays.asList(testTherapeute));
            when(seanceRepository.findByTherapeuteIdAndScheduledAtBetween(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.findByTherapeuteIdAndScheduledAtAfter(any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            List<TherapeuteDTO> result = therapeuteService.getAvailableTherapeutes();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(Therapeute.TherapeuteStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Should return empty list when no available therapeutes")
        void getAvailableTherapeutes_ShouldReturnEmptyList() {
            // Given
            when(therapeuteRepository.findAvailableTherapeutes()).thenReturn(Collections.emptyList());

            // When
            List<TherapeuteDTO> result = therapeuteService.getAvailableTherapeutes();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchTherapeutes Tests")
    class SearchTherapeutesTests {

        @Test
        @DisplayName("Should search therapeutes by term")
        void searchTherapeutes_ShouldReturnMatchingTherapeutes() {
            // Given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Therapeute> therapeutePage = new PageImpl<>(Arrays.asList(testTherapeute), pageable, 1);
            when(therapeuteRepository.searchTherapeutes("martin", pageable)).thenReturn(therapeutePage);
            when(seanceRepository.findByTherapeuteIdAndScheduledAtBetween(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.findByTherapeuteIdAndScheduledAtAfter(any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            Page<TherapeuteDTO> result = therapeuteService.searchTherapeutes("martin", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getLastName()).isEqualTo("Martin");
        }
    }

    @Nested
    @DisplayName("updateAvailability Tests")
    class UpdateAvailabilityTests {

        @Test
        @DisplayName("Should update therapeute to available")
        void updateAvailability_ShouldSetAvailable() {
            // Given
            testTherapeute.setStatus(Therapeute.TherapeuteStatus.BUSY);
            when(therapeuteRepository.findById(1L)).thenReturn(Optional.of(testTherapeute));
            when(therapeuteRepository.save(any(Therapeute.class))).thenReturn(testTherapeute);
            when(seanceRepository.findByTherapeuteIdAndScheduledAtBetween(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.findByTherapeuteIdAndScheduledAtAfter(any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            TherapeuteDTO result = therapeuteService.updateAvailability(1L, true);

            // Then
            assertThat(result).isNotNull();
            verify(therapeuteRepository).save(any(Therapeute.class));
        }

        @Test
        @DisplayName("Should update therapeute to busy")
        void updateAvailability_ShouldSetBusy() {
            // Given
            when(therapeuteRepository.findById(1L)).thenReturn(Optional.of(testTherapeute));
            when(therapeuteRepository.save(any(Therapeute.class))).thenReturn(testTherapeute);
            when(seanceRepository.findByTherapeuteIdAndScheduledAtBetween(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(seanceRepository.findByTherapeuteIdAndScheduledAtAfter(any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            TherapeuteDTO result = therapeuteService.updateAvailability(1L, false);

            // Then
            assertThat(result).isNotNull();
            verify(therapeuteRepository).save(any(Therapeute.class));
        }

        @Test
        @DisplayName("Should throw exception when therapeute not found")
        void updateAvailability_ShouldThrowWhenNotFound() {
            // Given
            when(therapeuteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> therapeuteService.updateAvailability(999L, true))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
