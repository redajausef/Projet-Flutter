package com.clinassist.service;

import com.clinassist.dto.TherapeuteDTO;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import com.clinassist.entity.User;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
import com.clinassist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TherapeuteService {

    private final TherapeuteRepository therapeuteRepository;
    private final UserRepository userRepository;
    private final SeanceRepository seanceRepository;

    public Page<TherapeuteDTO> getAllTherapeutes(Pageable pageable) {
        return therapeuteRepository.findAll(pageable).map(this::convertToDTO);
    }

    public TherapeuteDTO getTherapeuteById(Long id) {
        Therapeute therapeute = therapeuteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Therapeute not found with id: " + id));
        return convertToDTO(therapeute);
    }

    public TherapeuteDTO getTherapeuteByUserId(Long userId) {
        Therapeute therapeute = therapeuteRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapeute not found for user id: " + userId));
        return convertToDTO(therapeute);
    }

    public TherapeuteDTO getTherapeuteByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return getTherapeuteByUserId(user.getId());
    }

    public List<TherapeuteDTO> getAvailableTherapeutes() {
        return therapeuteRepository.findAvailableTherapeutes()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TherapeuteDTO> searchTherapeutes(String search, Pageable pageable) {
        return therapeuteRepository.searchTherapeutes(search, pageable).map(this::convertToDTO);
    }

    @Transactional
    public TherapeuteDTO updateAvailability(Long id, boolean available) {
        Therapeute therapeute = therapeuteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Therapeute not found with id: " + id));
        
        therapeute.setStatus(available ? 
                Therapeute.TherapeuteStatus.AVAILABLE : 
                Therapeute.TherapeuteStatus.BUSY);
        
        therapeuteRepository.save(therapeute);
        return convertToDTO(therapeute);
    }

    private TherapeuteDTO convertToDTO(Therapeute therapeute) {
        User user = therapeute.getUser();
        
        // Calculate today's seances
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        int todaySeances = seanceRepository.findByTherapeuteIdAndScheduledAtBetween(
                therapeute.getId(), startOfDay, endOfDay).size();
        
        // Calculate upcoming seances
        List<Seance> upcoming = seanceRepository.findByTherapeuteIdAndScheduledAtAfter(
                therapeute.getId(), LocalDateTime.now());
        
        return TherapeuteDTO.builder()
                .id(therapeute.getId())
                .therapeuteCode(therapeute.getTherapeuteCode())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .fullName(user != null ? user.getFirstName() + " " + user.getLastName() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .profileImageUrl(user != null ? user.getProfileImageUrl() : null)
                .licenseNumber(therapeute.getLicenseNumber())
                .specialization(therapeute.getSpecialization())
                .qualifications(therapeute.getQualifications())
                .biography(therapeute.getBiography())
                .yearsOfExperience(therapeute.getYearsOfExperience())
                .specialties(therapeute.getSpecialties())
                .languages(therapeute.getLanguages())
                .status(therapeute.getStatus())
                .consultationFee(therapeute.getConsultationFee())
                .currency(therapeute.getCurrency())
                .rating(therapeute.getRating())
                .totalReviews(therapeute.getTotalReviews())
                .totalPatients(therapeute.getPatients() != null ? therapeute.getPatients().size() : 0)
                .todaySeances(todaySeances)
                .upcomingSeances(upcoming.size())
                .createdAt(therapeute.getCreatedAt())
                .updatedAt(therapeute.getUpdatedAt())
                .build();
    }
}

