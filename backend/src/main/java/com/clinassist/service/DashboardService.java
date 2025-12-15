package com.clinassist.service;

import com.clinassist.dto.DashboardStatsDTO;
import com.clinassist.dto.PatientDTO;
import com.clinassist.dto.PredictionDTO;
import com.clinassist.dto.SeanceDTO;
import com.clinassist.entity.Patient;
import com.clinassist.entity.Seance;
import com.clinassist.entity.Therapeute;
import com.clinassist.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PatientRepository patientRepository;
    private final TherapeuteRepository therapeuteRepository;
    private final SeanceRepository seanceRepository;
    private final PredictionRepository predictionRepository;
    private final PatientService patientService;
    private final SeanceService seanceService;
    private final PredictionService predictionService;

    public DashboardStatsDTO getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // Patient stats
        long totalPatients = patientRepository.count();
        long activePatients = patientRepository.countByStatus(Patient.PatientStatus.ACTIVE);
        
        // Calculate new patients this month
        long newPatientsThisMonth = patientRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startOfMonth))
                .count();

        // Patient growth (compare to previous month)
        LocalDateTime startOfPrevMonth = startOfMonth.minusMonths(1);
        long patientsLastMonth = patientRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && 
                        p.getCreatedAt().isAfter(startOfPrevMonth) && 
                        p.getCreatedAt().isBefore(startOfMonth))
                .count();
        double patientGrowth = patientsLastMonth == 0 ? 100 : 
                ((double) (newPatientsThisMonth - patientsLastMonth) / patientsLastMonth) * 100;

        // Therapeute stats
        long totalTherapeutes = therapeuteRepository.count();
        long availableTherapeutes = therapeuteRepository.findByStatus(Therapeute.TherapeuteStatus.AVAILABLE).size();

        // Seance stats
        long totalSeances = seanceRepository.count();
        List<Seance> todaySeances = seanceRepository.findByScheduledAtBetween(startOfDay, endOfDay);
        long todaySeancesCount = todaySeances.size();
        
        List<Seance> upcomingSeances = seanceRepository.findUpcomingSeances(now);
        long upcomingSeancesCount = upcomingSeances.size();
        
        long completedThisMonth = seanceRepository.findByScheduledAtBetween(startOfMonth, now).stream()
                .filter(s -> s.getStatus() == Seance.SeanceStatus.COMPLETED)
                .count();
        
        long totalThisMonth = seanceRepository.findByScheduledAtBetween(startOfMonth, now).size();
        double completionRate = totalThisMonth == 0 ? 0 : 
                ((double) completedThisMonth / totalThisMonth) * 100;

        // Risk stats
        List<Patient> highRiskPatients = patientRepository.findHighRiskPatients(70);
        double avgRiskScore = patientRepository.findAll().stream()
                .filter(p -> p.getRiskScore() != null)
                .mapToInt(Patient::getRiskScore)
                .average()
                .orElse(0);

        // Prediction accuracy
        Long accuratePredictions = predictionRepository.countAccuratePredictions();
        Long evaluatedPredictions = predictionRepository.countEvaluatedPredictions();
        double predictionAccuracy = evaluatedPredictions == 0 ? 0 : 
                ((double) accuratePredictions / evaluatedPredictions) * 100;

        // Get lists for dashboard
        List<SeanceDTO> upcomingList = upcomingSeances.stream()
                .limit(5)
                .map(s -> seanceService.getSeanceById(s.getId()))
                .collect(Collectors.toList());

        List<PatientDTO> recentPatients = patientRepository.findAll(
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(p -> patientService.getPatientById(p.getId()))
                .collect(Collectors.toList());

        List<PredictionDTO> recentPredictions = predictionRepository.findAll(
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(p -> predictionService.getLatestPredictions(p.getPatient().getId(), 1).get(0))
                .collect(Collectors.toList());

        // Charts data
        Map<String, Long> seancesByType = Arrays.stream(Seance.SeanceType.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        type -> seanceRepository.findAll().stream()
                                .filter(s -> s.getType() == type)
                                .count()
                ));

        Map<String, Long> patientsByStatus = Arrays.stream(Patient.PatientStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        status -> patientRepository.countByStatus(status)
                ));

        // Seances trend (last 7 days)
        List<DashboardStatsDTO.ChartDataPoint> seancesTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            long count = seanceRepository.findByScheduledAtBetween(dayStart, dayEnd).size();
            seancesTrend.add(DashboardStatsDTO.ChartDataPoint.builder()
                    .label(date.getDayOfWeek().toString().substring(0, 3))
                    .value(count)
                    .color("#0D4F4F")
                    .build());
        }

        return DashboardStatsDTO.builder()
                .totalPatients(totalPatients)
                .activePatients(activePatients)
                .newPatientsThisMonth(newPatientsThisMonth)
                .patientGrowthPercentage(patientGrowth)
                .totalTherapeutes(totalTherapeutes)
                .availableTherapeutes(availableTherapeutes)
                .totalSeances(totalSeances)
                .todaySeances(todaySeancesCount)
                .upcomingSeances(upcomingSeancesCount)
                .completedSeancesThisMonth(completedThisMonth)
                .seanceCompletionRate(completionRate)
                .highRiskPatients((long) highRiskPatients.size())
                .averageRiskScore(avgRiskScore)
                .predictionAccuracy(predictionAccuracy)
                .totalPredictions(predictionRepository.count())
                .upcomingSeancesList(upcomingList)
                .recentPatients(recentPatients)
                .recentPredictions(recentPredictions)
                .seancesByType(seancesByType)
                .patientsByStatus(patientsByStatus)
                .seancesTrend(seancesTrend)
                .build();
    }
}

