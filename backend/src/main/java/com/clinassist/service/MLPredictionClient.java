package com.clinassist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for calling the Python ML Microservice
 * Provides real scikit-learn predictions for:
 * - Dropout Risk (Random Forest)
 * - Treatment Progress (Gradient Boosting)
 * - Next Session Scheduling (Linear Regression)
 */
@Service
@Slf4j
public class MLPredictionClient {
    
    // Constants for ML request/response keys (SonarQube fix)
    private static final String KEY_CANCELLATION_RATE = "cancellation_rate";
    private static final String KEY_NO_SHOW_RATE = "no_show_rate";
    private static final String KEY_DAYS_SINCE_LAST = "days_since_last_session";
    private static final String KEY_TOTAL_SESSIONS = "total_sessions";
    private static final String KEY_AVG_MOOD_SCORE = "avg_mood_score";
    private static final String KEY_AGE = "age";
    private static final String DEFAULT_MODEL_VERSION = "1.0.0";
    private static final String DEFAULT_ML_URL = "http://ml-service:5000";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String mlServiceUrl;
    
    public MLPredictionClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // Read ML service URL from environment variable (set in docker-compose.yml)
        String envUrl = System.getenv("ML_SERVICE_URL");
        this.mlServiceUrl = (envUrl != null && !envUrl.isEmpty()) 
                ? envUrl 
                : DEFAULT_ML_URL;
        log.info("ML Service URL configured: {}", this.mlServiceUrl);
    }
    
    /**
     * Predict dropout risk using ML model (Random Forest)
     */
    public MLPredictionResult predictDropoutRisk(
            double cancellationRate,
            double noShowRate,
            int daysSinceLastSession,
            int totalSessions,
            double avgMoodScore,
            int age) {
        
        try {
            Map<String, Object> request = new HashMap<>();
            request.put(KEY_CANCELLATION_RATE, cancellationRate);
            request.put(KEY_NO_SHOW_RATE, noShowRate);
            request.put(KEY_DAYS_SINCE_LAST, daysSinceLastSession);
            request.put(KEY_TOTAL_SESSIONS, totalSessions);
            request.put(KEY_AVG_MOOD_SCORE, avgMoodScore);
            request.put(KEY_AGE, age);
            
            String response = callMLService("/api/predict/dropout-risk", request);
            JsonNode json = objectMapper.readTree(response);
            
            MLPredictionResult result = new MLPredictionResult();
            result.setScore(json.get("risk_score").asDouble());
            result.setCategory(json.get("risk_category").asText());
            result.setConfidence(json.get("confidence").asDouble());
            result.setAlgorithm(json.get("algorithm").asText());
            result.setModelVersion(json.get("model_version").asText());
            
            // Extract factors
            JsonNode factors = json.get("factors");
            Map<String, Double> factorMap = new HashMap<>();
            if (factors != null) {
                factors.fieldNames().forEachRemaining(name -> 
                    factorMap.put(name, factors.get(name).asDouble())
                );
            }
            result.setFactors(factorMap);
            
            log.info("ML Dropout Risk Prediction: score={}, category={}, algorithm={}", 
                    result.getScore(), result.getCategory(), result.getAlgorithm());
            
            return result;
            
        } catch (Exception e) {
            log.warn("ML service unavailable, using fallback heuristics: {}", e.getMessage());
            return calculateFallbackDropoutRisk(cancellationRate, noShowRate, daysSinceLastSession);
        }
    }
    
    /**
     * Predict treatment progress using ML model (Gradient Boosting)
     */
    public MLPredictionResult predictTreatmentProgress(
            int totalSessions,
            double avgProgressRating,
            double moodImprovement,
            double sessionCompletionRate) {
        
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("total_sessions", totalSessions);
            request.put("avg_progress_rating", avgProgressRating);
            request.put("mood_improvement", moodImprovement);
            request.put("session_completion_rate", sessionCompletionRate);
            
            String response = callMLService("/api/predict/treatment-progress", request);
            JsonNode json = objectMapper.readTree(response);
            
            MLPredictionResult result = new MLPredictionResult();
            result.setScore(json.get("progress_score").asDouble());
            result.setCategory(json.get("progress_category").asText());
            result.setConfidence(json.get("confidence").asDouble());
            result.setAlgorithm(json.get("algorithm").asText());
            result.setRecommendation(json.get("recommendations").asText());
            
            log.info("ML Treatment Progress Prediction: score={}, category={}", 
                    result.getScore(), result.getCategory());
            
            return result;
            
        } catch (Exception e) {
            log.warn("ML service unavailable for progress prediction: {}", e.getMessage());
            return calculateFallbackProgress(avgProgressRating, moodImprovement);
        }
    }
    
    /**
     * Predict optimal next session timing (Linear Regression)
     */
    public int predictNextSessionDays(
            double avgDaysBetweenSessions,
            int currentRiskLevel,
            double lastProgressRating,
            int patientAge) {
        
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("avg_days_between_sessions", avgDaysBetweenSessions);
            request.put("current_risk_level", currentRiskLevel);
            request.put("last_progress_rating", lastProgressRating);
            request.put("patient_age", patientAge);
            
            String response = callMLService("/api/predict/next-session", request);
            JsonNode json = objectMapper.readTree(response);
            
            return json.get("recommended_days").asInt();
            
        } catch (Exception e) {
            log.warn("ML service unavailable for scheduling: {}", e.getMessage());
            return 7; // Default: 1 week
        }
    }
    
    /**
     * Check if ML service is healthy
     */
    public boolean isHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    mlServiceUrl + "/api/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("ML service health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private String callMLService(String endpoint, Map<String, Object> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                mlServiceUrl + endpoint, entity, String.class);
        
        return response.getBody();
    }
    
    // Fallback heuristics if ML service is unavailable
    private MLPredictionResult calculateFallbackDropoutRisk(
            double cancellationRate, double noShowRate, int daysSinceLastSession) {
        
        double score = Math.min(100, 
                cancellationRate * 30 + 
                noShowRate * 40 + 
                Math.min(daysSinceLastSession * 0.8, 30));
        
        String category;
        if (score < 25) category = "LOW";
        else if (score < 50) category = "MODERATE";
        else if (score < 75) category = "HIGH";
        else category = "CRITICAL";
        
        MLPredictionResult result = new MLPredictionResult();
        result.setScore(score);
        result.setCategory(category);
        result.setConfidence(0.7); // Lower confidence for heuristic
        result.setAlgorithm("HeuristicFallback");
        result.setModelVersion("fallback");
        
        Map<String, Double> factors = new HashMap<>();
        factors.put("cancellation_impact", cancellationRate * 30);
        factors.put("no_show_impact", noShowRate * 40);
        factors.put("inactivity_impact", Math.min(daysSinceLastSession * 0.8, 30));
        result.setFactors(factors);
        
        return result;
    }
    
    private MLPredictionResult calculateFallbackProgress(
            double avgProgressRating, double moodImprovement) {
        
        double score = Math.min(100, 50 + avgProgressRating * 8 + moodImprovement * 5);
        
        String category;
        if (score >= 80) category = "EXCELLENT";
        else if (score >= 60) category = "GOOD";
        else if (score >= 40) category = "MODERATE";
        else category = "NEEDS_ATTENTION";
        
        MLPredictionResult result = new MLPredictionResult();
        result.setScore(score);
        result.setCategory(category);
        result.setConfidence(0.7);
        result.setAlgorithm("HeuristicFallback");
        
        return result;
    }
    
    // Inner class for ML result
    @lombok.Data
    public static class MLPredictionResult {
        private double score;
        private String category;
        private double confidence;
        private String algorithm;
        private String modelVersion;
        private String recommendation;
        private Map<String, Double> factors;
    }
}
