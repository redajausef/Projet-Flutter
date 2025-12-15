package com.clinassist.controller;

import com.clinassist.dto.PredictionDTO;
import com.clinassist.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/predictions")
@RequiredArgsConstructor
@Tag(name = "Predictions", description = "Predictive analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'THERAPEUTE')")
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get predictions for a patient")
    public ResponseEntity<List<PredictionDTO>> getPatientPredictions(@PathVariable Long patientId) {
        return ResponseEntity.ok(predictionService.getPatientPredictions(patientId));
    }

    @GetMapping("/patient/{patientId}/latest")
    @Operation(summary = "Get latest predictions for a patient")
    public ResponseEntity<List<PredictionDTO>> getLatestPredictions(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(predictionService.getLatestPredictions(patientId, limit));
    }

    @GetMapping("/high-risk")
    @Operation(summary = "Get high risk predictions")
    public ResponseEntity<List<PredictionDTO>> getHighRiskPredictions(
            @RequestParam(defaultValue = "70") Integer minRisk) {
        return ResponseEntity.ok(predictionService.getHighRiskPredictions(minRisk));
    }

    @PostMapping("/patient/{patientId}/next-session")
    @Operation(summary = "Generate next session prediction")
    public ResponseEntity<PredictionDTO> generateNextSessionPrediction(@PathVariable Long patientId) {
        return ResponseEntity.ok(predictionService.generateNextSessionPrediction(patientId));
    }

    @PostMapping("/patient/{patientId}/dropout-risk")
    @Operation(summary = "Generate dropout risk prediction")
    public ResponseEntity<PredictionDTO> generateDropoutRiskPrediction(@PathVariable Long patientId) {
        return ResponseEntity.ok(predictionService.generateDropoutRiskPrediction(patientId));
    }

    @PostMapping("/patient/{patientId}/progress")
    @Operation(summary = "Generate treatment progress prediction")
    public ResponseEntity<PredictionDTO> generateProgressPrediction(@PathVariable Long patientId) {
        return ResponseEntity.ok(predictionService.generateTreatmentProgressPrediction(patientId));
    }
}

