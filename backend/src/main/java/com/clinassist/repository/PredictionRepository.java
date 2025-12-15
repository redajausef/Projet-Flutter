package com.clinassist.repository;

import com.clinassist.entity.Patient;
import com.clinassist.entity.Prediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    
    List<Prediction> findByPatient(Patient patient);
    
    List<Prediction> findByPatientId(Long patientId);
    
    List<Prediction> findByType(Prediction.PredictionType type);
    
    Page<Prediction> findByRiskCategory(Prediction.RiskCategory riskCategory, Pageable pageable);
    
    @Query("SELECT p FROM Prediction p WHERE p.patient.id = :patientId " +
           "ORDER BY p.createdAt DESC")
    List<Prediction> findLatestPredictions(
            @Param("patientId") Long patientId,
            Pageable pageable);
    
    @Query("SELECT p FROM Prediction p WHERE p.patient.id = :patientId " +
           "AND p.type = :type ORDER BY p.createdAt DESC")
    List<Prediction> findLatestByPatientAndType(
            @Param("patientId") Long patientId,
            @Param("type") Prediction.PredictionType type);
    
    @Query("SELECT p FROM Prediction p WHERE p.riskLevel >= :minRisk " +
           "ORDER BY p.riskLevel DESC, p.createdAt DESC")
    List<Prediction> findHighRiskPredictions(@Param("minRisk") Integer minRisk);
    
    @Query("SELECT p FROM Prediction p WHERE p.createdAt BETWEEN :start AND :end")
    List<Prediction> findByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT AVG(p.confidenceScore) FROM Prediction p WHERE p.wasAccurate = true")
    Double getAverageConfidenceForAccuratePredictions();
    
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.wasAccurate = true")
    Long countAccuratePredictions();
    
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.wasAccurate IS NOT NULL")
    Long countEvaluatedPredictions();
}

