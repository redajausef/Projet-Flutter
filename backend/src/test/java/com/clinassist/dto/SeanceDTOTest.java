package com.clinassist.dto;

import com.clinassist.entity.Seance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SeanceDTO Tests")
class SeanceDTOTest {

    private SeanceDTO seanceDTO;

    @BeforeEach
    void setUp() {
        seanceDTO = new SeanceDTO();
        seanceDTO.setId(1L);
        seanceDTO.setSeanceCode("SEA-001");
        seanceDTO.setPatientId(1L);
        seanceDTO.setTherapeuteId(1L);
        seanceDTO.setScheduledAt(LocalDateTime.now().plusDays(1));
        seanceDTO.setDurationMinutes(45);
        seanceDTO.setStatus(Seance.SeanceStatus.SCHEDULED);
        seanceDTO.setType(Seance.SeanceType.IN_PERSON);
    }

    @Test
    @DisplayName("Should create SeanceDTO with valid data")
    void createSeanceDTO_WithValidData_ShouldSucceed() {
        assertNotNull(seanceDTO);
        assertEquals("SEA-001", seanceDTO.getSeanceCode());
        assertEquals(45, seanceDTO.getDurationMinutes());
    }

    @Test
    @DisplayName("Should set status correctly")
    void setStatus_ShouldUpdateStatus() {
        seanceDTO.setStatus(Seance.SeanceStatus.COMPLETED);
        assertEquals(Seance.SeanceStatus.COMPLETED, seanceDTO.getStatus());
    }

    @Test
    @DisplayName("Should set type correctly")
    void setType_ShouldUpdateType() {
        seanceDTO.setType(Seance.SeanceType.VIDEO_CALL);
        assertEquals(Seance.SeanceType.VIDEO_CALL, seanceDTO.getType());
    }

    @Test
    @DisplayName("Should set notes")
    void setNotes_ShouldUpdateNotes() {
        seanceDTO.setNotes("Session notes");
        assertEquals("Session notes", seanceDTO.getNotes());
    }

    @Test
    @DisplayName("Should set objectives")
    void setObjectives_ShouldUpdateObjectives() {
        seanceDTO.setObjectives("Work on anxiety");
        assertEquals("Work on anxiety", seanceDTO.getObjectives());
    }

    @Test
    @DisplayName("Should set therapist notes")
    void setTherapeuteNotes_ShouldUpdateNotes() {
        seanceDTO.setTherapeuteNotes("Patient made progress");
        assertEquals("Patient made progress", seanceDTO.getTherapeuteNotes());
    }

    @Test
    @DisplayName("Should set patient mood before")
    void setPatientMoodBefore_ShouldUpdateMood() {
        seanceDTO.setPatientMoodBefore(3);
        assertEquals(3, seanceDTO.getPatientMoodBefore());
    }

    @Test
    @DisplayName("Should set patient mood after")
    void setPatientMoodAfter_ShouldUpdateMood() {
        seanceDTO.setPatientMoodAfter(7);
        assertEquals(7, seanceDTO.getPatientMoodAfter());
    }
}
