package com.clinassist.repository;

import com.clinassist.entity.DisponibiliteSlot;
import com.clinassist.entity.Therapeute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface DisponibiliteSlotRepository extends JpaRepository<DisponibiliteSlot, Long> {
    
    List<DisponibiliteSlot> findByTherapeute(Therapeute therapeute);
    
    List<DisponibiliteSlot> findByTherapeuteId(Long therapeuteId);
    
    List<DisponibiliteSlot> findByTherapeuteIdAndDayOfWeek(Long therapeuteId, DayOfWeek dayOfWeek);
    
    List<DisponibiliteSlot> findByTherapeuteIdAndIsAvailableTrue(Long therapeuteId);
}

