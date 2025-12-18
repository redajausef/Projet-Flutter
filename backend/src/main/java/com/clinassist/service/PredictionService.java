package com.clinassist.service;

import com.clinassist.dto.PredictionDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Prediction;
import com.clinassist.entity.Seance;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.PredictionRepository;
import com.clinassist.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final PatientRepository patientRepository;
    private final SeanceRepository seanceRepository;
    // ML integration handled via standalone Flask microservice on port 5001

    public List<PredictionDTO> getPatientPredictions(Long patientId) {
        return predictionRepository.findByPatientId(patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PredictionDTO> getLatestPredictions(Long patientId, int limit) {
        return predictionRepository.findLatestPredictions(patientId, PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PredictionDTO> getHighRiskPredictions(Integer minRisk) {
        return predictionRepository.findHighRiskPredictions(minRisk)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PredictionDTO generateNextSessionPrediction(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Get patient's seance history
        List<Seance> completedSeances = seanceRepository.findCompletedSeancesByPatient(patientId);
        
        // Calculate prediction factors
        Map<String, Double> factors = calculatePredictionFactors(patient, completedSeances);
        
        // Generate prediction
        Prediction prediction = generateSessionPrediction(patient, factors, completedSeances);
        prediction = predictionRepository.save(prediction);

        // Update patient risk score
        updatePatientRiskScore(patient, prediction);

        return convertToDTO(prediction);
    }

    @Transactional
    public PredictionDTO generateDropoutRiskPrediction(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        List<Seance> seances = seanceRepository.findByPatientId(patientId);
        
        Map<String, Double> factors = new HashMap<>();
        
        // Calculate dropout risk factors
        long cancelledCount = seances.stream()
                .filter(s -> s.getStatus() == Seance.SeanceStatus.CANCELLED)
                .count();
        long noShowCount = seances.stream()
                .filter(s -> s.getStatus() == Seance.SeanceStatus.NO_SHOW)
                .count();
        
        factors.put("cancellation_rate", seances.isEmpty() ? 0.0 : 
                (double) cancelledCount / seances.size());
        factors.put("no_show_rate", seances.isEmpty() ? 0.0 : 
                (double) noShowCount / seances.size());
        
        // Calculate days since last session
        List<Seance> completed = seances.stream()
                .filter(s -> s.getStatus() == Seance.SeanceStatus.COMPLETED)
                .sorted((a, b) -> b.getScheduledAt().compareTo(a.getScheduledAt()))
                .collect(Collectors.toList());
        
        long daysSinceLastSession = completed.isEmpty() ? 30 :
                ChronoUnit.DAYS.between(completed.get(0).getScheduledAt(), LocalDateTime.now());
        factors.put("days_since_last_session", (double) daysSinceLastSession);
        
        // Calculate risk score using heuristic (0-100)
        // ML service available on port 5001 for advanced predictions
        double riskScore = calculateDropoutRisk(factors);
        int riskLevel = (int) riskScore;
        
        Prediction.RiskCategory riskCategory;
        if (riskScore < 25) riskCategory = Prediction.RiskCategory.LOW;
        else if (riskScore < 50) riskCategory = Prediction.RiskCategory.MODERATE;
        else if (riskScore < 75) riskCategory = Prediction.RiskCategory.HIGH;
        else riskCategory = Prediction.RiskCategory.CRITICAL;

        Prediction prediction = Prediction.builder()
                .patient(patient)
                .type(Prediction.PredictionType.DROPOUT_RISK)
                .prediction("Patient dropout risk assessment")
                .confidenceScore(0.85)
                .factors(factors)
                .riskLevel(riskLevel)
                .riskCategory(riskCategory)
                .recommendations(generateDropoutRecommendations(riskCategory))
                .modelVersion("1.0.0")
                .algorithmUsed("HeuristicAnalyzer")
                .build();

        prediction = predictionRepository.save(prediction);
        return convertToDTO(prediction);
    }

    @Transactional
    public PredictionDTO generateTreatmentProgressPrediction(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        List<Seance> completedSeances = seanceRepository.findCompletedSeancesByPatient(patientId);
        Double avgProgress = seanceRepository.getAverageProgressRating(patientId);
        
        Map<String, Double> factors = new HashMap<>();
        factors.put("total_sessions", (double) completedSeances.size());
        factors.put("average_progress", avgProgress != null ? avgProgress : 0.0);
        
        // Calculate mood improvement
        if (!completedSeances.isEmpty()) {
            double moodImprovement = completedSeances.stream()
                    .filter(s -> s.getPatientMoodBefore() != null && s.getPatientMoodAfter() != null)
                    .mapToDouble(s -> s.getPatientMoodAfter() - s.getPatientMoodBefore())
                    .average()
                    .orElse(0.0);
            factors.put("mood_improvement", moodImprovement);
        }

        double progressScore = calculateProgressScore(factors);
        
        String progressCategory;
        if (progressScore >= 80) progressCategory = "Excellent";
        else if (progressScore >= 60) progressCategory = "Good";
        else if (progressScore >= 40) progressCategory = "Moderate";
        else progressCategory = "Needs Attention";

        Prediction prediction = Prediction.builder()
                .patient(patient)
                .type(Prediction.PredictionType.TREATMENT_PROGRESS)
                .prediction("Treatment progress: " + progressCategory)
                .confidenceScore(0.78)
                .factors(factors)
                .riskLevel((int) (100 - progressScore))
                .recommendations(generateProgressRecommendations(progressScore))
                .modelVersion("1.0.0")
                .algorithmUsed("ProgressAnalyzer")
                .build();

        prediction = predictionRepository.save(prediction);
        return convertToDTO(prediction);
    }

    private Map<String, Double> calculatePredictionFactors(Patient patient, List<Seance> seances) {
        Map<String, Double> factors = new HashMap<>();
        
        factors.put("total_sessions", (double) seances.size());
        factors.put("age", (double) patient.getAge());
        
        // Average session frequency (days between sessions)
        if (seances.size() >= 2) {
            double avgDaysBetween = 0;
            for (int i = 1; i < seances.size(); i++) {
                avgDaysBetween += ChronoUnit.DAYS.between(
                        seances.get(i).getScheduledAt(),
                        seances.get(i - 1).getScheduledAt()
                );
            }
            factors.put("avg_days_between_sessions", avgDaysBetween / (seances.size() - 1));
        }
        
        return factors;
    }

    private Prediction generateSessionPrediction(Patient patient, Map<String, Double> factors,
                                                   List<Seance> completedSeances) {
        // Simple prediction logic (in production, use ML model)
        int recommendedDays = 7; // Default weekly
        
        if (factors.containsKey("avg_days_between_sessions")) {
            recommendedDays = (int) Math.round(factors.get("avg_days_between_sessions"));
        }
        
        LocalDateTime predictedDate = LocalDateTime.now().plusDays(recommendedDays);
        
        return Prediction.builder()
                .patient(patient)
                .type(Prediction.PredictionType.NEXT_SESSION_NEEDED)
                .prediction("Next session recommended in " + recommendedDays + " days")
                .confidenceScore(0.82)
                .factors(factors)
                .predictedForDate(predictedDate)
                .recommendations("Schedule next session for optimal treatment continuity")
                .modelVersion("1.0.0")
                .algorithmUsed("SessionScheduler")
                .build();
    }

    private double calculateDropoutRisk(Map<String, Double> factors) {
        double risk = 0;
        
        risk += factors.getOrDefault("cancellation_rate", 0.0) * 30;
        risk += factors.getOrDefault("no_show_rate", 0.0) * 40;
        risk += Math.min(factors.getOrDefault("days_since_last_session", 0.0) / 30 * 30, 30);
        
        return Math.min(100, risk);
    }

    private double calculateProgressScore(Map<String, Double> factors) {
        double score = 50; // Base score
        
        score += factors.getOrDefault("average_progress", 0.0) * 5;
        score += factors.getOrDefault("mood_improvement", 0.0) * 10;
        score += Math.min(factors.getOrDefault("total_sessions", 0.0) * 2, 20);
        
        return Math.min(100, Math.max(0, score));
    }

    private String generateDropoutRecommendations(Prediction.RiskCategory category) {
        return switch (category) {
            case LOW -> "Continue current treatment plan. Patient shows good engagement.";
            case MODERATE -> "Consider scheduling a follow-up call. Review treatment goals with patient.";
            case HIGH -> "Urgent: Contact patient immediately. Consider adjusting treatment approach.";
            case CRITICAL -> "Critical: Immediate intervention required. Schedule emergency consultation.";
        };
    }

    private String generateProgressRecommendations(double score) {
        if (score >= 80) {
            return "Excellent progress! Consider transitioning to maintenance phase.";
        } else if (score >= 60) {
            return "Good progress. Continue current treatment approach.";
        } else if (score >= 40) {
            return "Moderate progress. Consider adjusting treatment intensity or approach.";
        } else {
            return "Progress needs attention. Comprehensive treatment review recommended.";
        }
    }

    private void updatePatientRiskScore(Patient patient, Prediction prediction) {
        if (prediction.getRiskLevel() != null) {
            patient.setRiskScore(prediction.getRiskLevel());
            if (prediction.getRiskCategory() != null) {
                patient.setRiskCategory(prediction.getRiskCategory().name());
            }
            patientRepository.save(patient);
        }
    }

    private PredictionDTO convertToDTO(Prediction prediction) {
        return PredictionDTO.builder()
                .id(prediction.getId())
                .patientId(prediction.getPatient().getId())
                .patientName(prediction.getPatient().getUser().getFullName())
                .patientCode(prediction.getPatient().getPatientCode())
                .type(prediction.getType())
                .prediction(prediction.getPrediction())
                .confidenceScore(prediction.getConfidenceScore())
                .factors(prediction.getFactors())
                .recommendations(prediction.getRecommendations())
                .riskLevel(prediction.getRiskLevel())
                .riskCategory(prediction.getRiskCategory())
                .riskCategoryColor(prediction.getRiskCategoryColor())
                .predictedForDate(prediction.getPredictedForDate())
                .wasAccurate(prediction.getWasAccurate())
                .actualOutcome(prediction.getActualOutcome())
                .modelVersion(prediction.getModelVersion())
                .algorithmUsed(prediction.getAlgorithmUsed())
                .createdAt(prediction.getCreatedAt())
                .build();
    }
}

