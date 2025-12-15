package com.clinassist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    private Long totalPatients;
    private Long activePatients;
    private Long newPatientsThisMonth;
    private Double patientGrowthPercentage;
    
    private Long totalTherapeutes;
    private Long availableTherapeutes;
    
    private Long totalSeances;
    private Long todaySeances;
    private Long upcomingSeances;
    private Long completedSeancesThisMonth;
    private Double seanceCompletionRate;
    
    private Long highRiskPatients;
    private Double averageRiskScore;
    
    private Double predictionAccuracy;
    private Long totalPredictions;
    
    private List<SeanceDTO> upcomingSeancesList;
    private List<PatientDTO> recentPatients;
    private List<PredictionDTO> recentPredictions;
    
    private Map<String, Long> seancesByType;
    private Map<String, Long> patientsByStatus;
    private List<ChartDataPoint> seancesTrend;
    private List<ChartDataPoint> patientsTrend;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataPoint {
        private String label;
        private Long value;
        private String color;
    }
}

