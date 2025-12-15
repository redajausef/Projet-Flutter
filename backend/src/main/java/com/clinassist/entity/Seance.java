package com.clinassist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String seanceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapeute_id", nullable = false)
    private Therapeute therapeute;

    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SeanceType type = SeanceType.IN_PERSON;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SeanceStatus status = SeanceStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String therapeuteNotes;

    @Column(columnDefinition = "TEXT")
    private String patientFeedback;

    private Integer patientMoodBefore;
    private Integer patientMoodAfter;

    private Integer progressRating;

    private String videoCallLink;
    private String meetingRoom;

    private Boolean reminderSent;
    private LocalDateTime reminderSentAt;

    private Boolean isRecurring;
    private String recurringPattern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_seance_id")
    private Seance parentSeance;

    private String cancellationReason;
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum SeanceType {
        IN_PERSON,
        VIDEO_CALL,
        PHONE_CALL,
        HOME_VISIT,
        GROUP_SESSION
    }

    public enum SeanceStatus {
        SCHEDULED,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW,
        RESCHEDULED
    }

    @PrePersist
    public void generateSeanceCode() {
        if (seanceCode == null) {
            seanceCode = "SEA-" + System.currentTimeMillis();
        }
    }

    public boolean isUpcoming() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }

    public boolean isPast() {
        return scheduledAt != null && scheduledAt.isBefore(LocalDateTime.now());
    }
}

