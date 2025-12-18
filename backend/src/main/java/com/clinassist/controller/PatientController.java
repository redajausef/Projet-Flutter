package com.clinassist.controller;

import com.clinassist.dto.PatientCreateRequest;
import com.clinassist.dto.PatientDTO;
import com.clinassist.entity.Patient;
import com.clinassist.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "Get all patients with pagination")
    public ResponseEntity<Page<PatientDTO>> getAllPatients(Pageable pageable) {
        return ResponseEntity.ok(patientService.getAllPatients(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search patients")
    public ResponseEntity<Page<PatientDTO>> searchPatients(
            @RequestParam String q,
            Pageable pageable) {
        return ResponseEntity.ok(patientService.searchPatients(q, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get patient by code")
    public ResponseEntity<PatientDTO> getPatientByCode(@PathVariable String code) {
        return ResponseEntity.ok(patientService.getPatientByCode(code));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get patient by user ID")
    public ResponseEntity<PatientDTO> getPatientByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(patientService.getPatientByUserId(userId));
    }

    @GetMapping("/therapeute/{therapeuteId}")
    @Operation(summary = "Get patients by therapeute")
    public ResponseEntity<List<PatientDTO>> getPatientsByTherapeute(@PathVariable Long therapeuteId) {
        return ResponseEntity.ok(patientService.getPatientsByTherapeute(therapeuteId));
    }

    @GetMapping("/high-risk")
    @Operation(summary = "Get high risk patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPEUTE')")
    public ResponseEntity<List<PatientDTO>> getHighRiskPatients(
            @RequestParam(defaultValue = "70") Integer minRisk) {
        return ResponseEntity.ok(patientService.getHighRiskPatients(minRisk));
    }

    @PostMapping
    @Operation(summary = "Create a new patient")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'THERAPEUTE')")
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientCreateRequest request) {
        PatientDTO createdPatient = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPEUTE', 'RECEPTIONIST')")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientDTO patientDTO) {
        return ResponseEntity.ok(patientService.updatePatient(id, patientDTO));
    }

    @PatchMapping("/{id}/assign/{therapeuteId}")
    @Operation(summary = "Assign therapeute to patient")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientDTO> assignTherapeute(
            @PathVariable Long id,
            @PathVariable Long therapeuteId) {
        return ResponseEntity.ok(patientService.assignTherapeute(id, therapeuteId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update patient status")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPEUTE')")
    public ResponseEntity<PatientDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam Patient.PatientStatus status) {
        return ResponseEntity.ok(patientService.updatePatientStatus(id, status));
    }
}

