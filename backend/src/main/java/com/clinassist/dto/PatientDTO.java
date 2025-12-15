package com.clinassist.dto;

import com.clinassist.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    
    private Long id;
    private String patientCode;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String profileImageUrl;
    
    private LocalDate dateOfBirth;
    private Integer age;
    private Patient.Gender gender;
    
    private String address;
    private String city;
    private String postalCode;
    private String country;
    
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    
    private String medicalHistory;
    private String currentMedications;
    private String allergies;
    private String notes;
    
    private String insuranceProvider;
    private String insuranceNumber;
    
    private Patient.PatientStatus status;
    
    private Long assignedTherapeuteId;
    private String assignedTherapeuteName;
    
    private Integer riskScore;
    private String riskCategory;
    
    private Integer totalSeances;
    private Integer completedSeances;
    private LocalDateTime nextSeanceAt;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

