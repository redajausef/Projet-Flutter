package com.clinassist.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Seance Entity Tests")
class SeanceTest {

    private Seance seance;
    private Patient patient;
    private Therapeute therapeute;

    @BeforeEach
    void setUp() {
        User patientUser = new User();
        patientUser.setId(1L);
        patientUser.setFirstName("Patient");
        patientUser.setLastName("Test");

        User therapeuteUser = new User();
        therapeuteUser.setId(2L);
        therapeuteUser.setFirstName("Dr");
        therapeuteUser.setLastName("Test");

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(patientUser);

        therapeute = new Therapeute();
        therapeute.setId(1L);
        therapeute.setUser(therapeuteUser);

        seance = new Seance();
        seance.setId(1L);
        seance.setPatient(patient);
        seance.setTherapeute(therapeute);
        seance.setScheduledAt(LocalDateTime.now().plusDays(1));
        seance.setDurationMinutes(45);
        seance.setType(Seance.SeanceType.CONSULTATION);
        seance.setStatus(Seance.SeanceStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should create seance with valid data")
    void createSeance_WithValidData_ShouldSucceed() {
        assertNotNull(seance);
        assertEquals(45, seance.getDurationMinutes());
        assertEquals(Seance.SeanceType.CONSULTATION, seance.getType());
    }

    @Test
    @DisplayName("Should update seance status")
    void updateStatus_ShouldChangeStatus() {
        seance.setStatus(Seance.SeanceStatus.COMPLETED);
        assertEquals(Seance.SeanceStatus.COMPLETED, seance.getStatus());
    }

    @Test
    @DisplayName("Seance type enum should have correct values")
    void seanceType_ShouldHaveCorrectValues() {
        Seance.SeanceType[] types = Seance.SeanceType.values();
        assertTrue(types.length >= 1);
    }

    @Test
    @DisplayName("Seance status enum should have correct values")
    void seanceStatus_ShouldHaveCorrectValues() {
        Seance.SeanceStatus[] statuses = Seance.SeanceStatus.values();
        assertTrue(statuses.length >= 2);
    }

    @Test
    @DisplayName("Should set notes")
    void setNotes_ShouldUpdateNotes() {
        seance.setNotes("Test notes for the session");
        assertEquals("Test notes for the session", seance.getNotes());
    }
}
