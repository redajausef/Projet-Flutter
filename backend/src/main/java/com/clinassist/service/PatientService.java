package com.clinassist.service;

import com.clinassist.dto.PatientDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final TherapeuteRepository therapeuteRepository;
    private final SeanceRepository seanceRepository;

    public Page<PatientDTO> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::convertToDTO);
    }

    public Page<PatientDTO> searchPatients(String search, Pageable pageable) {
        return patientRepository.searchPatients(search, pageable).map(this::convertToDTO);
    }

    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return convertToDTO(patient);
    }

    public PatientDTO getPatientByCode(String code) {
        Patient patient = patientRepository.findByPatientCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with code: " + code));
        return convertToDTO(patient);
    }

    public List<PatientDTO> getPatientsByTherapeute(Long therapeuteId) {
        return patientRepository.findByAssignedTherapeuteId(therapeuteId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        // Update fields
        if (patientDTO.getDateOfBirth() != null) {
            patient.setDateOfBirth(patientDTO.getDateOfBirth());
        }
        if (patientDTO.getGender() != null) {
            patient.setGender(patientDTO.getGender());
        }
        if (patientDTO.getAddress() != null) {
            patient.setAddress(patientDTO.getAddress());
        }
        if (patientDTO.getCity() != null) {
            patient.setCity(patientDTO.getCity());
        }
        if (patientDTO.getMedicalHistory() != null) {
            patient.setMedicalHistory(patientDTO.getMedicalHistory());
        }
        if (patientDTO.getAllergies() != null) {
            patient.setAllergies(patientDTO.getAllergies());
        }
        if (patientDTO.getNotes() != null) {
            patient.setNotes(patientDTO.getNotes());
        }

        patient = patientRepository.save(patient);
        return convertToDTO(patient);
    }

    @Transactional
    public PatientDTO assignTherapeute(Long patientId, Long therapeuteId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Therapeute therapeute = therapeuteRepository.findById(therapeuteId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapeute not found"));

        patient.setAssignedTherapeute(therapeute);
        patient = patientRepository.save(patient);

        return convertToDTO(patient);
    }

    @Transactional
    public PatientDTO updatePatientStatus(Long id, Patient.PatientStatus status) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patient.setStatus(status);
        patient = patientRepository.save(patient);

        return convertToDTO(patient);
    }

    public List<PatientDTO> getHighRiskPatients(Integer minRisk) {
        return patientRepository.findHighRiskPatients(minRisk)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Long countByStatus(Patient.PatientStatus status) {
        return patientRepository.countByStatus(status);
    }

    private PatientDTO convertToDTO(Patient patient) {
        // Get upcoming seance
        List<Seance> upcomingSeances = seanceRepository.findUpcomingSeancesByPatient(
                patient.getId(), LocalDateTime.now());
        LocalDateTime nextSeance = upcomingSeances.isEmpty() ? null : 
                upcomingSeances.get(0).getScheduledAt();

        // Count seances
        List<Seance> allSeances = seanceRepository.findByPatientId(patient.getId());
        int totalSeances = allSeances.size();
        int completedSeances = (int) allSeances.stream()
                .filter(s -> s.getStatus() == Seance.SeanceStatus.COMPLETED)
                .count();

        return PatientDTO.builder()
                .id(patient.getId())
                .patientCode(patient.getPatientCode())
                .userId(patient.getUser().getId())
                .username(patient.getUser().getUsername())
                .email(patient.getUser().getEmail())
                .firstName(patient.getUser().getFirstName())
                .lastName(patient.getUser().getLastName())
                .fullName(patient.getUser().getFullName())
                .phoneNumber(patient.getUser().getPhoneNumber())
                .profileImageUrl(patient.getUser().getProfileImageUrl())
                .dateOfBirth(patient.getDateOfBirth())
                .age(patient.getAge())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .city(patient.getCity())
                .postalCode(patient.getPostalCode())
                .country(patient.getCountry())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .emergencyContactRelation(patient.getEmergencyContactRelation())
                .medicalHistory(patient.getMedicalHistory())
                .currentMedications(patient.getCurrentMedications())
                .allergies(patient.getAllergies())
                .notes(patient.getNotes())
                .insuranceProvider(patient.getInsuranceProvider())
                .insuranceNumber(patient.getInsuranceNumber())
                .status(patient.getStatus())
                .assignedTherapeuteId(patient.getAssignedTherapeute() != null ? 
                        patient.getAssignedTherapeute().getId() : null)
                .assignedTherapeuteName(patient.getAssignedTherapeute() != null ? 
                        patient.getAssignedTherapeute().getUser().getFullName() : null)
                .riskScore(patient.getRiskScore())
                .riskCategory(patient.getRiskCategory())
                .totalSeances(totalSeances)
                .completedSeances(completedSeances)
                .nextSeanceAt(nextSeance)
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}

