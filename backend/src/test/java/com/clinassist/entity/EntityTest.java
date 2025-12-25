package com.clinassist.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Entity Unit Tests")
class EntityTest {

    @Test
    void user_ShouldSetAndGet() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("test@test.com");
        user.setEmail("test@test.com");
        user.setPassword("hashedPwd");
        user.setRole(User.Role.PATIENT);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getFullName()).isEqualTo("John Doe");
        assertThat(user.getUsername()).isEqualTo("test@test.com");
        // UserDetails not implemented directly on Entity in this version
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    void patient_ShouldSetAndGet() {
        User user = new User();
        user.setId(1L);

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setUser(user);
        patient.setPatientCode("P01");
        patient.setRiskScore(50);
        patient.setRiskCategory("MODERATE");
        // Phone number belongs to User
        patient.setAddress("123 Main St");

        assertThat(patient.getId()).isEqualTo(1L);
        assertThat(patient.getUser()).isEqualTo(user);
        assertThat(patient.getPatientCode()).isEqualTo("P01");
        assertThat(patient.getRiskCategory()).isEqualTo("MODERATE");
    }

    @Test
    void therapeute_ShouldSetAndGet() {
        User user = new User();
        user.setId(2L);

        Therapeute therapeute = new Therapeute();
        therapeute.setId(1L);
        therapeute.setUser(user);
        therapeute.setSpecialization("Psychologist");
        therapeute.setLicenseNumber("LIC-123");
        therapeute.setBiography("Experienced"); // bio or biography? Checked Therapeute.java: it is biography
        therapeute.setStatus(Therapeute.TherapeuteStatus.AVAILABLE);

        assertThat(therapeute.getId()).isEqualTo(1L);
        assertThat(therapeute.getUser()).isEqualTo(user);
        // assertThat(therapeute.getSpecialty()).isEqualTo("Psychologist"); //
        // List<String> specialties? OR field specialization?
        // Therapeute.java has "specialization" AND "List<String> specialties".
        therapeute.setSpecialization("Psychologist");
        assertThat(therapeute.getSpecialization()).isEqualTo("Psychologist");
        assertThat(therapeute.getStatus()).isEqualTo(Therapeute.TherapeuteStatus.AVAILABLE);
    }

    @Test
    void seance_ShouldSetAndGet() {
        Seance seance = new Seance();
        seance.setId(1L);
        seance.setSeanceCode("S001");
        seance.setScheduledAt(LocalDateTime.now());
        seance.setDurationMinutes(60);
        seance.setStatus(Seance.SeanceStatus.SCHEDULED);
        seance.setType(Seance.SeanceType.VIDEO_CALL);
        seance.setNotes("Notes");

        assertThat(seance.getId()).isEqualTo(1L);
        assertThat(seance.getSeanceCode()).isEqualTo("S001");
        assertThat(seance.getStatus()).isEqualTo(Seance.SeanceStatus.SCHEDULED);
        assertThat(seance.getType()).isEqualTo(Seance.SeanceType.VIDEO_CALL);
    }

    @Test
    void prediction_ShouldBuildAndGet() {
        Prediction prediction = Prediction.builder()
                .id(1L)
                .type(Prediction.PredictionType.DROPOUT_RISK)
                .riskCategory(Prediction.RiskCategory.HIGH)
                .confidenceScore(0.8)
                .algorithmUsed("RandomForest")
                .modelVersion("1.0")
                .isActive(true)
                .build();

        assertThat(prediction.getId()).isEqualTo(1L);
        assertThat(prediction.getType()).isEqualTo(Prediction.PredictionType.DROPOUT_RISK);
        assertThat(prediction.getRiskCategory()).isEqualTo(Prediction.RiskCategory.HIGH);
        // getRiskCategoryColor() logic is inside @Data or manual getter?
        // Checked Prediction.java (will check in next step thought but assuming it's
        // not there for now to be safe)
        assertThat(prediction.getIsActive()).isTrue();
    }

    @Test
    void disponibiliteSlot_ShouldSetAndGet() {
        DisponibiliteSlot slot = new DisponibiliteSlot();
        slot.setDayOfWeek(DayOfWeek.MONDAY);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(17, 0));
        slot.setIsAvailable(true);

        assertThat(slot.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(slot.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slot.getIsAvailable()).isTrue();
    }

    @Test
    void notification_ShouldSetAndGet() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Alert");
        notification.setMessage("Message content");
        notification.setType(Notification.NotificationType.PREDICTION_ALERT);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        assertThat(notification.getId()).isEqualTo(1L);
        assertThat(notification.getTitle()).isEqualTo("Alert");
        assertThat(notification.getType()).isEqualTo(Notification.NotificationType.PREDICTION_ALERT);
        assertThat(notification.getIsRead()).isFalse();
    }
}
