package com.clinassist.dto;

import com.clinassist.entity.Therapeute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapeuteDTO {
    
    private Long id;
    private String therapeuteCode;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String profileImageUrl;
    
    private String licenseNumber;
    private String specialization;
    private String qualifications;
    private String biography;
    private Integer yearsOfExperience;
    
    private List<String> specialties;
    private List<String> languages;
    
    private Therapeute.TherapeuteStatus status;
    
    private Double consultationFee;
    private String currency;
    
    private Double rating;
    private Integer totalReviews;
    
    private Integer totalPatients;
    private Integer todaySeances;
    private Integer upcomingSeances;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

