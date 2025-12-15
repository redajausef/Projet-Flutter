package com.clinassist.controller;

import com.clinassist.dto.PatientDTO;
import com.clinassist.entity.Patient;
import com.clinassist.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @PutMapping("/{id}")
    @Operation(summary = "Update patient")
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

