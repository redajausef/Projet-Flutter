package com.clinassist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilite_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibiliteSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapeute_id", nullable = false)
    private Therapeute therapeute;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    private Boolean isAvailable;

    private Integer slotDurationMinutes;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

