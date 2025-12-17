package com.clinassist.dto;

import com.clinassist.entity.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientCreateRequest {
    
    // User information
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String phoneNumber;
    
    // Patient specific information
    private LocalDate dateOfBirth;
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
}
