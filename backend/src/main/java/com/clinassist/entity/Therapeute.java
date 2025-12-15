package com.clinassist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "therapeutes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Therapeute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(unique = true)
    private String therapeuteCode;

    private String licenseNumber;
    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String qualifications;

    @Column(columnDefinition = "TEXT")
    private String biography;

    private Integer yearsOfExperience;

    @ElementCollection
    @CollectionTable(name = "therapeute_specialties", joinColumns = @JoinColumn(name = "therapeute_id"))
    @Column(name = "specialty")
    @Builder.Default
    private List<String> specialties = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "therapeute_languages", joinColumns = @JoinColumn(name = "therapeute_id"))
    @Column(name = "language")
    @Builder.Default
    private List<String> languages = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTherapeute")
    @Builder.Default
    private List<Patient> patients = new ArrayList<>();

    @OneToMany(mappedBy = "therapeute", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Seance> seances = new ArrayList<>();

    @OneToMany(mappedBy = "therapeute", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DisponibiliteSlot> disponibilites = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TherapeuteStatus status = TherapeuteStatus.AVAILABLE;

    private Double consultationFee;
    private String currency;

    private Double rating;
    private Integer totalReviews;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TherapeuteStatus {
        AVAILABLE, BUSY, ON_LEAVE, INACTIVE
    }

    @PrePersist
    public void generateTherapeuteCode() {
        if (therapeuteCode == null) {
            therapeuteCode = "TH-" + System.currentTimeMillis();
        }
    }
}

