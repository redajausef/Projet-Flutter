package com.clinassist.repository;

import com.clinassist.entity.Patient;
import com.clinassist.entity.Therapeute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByPatientCode(String patientCode);
    
    Optional<Patient> findByUserId(Long userId);
    
    List<Patient> findByAssignedTherapeute(Therapeute therapeute);
    
    List<Patient> findByAssignedTherapeuteId(Long therapeuteId);
    
    Page<Patient> findByStatus(Patient.PatientStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT p FROM Patient p WHERE p.riskScore >= :minRisk ORDER BY p.riskScore DESC")
    List<Patient> findHighRiskPatients(@Param("minRisk") Integer minRisk);
    
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.status = :status")
    Long countByStatus(@Param("status") Patient.PatientStatus status);
    
    @Query("SELECT p FROM Patient p WHERE p.assignedTherapeute IS NULL AND p.status = 'ACTIVE'")
    List<Patient> findUnassignedPatients();
}

