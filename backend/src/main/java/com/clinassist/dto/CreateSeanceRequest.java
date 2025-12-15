package com.clinassist.dto;

import com.clinassist.entity.Seance;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateSeanceRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Therapeute ID is required")
    private Long therapeuteId;
    
    @NotNull(message = "Scheduled date is required")
    @Future(message = "Scheduled date must be in the future")
    private LocalDateTime scheduledAt;
    
    private Integer durationMinutes = 60;
    
    private Seance.SeanceType type = Seance.SeanceType.IN_PERSON;
    
    private String objectives;
    private String notes;
    
    private String meetingRoom;
    
    private Boolean isRecurring = false;
    private String recurringPattern;
}

