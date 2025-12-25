package com.clinassist.dto;

import com.clinassist.dto.auth.AuthResponse;
import com.clinassist.dto.auth.LoginRequest;
import com.clinassist.dto.auth.RegisterRequest;
import com.clinassist.entity.Prediction;
import com.clinassist.entity.Seance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO Unit Tests")
class DtoTest {

    @Test
    void loginRequest_ShouldSetAndGet() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("test@test.com");
        request.setPassword("password");

        assertThat(request.getUsernameOrEmail()).isEqualTo("test@test.com");
        assertThat(request.getPassword()).isEqualTo("password");
    }

    @Test
    void registerRequest_ShouldSetAndGet() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@test.com");
        request.setPassword("pass");
        request.setRole(com.clinassist.entity.User.Role.PATIENT);

        assertThat(request.getFirstName()).isEqualTo("John");
        assertThat(request.getLastName()).isEqualTo("Doe");
        assertThat(request.getEmail()).isEqualTo("john@test.com");
        assertThat(request.getPassword()).isEqualTo("pass");
        assertThat(request.getRole()).isEqualTo(com.clinassist.entity.User.Role.PATIENT);
    }

    @Test
    void authResponse_ShouldBuildCorrectly() {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(1L)
                .email("test@test.com")
                .role(com.clinassist.entity.User.Role.PATIENT)
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("jwt-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userInfo)
                .build();

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getEmail()).isEqualTo("test@test.com");
        assertThat(response.getUser().getRole()).isEqualTo(com.clinassist.entity.User.Role.PATIENT);
    }

    @Test
    void createSeanceRequest_ShouldSetAndGet() {
        CreateSeanceRequest request = new CreateSeanceRequest();
        request.setPatientId(1L);
        request.setTherapeuteId(2L);
        request.setDurationMinutes(60);
        LocalDateTime now = LocalDateTime.now();
        request.setScheduledAt(now);
        request.setType(Seance.SeanceType.IN_PERSON);
        request.setNotes("Notes");

        assertThat(request.getPatientId()).isEqualTo(1L);
        assertThat(request.getTherapeuteId()).isEqualTo(2L);
        assertThat(request.getDurationMinutes()).isEqualTo(60);
        assertThat(request.getScheduledAt()).isEqualTo(now);
        assertThat(request.getType()).isEqualTo(Seance.SeanceType.IN_PERSON);
        assertThat(request.getNotes()).isEqualTo("Notes");
    }

    @Test
    void dashboardStatsDTO_ShouldBuildCorrectly() {
        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalPatients(10L)
                .totalSeances(50L)
                .totalTherapeutes(5L)
                .highRiskPatients(2L)
                .patientGrowthPercentage(5.5)
                .seanceCompletionRate(90.0)
                .build();

        assertThat(stats.getTotalPatients()).isEqualTo(10L);
        assertThat(stats.getTotalSeances()).isEqualTo(50L);
        assertThat(stats.getHighRiskPatients()).isEqualTo(2L);
        assertThat(stats.getPatientGrowthPercentage()).isEqualTo(5.5);
    }

    @Test
    void patientDTO_ShouldBuildCorrectly() {
        PatientDTO dto = PatientDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .patientCode("P001")
                .riskScore(10)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getPatientCode()).isEqualTo("P001");
    }

    @Test
    void predictionDTO_ShouldBuildCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Double> factors = Collections.singletonMap("factor1", 1.0);

        PredictionDTO dto = PredictionDTO.builder()
                .id(1L)
                .patientId(2L)
                .type(Prediction.PredictionType.DROPOUT_RISK)
                .prediction("Prediction text")
                .confidenceScore(0.95)
                .factors(factors)
                .riskLevel(5)
                .riskCategory(Prediction.RiskCategory.LOW)
                .predictedForDate(now)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPatientId()).isEqualTo(2L);
        assertThat(dto.getType()).isEqualTo(Prediction.PredictionType.DROPOUT_RISK);
        assertThat(dto.getFactors()).containsEntry("factor1", 1.0);
    }
}
