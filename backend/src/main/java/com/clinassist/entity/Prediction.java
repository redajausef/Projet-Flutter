package com.clinassist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private PredictionType type;

    @Column(columnDefinition = "TEXT")
    private String prediction;

    private Double confidenceScore;

    @ElementCollection
    @CollectionTable(name = "prediction_factors", joinColumns = @JoinColumn(name = "prediction_id"))
    @MapKeyColumn(name = "factor_name")
    @Column(name = "factor_value")
    @Builder.Default
    private Map<String, Double> factors = new HashMap<>();

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    private Integer riskLevel;

    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory;

    private LocalDateTime predictedForDate;

    private Boolean wasAccurate;
    private String actualOutcome;

    private String modelVersion;
    private String algorithmUsed;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum PredictionType {
        NEXT_SESSION_NEEDED,
        TREATMENT_PROGRESS,
        RISK_ASSESSMENT,
        OPTIMAL_SCHEDULE,
        SESSION_OUTCOME,
        DROPOUT_RISK,
        TREATMENT_DURATION
    }

    public enum RiskCategory {
        LOW,
        MODERATE,
        HIGH,
        CRITICAL
    }

    public String getRiskCategoryColor() {
        return switch (riskCategory) {
            case LOW -> "#4CAF50";
            case MODERATE -> "#FF9800";
            case HIGH -> "#F44336";
            case CRITICAL -> "#9C27B0";
            default -> "#9E9E9E";
        };
    }
}

