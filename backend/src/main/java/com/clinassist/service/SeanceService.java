package com.clinassist.service;

import com.clinassist.dto.CreateSeanceRequest;
import com.clinassist.dto.SeanceDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import com.clinassist.exception.BadRequestException;
import com.clinassist.exception.ResourceNotFoundException;
import com.clinassist.repository.PatientRepository;
import com.clinassist.repository.SeanceRepository;
import com.clinassist.repository.TherapeuteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final PatientRepository patientRepository;
    private final TherapeuteRepository therapeuteRepository;

    public Page<SeanceDTO> getAllSeances(Pageable pageable) {
        return seanceRepository.findAll(pageable).map(this::convertToDTO);
    }

    public SeanceDTO getSeanceById(Long id) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance not found with id: " + id));
        return convertToDTO(seance);
    }

    public List<SeanceDTO> getSeancesByPatient(Long patientId) {
        return seanceRepository.findByPatientId(patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SeanceDTO> getSeancesByTherapeute(Long therapeuteId) {
        return seanceRepository.findByTherapeuteId(therapeuteId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SeanceDTO> getTodaySeancesByTherapeute(Long therapeuteId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return seanceRepository.findByTherapeuteAndDateRange(therapeuteId, startOfDay, endOfDay)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SeanceDTO> getTodaySeances() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return seanceRepository.findByScheduledAtBetween(startOfDay, endOfDay)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SeanceDTO> getUpcomingSeances() {
        return seanceRepository.findUpcomingSeances(LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SeanceDTO> getSeancesByDateRange(LocalDateTime start, LocalDateTime end) {
        return seanceRepository.findByScheduledAtBetween(start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SeanceDTO> getTherapeuteSchedule(Long therapeuteId, LocalDateTime start, LocalDateTime end) {
        return seanceRepository.findByTherapeuteAndDateRange(therapeuteId, start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SeanceDTO createSeance(CreateSeanceRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Therapeute therapeute = therapeuteRepository.findById(request.getTherapeuteId())
                .orElseThrow(() -> new ResourceNotFoundException("Therapeute not found"));

        // Check for conflicts
        List<Seance> conflicts = seanceRepository.findByTherapeuteAndDateRange(
                therapeute.getId(),
                request.getScheduledAt(),
                request.getScheduledAt().plusMinutes(request.getDurationMinutes())
        );

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Therapeute has a conflicting appointment at this time");
        }

        // Determine status based on creator role (via request or context)
        // If created by patient, set to PENDING_APPROVAL, otherwise SCHEDULED
        Seance.SeanceStatus initialStatus = request.getInitialStatus() != null 
                ? request.getInitialStatus() 
                : Seance.SeanceStatus.SCHEDULED;

        Seance seance = Seance.builder()
                .patient(patient)
                .therapeute(therapeute)
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .type(request.getType())
                .status(initialStatus)
                .objectives(request.getObjectives())
                .notes(request.getNotes())
                .meetingRoom(request.getMeetingRoom())
                .isRecurring(request.getIsRecurring())
                .recurringPattern(request.getRecurringPattern())
                .reminderSent(false)
                .build();

        // Generate video link if it's a video call
        if (request.getType() == Seance.SeanceType.VIDEO_CALL) {
            seance.setVideoCallLink("https://meet.clinassist.com/" + UUID.randomUUID());
        }

        seance = seanceRepository.save(seance);
        return convertToDTO(seance);
    }

    @Transactional
    public SeanceDTO updateSeanceStatus(Long id, Seance.SeanceStatus status) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance not found"));

        seance.setStatus(status);

        if (status == Seance.SeanceStatus.IN_PROGRESS) {
            seance.setStartedAt(LocalDateTime.now());
        } else if (status == Seance.SeanceStatus.COMPLETED) {
            seance.setEndedAt(LocalDateTime.now());
        } else if (status == Seance.SeanceStatus.CANCELLED) {
            seance.setCancelledAt(LocalDateTime.now());
        }

        seance = seanceRepository.save(seance);
        return convertToDTO(seance);
    }

    @Transactional
    public SeanceDTO cancelSeance(Long id, String reason) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance not found"));

        seance.setStatus(Seance.SeanceStatus.CANCELLED);
        seance.setCancellationReason(reason);
        seance.setCancelledAt(LocalDateTime.now());

        seance = seanceRepository.save(seance);
        return convertToDTO(seance);
    }

    @Transactional
    public SeanceDTO rescheduleSeance(Long id, LocalDateTime newDateTime) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance not found"));

        // Check for conflicts
        List<Seance> conflicts = seanceRepository.findByTherapeuteAndDateRange(
                seance.getTherapeute().getId(),
                newDateTime,
                newDateTime.plusMinutes(seance.getDurationMinutes())
        );

        conflicts.removeIf(s -> s.getId().equals(id));

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Therapeute has a conflicting appointment at this time");
        }

        seance.setScheduledAt(newDateTime);
        seance.setStatus(Seance.SeanceStatus.RESCHEDULED);
        seance.setReminderSent(false);

        seance = seanceRepository.save(seance);
        return convertToDTO(seance);
    }

    @Transactional
    public SeanceDTO addSessionNotes(Long id, String therapeuteNotes, Integer progressRating,
                                     Integer moodBefore, Integer moodAfter) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seance not found"));

        seance.setTherapeuteNotes(therapeuteNotes);
        seance.setProgressRating(progressRating);
        seance.setPatientMoodBefore(moodBefore);
        seance.setPatientMoodAfter(moodAfter);

        seance = seanceRepository.save(seance);
        return convertToDTO(seance);
    }

    public Long countByStatus(Seance.SeanceStatus status) {
        return seanceRepository.countByStatus(status);
    }

    public boolean hasConflict(Long therapeuteId, LocalDateTime scheduledAt, Integer durationMinutes) {
        LocalDateTime endTime = scheduledAt.plusMinutes(durationMinutes);
        List<Seance> conflicts = seanceRepository.findByTherapeuteAndDateRange(
                therapeuteId,
                scheduledAt,
                endTime
        );
        return !conflicts.isEmpty();
    }

    private SeanceDTO convertToDTO(Seance seance) {
        return SeanceDTO.builder()
                .id(seance.getId())
                .seanceCode(seance.getSeanceCode())
                .patientId(seance.getPatient().getId())
                .patientName(seance.getPatient().getUser().getFullName())
                .patientCode(seance.getPatient().getPatientCode())
                .patientImageUrl(seance.getPatient().getUser().getProfileImageUrl())
                .therapeuteId(seance.getTherapeute().getId())
                .therapeuteName(seance.getTherapeute().getUser().getFullName())
                .therapeuteCode(seance.getTherapeute().getTherapeuteCode())
                .therapeuteImageUrl(seance.getTherapeute().getUser().getProfileImageUrl())
                .scheduledAt(seance.getScheduledAt())
                .startedAt(seance.getStartedAt())
                .endedAt(seance.getEndedAt())
                .durationMinutes(seance.getDurationMinutes())
                .type(seance.getType())
                .status(seance.getStatus())
                .objectives(seance.getObjectives())
                .notes(seance.getNotes())
                .therapeuteNotes(seance.getTherapeuteNotes())
                .patientFeedback(seance.getPatientFeedback())
                .patientMoodBefore(seance.getPatientMoodBefore())
                .patientMoodAfter(seance.getPatientMoodAfter())
                .progressRating(seance.getProgressRating())
                .videoCallLink(seance.getVideoCallLink())
                .meetingRoom(seance.getMeetingRoom())
                .reminderSent(seance.getReminderSent())
                .isRecurring(seance.getIsRecurring())
                .recurringPattern(seance.getRecurringPattern())
                .cancellationReason(seance.getCancellationReason())
                .createdAt(seance.getCreatedAt())
                .updatedAt(seance.getUpdatedAt())
                .build();
    }
}

