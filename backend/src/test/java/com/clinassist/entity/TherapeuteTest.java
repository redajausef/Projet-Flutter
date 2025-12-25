package com.clinassist.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Therapeute Entity Tests")
class TherapeuteTest {

    private Therapeute therapeute;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Dr Sophie");
        user.setLastName("Martin");
        user.setEmail("sophie.martin@clinassist.com");

        therapeute = new Therapeute();
        therapeute.setId(1L);
        therapeute.setTherapeuteCode("TH-001");
        therapeute.setUser(user);
        therapeute.setSpecialization("Psychologie");
        therapeute.setYearsOfExperience(10);
        therapeute.setStatus(Therapeute.TherapeuteStatus.AVAILABLE);
        therapeute.setLicenseNumber("PSY-12345");
        therapeute.setConsultationFee(80.0);
        therapeute.setCurrency("EUR");
    }

    @Test
    @DisplayName("Should create therapeute with valid data")
    void createTherapeute_WithValidData_ShouldSucceed() {
        assertNotNull(therapeute);
        assertEquals("TH-001", therapeute.getTherapeuteCode());
        assertEquals("Psychologie", therapeute.getSpecialization());
    }

    @Test
    @DisplayName("Should update therapeute status")
    void updateStatus_ShouldChangeStatus() {
        therapeute.setStatus(Therapeute.TherapeuteStatus.BUSY);
        assertEquals(Therapeute.TherapeuteStatus.BUSY, therapeute.getStatus());
    }

    @Test
    @DisplayName("Therapeute status enum should have correct values")
    void therapeuteStatus_ShouldHaveCorrectValues() {
        Therapeute.TherapeuteStatus[] statuses = Therapeute.TherapeuteStatus.values();
        assertTrue(statuses.length >= 3);
        assertEquals(Therapeute.TherapeuteStatus.AVAILABLE, Therapeute.TherapeuteStatus.valueOf("AVAILABLE"));
    }

    @Test
    @DisplayName("Should set years of experience")
    void setYearsOfExperience_ShouldUpdateExperience() {
        therapeute.setYearsOfExperience(15);
        assertEquals(15, therapeute.getYearsOfExperience());
    }

    @Test
    @DisplayName("Should set consultation fee")
    void setConsultationFee_ShouldUpdateFee() {
        therapeute.setConsultationFee(100.0);
        assertEquals(100.0, therapeute.getConsultationFee());
    }

    @Test
    @DisplayName("Should set license number")
    void setLicenseNumber_ShouldUpdateLicense() {
        therapeute.setLicenseNumber("PSY-99999");
        assertEquals("PSY-99999", therapeute.getLicenseNumber());
    }

    @Test
    @DisplayName("Should manage specialties list")
    void setSpecialties_ShouldUpdateList() {
        therapeute.setSpecialties(new ArrayList<>(Arrays.asList("CBT", "DBT", "EMDR")));
        assertEquals(3, therapeute.getSpecialties().size());
    }

    @Test
    @DisplayName("Should manage languages list")
    void setLanguages_ShouldUpdateList() {
        therapeute.setLanguages(new ArrayList<>(Arrays.asList("Français", "English", "Español")));
        assertEquals(3, therapeute.getLanguages().size());
    }

    @Test
    @DisplayName("Should set biography")
    void setBiography_ShouldUpdateBiography() {
        therapeute.setBiography("Expert en thérapie cognitive");
        assertEquals("Expert en thérapie cognitive", therapeute.getBiography());
    }

    @Test
    @DisplayName("Should set rating")
    void setRating_ShouldUpdateRating() {
        therapeute.setRating(4.8);
        assertEquals(4.8, therapeute.getRating());
    }
}
