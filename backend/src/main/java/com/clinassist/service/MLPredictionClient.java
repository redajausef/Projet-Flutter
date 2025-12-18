package com.clinassist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client service to communicate with the ML Prediction microservice
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MLPredictionClient {

    private final RestTemplate restTemplate;

    @Value("${ml.service.url:http://ml-service:5000}")
    private String mlServiceUrl;

    /**
     * Predict dropout risk using ML model
     */
    public Map<String, Object> predictDropoutRisk(
            double cancellationRate,
            double noShowRate,
            int daysSinceLastSession,
            int totalSessions,
            double avgMoodScore,
            int age
    ) {
        try {
            String url = mlServiceUrl + "/api/predict/dropout-risk";
            
            Map<String, Object> request = new HashMap<>();
            request.put("cancellation_rate", cancellationRate);
            request.put("no_show_rate", noShowRate);
            request.put("days_since_last_session", daysSinceLastSession);
            request.put("total_sessions", totalSessions);
            request.put("avg_mood_score", avgMoodScore);
            request.put("age", age);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("ML Dropout Risk prediction received: {}", response.getBody());
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("ML service unavailable for dropout risk prediction: {}", e.getMessage());
        }
        
        // Return null to indicate fallback to heuristic should be used
        return null;
    }

    /**
     * Predict treatment progress using ML model
     */
    public Map<String, Object> predictTreatmentProgress(
            int totalSessions,
            double avgProgressRating,
            double moodImprovement,
            double sessionCompletionRate
    ) {
        try {
            String url = mlServiceUrl + "/api/predict/treatment-progress";
            
            Map<String, Object> request = new HashMap<>();
            request.put("total_sessions", totalSessions);
            request.put("avg_progress_rating", avgProgressRating);
            request.put("mood_improvement", moodImprovement);
            request.put("session_completion_rate", sessionCompletionRate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("ML Treatment Progress prediction received: {}", response.getBody());
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("ML service unavailable for treatment progress prediction: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Predict optimal next session timing using ML model
     */
    public Map<String, Object> predictNextSession(
            double avgDaysBetweenSessions,
            int currentRiskLevel,
            int lastProgressRating,
            int patientAge
    ) {
        try {
            String url = mlServiceUrl + "/api/predict/next-session";
            
            Map<String, Object> request = new HashMap<>();
            request.put("avg_days_between_sessions", avgDaysBetweenSessions);
            request.put("current_risk_level", currentRiskLevel);
            request.put("last_progress_rating", lastProgressRating);
            request.put("patient_age", patientAge);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("ML Next Session prediction received: {}", response.getBody());
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("ML service unavailable for next session prediction: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Check if ML service is healthy
     */
    public boolean isHealthy() {
        try {
            String url = mlServiceUrl + "/api/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("ML service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
