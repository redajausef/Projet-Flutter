package com.clinassist.dto;

import com.clinassist.entity.Seance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeanceDTO {
    
    private Long id;
    private String seanceCode;
    
    private Long patientId;
    private String patientName;
    private String patientCode;
    private String patientImageUrl;
    
    private Long therapeuteId;
    private String therapeuteName;
    private String therapeuteCode;
    private String therapeuteImageUrl;
    
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationMinutes;
    
    private Seance.SeanceType type;
    private Seance.SeanceStatus status;
    
    private String objectives;
    private String notes;
    private String therapeuteNotes;
    private String patientFeedback;
    
    private Integer patientMoodBefore;
    private Integer patientMoodAfter;
    private Integer progressRating;
    
    private String videoCallLink;
    private String meetingRoom;
    
    private Boolean reminderSent;
    private Boolean isRecurring;
    private String recurringPattern;
    
    private String cancellationReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

