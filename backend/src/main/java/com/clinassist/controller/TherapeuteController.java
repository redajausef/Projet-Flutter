package com.clinassist.controller;

import com.clinassist.dto.TherapeuteDTO;
import com.clinassist.service.TherapeuteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/therapeutes")
@RequiredArgsConstructor
@Tag(name = "Therapeute", description = "Therapeute management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TherapeuteController {

    private final TherapeuteService therapeuteService;

    @GetMapping
    @Operation(summary = "Get all therapeutes with pagination")
    public ResponseEntity<Page<TherapeuteDTO>> getAllTherapeutes(Pageable pageable) {
        return ResponseEntity.ok(therapeuteService.getAllTherapeutes(pageable));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in therapeute profile")
    @PreAuthorize("hasRole('THERAPEUTE')")
    public ResponseEntity<TherapeuteDTO> getCurrentTherapeute(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(therapeuteService.getTherapeuteByUsername(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get therapeute by ID")
    public ResponseEntity<TherapeuteDTO> getTherapeuteById(@PathVariable Long id) {
        return ResponseEntity.ok(therapeuteService.getTherapeuteById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get therapeute by user ID")
    public ResponseEntity<TherapeuteDTO> getTherapeuteByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(therapeuteService.getTherapeuteByUserId(userId));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available therapeutes")
    public ResponseEntity<List<TherapeuteDTO>> getAvailableTherapeutes() {
        return ResponseEntity.ok(therapeuteService.getAvailableTherapeutes());
    }

    @GetMapping("/search")
    @Operation(summary = "Search therapeutes")
    public ResponseEntity<Page<TherapeuteDTO>> searchTherapeutes(
            @RequestParam String q,
            Pageable pageable) {
        return ResponseEntity.ok(therapeuteService.searchTherapeutes(q, pageable));
    }

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Update therapeute availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPEUTE')")
    public ResponseEntity<TherapeuteDTO> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        return ResponseEntity.ok(therapeuteService.updateAvailability(id, available));
    }
}

