package com.clinassist.repository;

import com.clinassist.entity.Patient;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {
    
    Optional<Seance> findBySeanceCode(String seanceCode);
    
    List<Seance> findByPatient(Patient patient);
    
    List<Seance> findByPatientId(Long patientId);
    
    List<Seance> findByTherapeute(Therapeute therapeute);
    
    List<Seance> findByTherapeuteId(Long therapeuteId);
    
    Page<Seance> findByStatus(Seance.SeanceStatus status, Pageable pageable);
    
    @Query("SELECT s FROM Seance s WHERE s.scheduledAt BETWEEN :start AND :end")
    List<Seance> findByScheduledAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM Seance s WHERE s.therapeute.id = :therapeuteId " +
           "AND s.scheduledAt BETWEEN :start AND :end")
    List<Seance> findByTherapeuteAndDateRange(
            @Param("therapeuteId") Long therapeuteId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM Seance s WHERE s.patient.id = :patientId " +
           "AND s.scheduledAt BETWEEN :start AND :end")
    List<Seance> findByPatientAndDateRange(
            @Param("patientId") Long patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM Seance s WHERE s.scheduledAt > :now AND s.status = 'SCHEDULED' " +
           "ORDER BY s.scheduledAt ASC")
    List<Seance> findUpcomingSeances(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Seance s WHERE s.therapeute.id = :therapeuteId " +
           "AND s.scheduledAt > :now AND s.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY s.scheduledAt ASC")
    List<Seance> findUpcomingSeancesByTherapeute(
            @Param("therapeuteId") Long therapeuteId,
            @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Seance s WHERE s.patient.id = :patientId " +
           "AND s.scheduledAt > :now AND s.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY s.scheduledAt ASC")
    List<Seance> findUpcomingSeancesByPatient(
            @Param("patientId") Long patientId,
            @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Seance s WHERE s.reminderSent = false " +
           "AND s.scheduledAt BETWEEN :now AND :reminderTime " +
           "AND s.status IN ('SCHEDULED', 'CONFIRMED')")
    List<Seance> findSeancesNeedingReminder(
            @Param("now") LocalDateTime now,
            @Param("reminderTime") LocalDateTime reminderTime);
    
    @Query("SELECT COUNT(s) FROM Seance s WHERE s.status = :status")
    Long countByStatus(@Param("status") Seance.SeanceStatus status);
    
    @Query("SELECT s FROM Seance s WHERE s.patient.id = :patientId " +
           "AND s.status = 'COMPLETED' ORDER BY s.scheduledAt DESC")
    List<Seance> findCompletedSeancesByPatient(@Param("patientId") Long patientId);
    
    @Query("SELECT AVG(s.progressRating) FROM Seance s WHERE s.patient.id = :patientId " +
           "AND s.progressRating IS NOT NULL")
    Double getAverageProgressRating(@Param("patientId") Long patientId);
    
    @Query("SELECT s FROM Seance s WHERE s.therapeute.id = :therapeuteId " +
           "AND s.scheduledAt BETWEEN :start AND :end")
    List<Seance> findByTherapeuteIdAndScheduledAtBetween(
            @Param("therapeuteId") Long therapeuteId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM Seance s WHERE s.therapeute.id = :therapeuteId " +
           "AND s.scheduledAt > :dateTime ORDER BY s.scheduledAt ASC")
    List<Seance> findByTherapeuteIdAndScheduledAtAfter(
            @Param("therapeuteId") Long therapeuteId,
            @Param("dateTime") LocalDateTime dateTime);
}

