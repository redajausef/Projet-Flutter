package com.clinassist.controller;

import com.clinassist.dto.CreateSeanceRequest;
import com.clinassist.dto.SeanceDTO;
import com.clinassist.entity.Seance;
import com.clinassist.service.SeanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/seances")
@RequiredArgsConstructor
@Tag(name = "Seances", description = "Session management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SeanceController {

    private final SeanceService seanceService;

    @GetMapping
    @Operation(summary = "Get all seances with pagination")
    public ResponseEntity<Page<SeanceDTO>> getAllSeances(Pageable pageable) {
        return ResponseEntity.ok(seanceService.getAllSeances(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get seance by ID")
    public ResponseEntity<SeanceDTO> getSeanceById(@PathVariable Long id) {
        return ResponseEntity.ok(seanceService.getSeanceById(id));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get seances by patient")
    public ResponseEntity<List<SeanceDTO>> getSeancesByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(seanceService.getSeancesByPatient(patientId));
    }

    @GetMapping("/therapeute/{therapeuteId}")
    @Operation(summary = "Get seances by therapeute")
    public ResponseEntity<List<SeanceDTO>> getSeancesByTherapeute(@PathVariable Long therapeuteId) {
        return ResponseEntity.ok(seanceService.getSeancesByTherapeute(therapeuteId));
    }

    @GetMapping("/therapeute/{therapeuteId}/today")
    @Operation(summary = "Get today's seances by therapeute")
    public ResponseEntity<List<SeanceDTO>> getTodaySeancesByTherapeute(@PathVariable Long therapeuteId) {
        return ResponseEntity.ok(seanceService.getTodaySeancesByTherapeute(therapeuteId));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's seances")
    public ResponseEntity<List<SeanceDTO>> getTodaySeances() {
        return ResponseEntity.ok(seanceService.getTodaySeances());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming seances")
    public ResponseEntity<List<SeanceDTO>> getUpcomingSeances() {
        return ResponseEntity.ok(seanceService.getUpcomingSeances());
    }

    @GetMapping("/range")
    @Operation(summary = "Get seances by date range")
    public ResponseEntity<List<SeanceDTO>> getSeancesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(seanceService.getSeancesByDateRange(start, end));
    }

    @GetMapping("/therapeute/{therapeuteId}/schedule")
    @Operation(summary = "Get therapeute schedule")
    public ResponseEntity<List<SeanceDTO>> getTherapeuteSchedule(
            @PathVariable Long therapeuteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(seanceService.getTherapeuteSchedule(therapeuteId, start, end));
    }

    @PostMapping
    @Operation(summary = "Create a new seance")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPEUTE', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<SeanceDTO> createSeance(@Valid @RequestBody CreateSeanceRequest request) {
        SeanceDTO created = seanceService.createSeance(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/check-conflict")
    @Operation(summary = "Check for scheduling conflicts")
    public ResponseEntity<Boolean> checkConflict(
            @RequestParam Long therapeuteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
            @RequestParam(defaultValue = "60") Integer durationMinutes) {
        boolean hasConflict = seanceService.hasConflict(therapeuteId, scheduledAt, durationMinutes);
        return ResponseEntity.ok(hasConflict);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update seance status")
    public ResponseEntity<SeanceDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam Seance.SeanceStatus status) {
        return ResponseEntity.ok(seanceService.updateSeanceStatus(id, status));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a seance")
    public ResponseEntity<SeanceDTO> cancelSeance(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(seanceService.cancelSeance(id, reason));
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(summary = "Reschedule a seance")
    public ResponseEntity<SeanceDTO> rescheduleSeance(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDateTime) {
        return ResponseEntity.ok(seanceService.rescheduleSeance(id, newDateTime));
    }

    @PatchMapping("/{id}/notes")
    @Operation(summary = "Add session notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPEUTE')")
    public ResponseEntity<SeanceDTO> addSessionNotes(
            @PathVariable Long id,
            @RequestParam String notes,
            @RequestParam(required = false) Integer progressRating,
            @RequestParam(required = false) Integer moodBefore,
            @RequestParam(required = false) Integer moodAfter) {
        return ResponseEntity.ok(seanceService.addSessionNotes(id, notes, progressRating, moodBefore, moodAfter));
    }
}

