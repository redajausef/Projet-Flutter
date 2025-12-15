package com.clinassist.repository;

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
public interface TherapeuteRepository extends JpaRepository<Therapeute, Long> {
    
    Optional<Therapeute> findByTherapeuteCode(String therapeuteCode);
    
    Optional<Therapeute> findByUserId(Long userId);
    
    Optional<Therapeute> findByLicenseNumber(String licenseNumber);
    
    List<Therapeute> findByStatus(Therapeute.TherapeuteStatus status);
    
    @Query("SELECT t FROM Therapeute t WHERE :specialty MEMBER OF t.specialties")
    List<Therapeute> findBySpecialty(@Param("specialty") String specialty);
    
    @Query("SELECT t FROM Therapeute t WHERE " +
           "LOWER(t.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.specialization) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Therapeute> searchTherapeutes(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT t FROM Therapeute t WHERE t.status = 'AVAILABLE' ORDER BY t.rating DESC")
    List<Therapeute> findAvailableTherapeutes();
    
    @Query("SELECT t FROM Therapeute t LEFT JOIN t.patients p GROUP BY t ORDER BY COUNT(p) ASC")
    List<Therapeute> findTherapeutesOrderedByPatientCount();
}

