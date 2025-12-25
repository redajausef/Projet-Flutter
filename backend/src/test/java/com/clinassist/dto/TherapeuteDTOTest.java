package com.clinassist.dto;

import com.clinassist.entity.Therapeute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TherapeuteDTO Tests")
class TherapeuteDTOTest {

    private TherapeuteDTO therapeuteDTO;

    @BeforeEach
    void setUp() {
        therapeuteDTO = new TherapeuteDTO();
        therapeuteDTO.setId(1L);
        therapeuteDTO.setTherapeuteCode("TH-001");
        therapeuteDTO.setFirstName("Dr Sophie");
        therapeuteDTO.setLastName("Martin");
        therapeuteDTO.setEmail("sophie.martin@clinassist.com");
        therapeuteDTO.setSpecialization("Psychologie");
        therapeuteDTO.setStatus(Therapeute.TherapeuteStatus.AVAILABLE);
        therapeuteDTO.setYearsOfExperience(10);
    }

    @Test
    @DisplayName("Should create TherapeuteDTO with valid data")
    void createTherapeuteDTO_WithValidData_ShouldSucceed() {
        assertNotNull(therapeuteDTO);
        assertEquals("TH-001", therapeuteDTO.getTherapeuteCode());
        assertEquals("Dr Sophie", therapeuteDTO.getFirstName());
    }

    @Test
    @DisplayName("Should set status correctly")
    void setStatus_ShouldUpdateStatus() {
        therapeuteDTO.setStatus(Therapeute.TherapeuteStatus.BUSY);
        assertEquals(Therapeute.TherapeuteStatus.BUSY, therapeuteDTO.getStatus());
    }

    @Test
    @DisplayName("Should set specialties")
    void setSpecialties_ShouldUpdateSpecialties() {
        List<String> specialties = Arrays.asList("CBT", "EMDR");
        therapeuteDTO.setSpecialties(specialties);
        assertEquals(2, therapeuteDTO.getSpecialties().size());
    }

    @Test
    @DisplayName("Should set languages")
    void setLanguages_ShouldUpdateLanguages() {
        List<String> languages = Arrays.asList("Français", "English");
        therapeuteDTO.setLanguages(languages);
        assertEquals(2, therapeuteDTO.getLanguages().size());
    }

    @Test
    @DisplayName("Should set consultation fee")
    void setConsultationFee_ShouldUpdateFee() {
        therapeuteDTO.setConsultationFee(80.0);
        assertEquals(80.0, therapeuteDTO.getConsultationFee());
    }

    @Test
    @DisplayName("Should set biography")
    void setBiography_ShouldUpdateBiography() {
        therapeuteDTO.setBiography("Expert therapist");
        assertEquals("Expert therapist", therapeuteDTO.getBiography());
    }

    @Test
    @DisplayName("Should set license number")
    void setLicenseNumber_ShouldUpdateLicense() {
        therapeuteDTO.setLicenseNumber("PSY-12345");
        assertEquals("PSY-12345", therapeuteDTO.getLicenseNumber());
    }

    @Test
    @DisplayName("Should set rating")
    void setRating_ShouldUpdateRating() {
        therapeuteDTO.setRating(4.8);
        assertEquals(4.8, therapeuteDTO.getRating());
    }
}
