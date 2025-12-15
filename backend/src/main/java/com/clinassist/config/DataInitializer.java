package com.clinassist.config;

import com.clinassist.entity.*;
import com.clinassist.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final TherapeuteRepository therapeuteRepository;
    private final SeanceRepository seanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initializing demo data...");
            initializeData();
            log.info("Demo data initialized successfully!");
        }
    }

    private void initializeData() {
        // Create Admin
        User admin = User.builder()
                .username("admin")
                .email("admin@clinassist.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("System")
                .role(User.Role.ADMIN)
                .isActive(true)
                .isEmailVerified(true)
                .build();
        userRepository.save(admin);

        // Create Therapeutes
        User therapist1User = User.builder()
                .username("dr.martin")
                .email("martin@clinassist.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Sophie")
                .lastName("Martin")
                .phoneNumber("+33 6 12 34 56 78")
                .role(User.Role.THERAPEUTE)
                .isActive(true)
                .isEmailVerified(true)
                .profileImageUrl("https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=200")
                .build();
        therapist1User = userRepository.save(therapist1User);

        Therapeute therapist1 = Therapeute.builder()
                .user(therapist1User)
                .licenseNumber("TH-2024-001")
                .specialization("Psychologie Clinique")
                .qualifications("Doctorat en Psychologie, Université Paris-Saclay")
                .biography("Spécialisée dans le traitement des troubles anxieux et dépressifs avec 15 ans d'expérience.")
                .yearsOfExperience(15)
                .specialties(Arrays.asList("Anxiété", "Dépression", "TCC", "EMDR"))
                .languages(Arrays.asList("Français", "Anglais"))
                .status(Therapeute.TherapeuteStatus.AVAILABLE)
                .consultationFee(80.0)
                .currency("EUR")
                .rating(4.9)
                .totalReviews(127)
                .build();
        therapist1 = therapeuteRepository.save(therapist1);

        User therapist2User = User.builder()
                .username("dr.dubois")
                .email("dubois@clinassist.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jean")
                .lastName("Dubois")
                .phoneNumber("+33 6 98 76 54 32")
                .role(User.Role.THERAPEUTE)
                .isActive(true)
                .isEmailVerified(true)
                .profileImageUrl("https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=200")
                .build();
        therapist2User = userRepository.save(therapist2User);

        Therapeute therapist2 = Therapeute.builder()
                .user(therapist2User)
                .licenseNumber("TH-2024-002")
                .specialization("Psychothérapie Familiale")
                .qualifications("Master en Psychologie Familiale, HEC Paris")
                .biography("Expert en thérapie familiale et de couple, approche systémique.")
                .yearsOfExperience(10)
                .specialties(Arrays.asList("Thérapie Familiale", "Thérapie de Couple", "Médiation"))
                .languages(Arrays.asList("Français", "Espagnol"))
                .status(Therapeute.TherapeuteStatus.AVAILABLE)
                .consultationFee(90.0)
                .currency("EUR")
                .rating(4.7)
                .totalReviews(89)
                .build();
        therapist2 = therapeuteRepository.save(therapist2);

        // Create Patients
        createPatient("marie.laurent", "Marie", "Laurent", "marie.laurent@email.com",
                LocalDate.of(1985, 3, 15), Patient.Gender.FEMALE, therapist1, 35, "MODERATE");

        createPatient("pierre.bernard", "Pierre", "Bernard", "pierre.bernard@email.com",
                LocalDate.of(1978, 7, 22), Patient.Gender.MALE, therapist1, 65, "HIGH");

        createPatient("claire.petit", "Claire", "Petit", "claire.petit@email.com",
                LocalDate.of(1992, 11, 8), Patient.Gender.FEMALE, therapist2, 20, "LOW");

        createPatient("lucas.moreau", "Lucas", "Moreau", "lucas.moreau@email.com",
                LocalDate.of(1988, 5, 30), Patient.Gender.MALE, therapist2, 45, "MODERATE");

        createPatient("emma.garcia", "Emma", "Garcia", "emma.garcia@email.com",
                LocalDate.of(1995, 9, 12), Patient.Gender.FEMALE, null, 25, "LOW");

        // Create some seances
        List<Patient> patients = patientRepository.findAll();
        for (Patient patient : patients) {
            if (patient.getAssignedTherapeute() != null) {
                // Past seance
                Seance pastSeance = Seance.builder()
                        .patient(patient)
                        .therapeute(patient.getAssignedTherapeute())
                        .scheduledAt(LocalDateTime.now().minusDays(7))
                        .durationMinutes(60)
                        .type(Seance.SeanceType.IN_PERSON)
                        .status(Seance.SeanceStatus.COMPLETED)
                        .objectives("Évaluation initiale")
                        .therapeuteNotes("Bonne progression observée")
                        .patientMoodBefore(4)
                        .patientMoodAfter(7)
                        .progressRating(8)
                        .build();
                seanceRepository.save(pastSeance);

                // Upcoming seance
                Seance upcomingSeance = Seance.builder()
                        .patient(patient)
                        .therapeute(patient.getAssignedTherapeute())
                        .scheduledAt(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0))
                        .durationMinutes(60)
                        .type(Seance.SeanceType.VIDEO_CALL)
                        .status(Seance.SeanceStatus.SCHEDULED)
                        .objectives("Suivi thérapeutique")
                        .videoCallLink("https://meet.clinassist.com/" + patient.getPatientCode())
                        .build();
                seanceRepository.save(upcomingSeance);
            }
        }
    }

    private void createPatient(String username, String firstName, String lastName, String email,
                               LocalDate dob, Patient.Gender gender, Therapeute therapeute,
                               Integer riskScore, String riskCategory) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("patient123"))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("+33 6 00 00 00 00")
                .role(User.Role.PATIENT)
                .isActive(true)
                .isEmailVerified(true)
                .build();
        user = userRepository.save(user);

        Patient patient = Patient.builder()
                .user(user)
                .dateOfBirth(dob)
                .gender(gender)
                .address("123 Rue de Paris")
                .city("Paris")
                .postalCode("75001")
                .country("France")
                .status(Patient.PatientStatus.ACTIVE)
                .assignedTherapeute(therapeute)
                .riskScore(riskScore)
                .riskCategory(riskCategory)
                .medicalHistory("Aucun antécédent particulier")
                .build();
        patientRepository.save(patient);
    }
}

