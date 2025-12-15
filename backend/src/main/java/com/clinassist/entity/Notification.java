package com.clinassist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;

    private String actionUrl;
    private String iconUrl;

    private Long referenceId;
    private String referenceType;

    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime readAt;

    private LocalDateTime scheduledFor;
    private Boolean isSent;
    private LocalDateTime sentAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum NotificationType {
        APPOINTMENT_REMINDER,
        APPOINTMENT_CONFIRMED,
        APPOINTMENT_CANCELLED,
        NEW_MESSAGE,
        TREATMENT_UPDATE,
        SYSTEM_ALERT,
        PREDICTION_ALERT,
        PAYMENT_RECEIVED,
        DOCUMENT_UPLOADED
    }

    public enum NotificationStatus {
        UNREAD,
        READ,
        ARCHIVED
    }
}

