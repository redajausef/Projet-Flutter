package com.clinassist.dto;

import com.clinassist.entity.Prediction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDTO {
    
    private Long id;
    
    private Long patientId;
    private String patientName;
    private String patientCode;
    
    private Prediction.PredictionType type;
    private String prediction;
    private Double confidenceScore;
    
    private Map<String, Double> factors;
    private String recommendations;  // For therapeute
    private String patientRecommendations;  // For patient
    
    private Integer riskLevel;
    private Prediction.RiskCategory riskCategory;
    private String riskCategoryColor;
    
    private LocalDateTime predictedForDate;
    
    private Boolean wasAccurate;
    private String actualOutcome;
    
    private String modelVersion;
    private String algorithmUsed;
    
    private LocalDateTime createdAt;
}

