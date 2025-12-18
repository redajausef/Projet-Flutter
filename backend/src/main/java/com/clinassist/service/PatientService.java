package com.clinassist.service;

import com.clinassist.dto.PatientCreateRequest;
import com.clinassist.dto.PatientDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import com.clinassist.entity.User;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
import com.clinassist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final TherapeuteRepository therapeuteRepository;
    private final SeanceRepository seanceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<PatientDTO> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional
    public PatientDTO createPatient(PatientCreateRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create User entity
        String username = generateUniqueUsername(request.getEmail());
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        
        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(User.Role.PATIENT)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        // Create Patient entity
        Patient patient = Patient.builder()
                .user(user)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .emergencyContactRelation(request.getEmergencyContactRelation())
                .medicalHistory(request.getMedicalHistory())
                .currentMedications(request.getCurrentMedications())
                .allergies(request.getAllergies())
                .notes(request.getNotes())
                .insuranceProvider(request.getInsuranceProvider())
                .insuranceNumber(request.getInsuranceNumber())
                .status(request.getStatus() != null ? request.getStatus() : Patient.PatientStatus.ACTIVE)
                .build();

        // Assign therapeute if provided
        if (request.getAssignedTherapeuteId() != null) {
            Therapeute therapeute = therapeuteRepository.findById(request.getAssignedTherapeuteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Therapeute not found"));
            patient.setAssignedTherapeute(therapeute);
        }

        // Save patient (cascades to user)
        patient = patientRepository.save(patient);

        // TODO: Send email with temporary password

        return convertToDTO(patient);
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].toLowerCase();
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }

    public Page<PatientDTO> searchPatients(String search, Pageable pageable) {
        return patientRepository.searchPatients(search, pageable).map(this::convertToDTO);
    }

    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return convertToDTO(patient);
    }

    public PatientDTO getPatientByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found for user id: " + userId));
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

        // Update User fields
        User user = patient.getUser();
        if (user != null) {
            if (patientDTO.getFirstName() != null) {
                user.setFirstName(patientDTO.getFirstName());
            }
            if (patientDTO.getLastName() != null) {
                user.setLastName(patientDTO.getLastName());
            }
            if (patientDTO.getEmail() != null) {
                // Check if email is being changed and if new email already exists
                if (!user.getEmail().equals(patientDTO.getEmail()) && 
                    userRepository.existsByEmail(patientDTO.getEmail())) {
                    throw new IllegalArgumentException("Email already exists");
                }
                user.setEmail(patientDTO.getEmail());
            }
            if (patientDTO.getPhoneNumber() != null) {
                user.setPhoneNumber(patientDTO.getPhoneNumber());
            }
        }

        // Update Patient fields
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

