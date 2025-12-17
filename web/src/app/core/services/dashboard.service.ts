import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, of, forkJoin, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardStats, ChartDataPoint } from '../models';
import { TherapeuteService } from './therapeute.service';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;
  private http = inject(HttpClient);
  private therapeuteService = inject(TherapeuteService);
  
  // State management
  private statsSubject = new BehaviorSubject<DashboardStats | null>(null);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  
  stats$ = this.statsSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();

  /**
   * Get dashboard statistics - filtered by therapeute if applicable
   */
  getDashboardStats(): Observable<DashboardStats> {
    this.loadingSubject.next(true);
    
    const therapeuteId = this.therapeuteService.getCurrentTherapeuteId();
    
    let params = new HttpParams();
    if (therapeuteId) {
      params = params.set('therapeuteId', therapeuteId.toString());
    }
    
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`, { params }).pipe(
      tap(stats => {
        this.statsSubject.next(stats);
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        console.error('Error fetching dashboard stats:', error);
        this.loadingSubject.next(false);
        throw error;
      })
    );
  }

  /**
   * Get weekly activity data
   */
  getWeeklyActivity(): Observable<ChartDataPoint[]> {
    return this.http.get<ChartDataPoint[]>(`${this.apiUrl}/weekly-activity`).pipe(
      catchError(error => {
        console.error('Error fetching weekly activity:', error);
        throw error;
      })
    );
  }

  /**
   * Get monthly activity data
   */
  getMonthlyActivity(): Observable<ChartDataPoint[]> {
    return this.http.get<ChartDataPoint[]>(`${this.apiUrl}/monthly-activity`).pipe(
      catchError(error => {
        console.error('Error fetching monthly activity:', error);
        throw error;
      })
    );
  }

  /**
   * Get seances by type breakdown
   */
  getSeancesByType(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/seances-by-type`).pipe(
      catchError(error => {
        console.error('Error fetching seances by type:', error);
        throw error;
      })
    );
  }

  /**
   * Refresh all dashboard data
   */
  refreshDashboard(): void {
    this.getDashboardStats().subscribe();
  }

  // Demo data for development
  private getDemoStats(): DashboardStats {
    return {
      totalPatients: 156,
      activePatients: 124,
      newPatientsThisMonth: 32,
      patientGrowthPercentage: 12.5,
      
      totalTherapeutes: 8,
      availableTherapeutes: 6,
      
      totalSeances: 1245,
      todaySeances: 8,
      upcomingSeances: 24,
      completedSeancesThisMonth: 187,
      seanceCompletionRate: 94.2,
      
      highRiskPatients: 5,
      averageRiskScore: 35.2,
      
      predictionAccuracy: 87.5,
      totalPredictions: 892,
      
      upcomingSeancesList: [],
      recentPatients: [],
      recentPredictions: [],
      
      seancesByType: {
        'CONSULTATION': 45,
        'THERAPY': 30,
        'FOLLOW_UP': 15,
        'VIDEO': 10
      },
      
      patientsByStatus: {
        'ACTIVE': 124,
        'ON_HOLD': 18,
        'INACTIVE': 10,
        'DISCHARGED': 4
      },
      
      seancesTrend: this.getDemoWeeklyActivity(),
      patientsTrend: this.getDemoPatientsTrend()
    };
  }

  private getDemoWeeklyActivity(): ChartDataPoint[] {
    return [
      { label: 'Lun', value: 8, color: '#6366f1' },
      { label: 'Mar', value: 10, color: '#6366f1' },
      { label: 'Mer', value: 6, color: '#6366f1' },
      { label: 'Jeu', value: 12, color: '#6366f1' },
      { label: 'Ven', value: 9, color: '#6366f1' },
      { label: 'Sam', value: 4, color: '#6366f1' },
      { label: 'Dim', value: 1, color: '#6366f1' }
    ];
  }

  private getDemoMonthlyActivity(): ChartDataPoint[] {
    return [
      { label: 'Jan', value: 145, color: '#6366f1' },
      { label: 'Fév', value: 132, color: '#6366f1' },
      { label: 'Mar', value: 168, color: '#6366f1' },
      { label: 'Avr', value: 155, color: '#6366f1' },
      { label: 'Mai', value: 178, color: '#6366f1' },
      { label: 'Jun', value: 165, color: '#6366f1' },
      { label: 'Jul', value: 142, color: '#6366f1' },
      { label: 'Aoû', value: 98, color: '#6366f1' },
      { label: 'Sep', value: 156, color: '#6366f1' },
      { label: 'Oct', value: 172, color: '#6366f1' },
      { label: 'Nov', value: 185, color: '#6366f1' },
      { label: 'Déc', value: 187, color: '#6366f1' }
    ];
  }

  private getDemoPatientsTrend(): ChartDataPoint[] {
    return [
      { label: 'Jan', value: 98 },
      { label: 'Fév', value: 105 },
      { label: 'Mar', value: 112 },
      { label: 'Avr', value: 118 },
      { label: 'Mai', value: 125 },
      { label: 'Jun', value: 132 },
      { label: 'Jul', value: 138 },
      { label: 'Aoû', value: 140 },
      { label: 'Sep', value: 145 },
      { label: 'Oct', value: 148 },
      { label: 'Nov', value: 152 },
      { label: 'Déc', value: 156 }
    ];
  }

  /**
   * Demo stats for a specific therapeute (filtered data)
   */
  private getDemoStatsForTherapeute(therapeuteId: number): DashboardStats {
    // Sample data based on therapeute ID (1 = Dr. Fatima Benali)
    const baseStats = this.getDemoStats();
    
    if (therapeuteId === 1) {
      return {
        ...baseStats,
        totalPatients: 4, // Only their patients
        activePatients: 4,
        newPatientsThisMonth: 1,
        patientGrowthPercentage: 8.5,
        todaySeances: 4,
        upcomingSeances: 8,
        completedSeancesThisMonth: 35,
        seanceCompletionRate: 91.5,
        highRiskPatients: 1,
        averageRiskScore: 37.5
      };
    } else if (therapeuteId === 2) {
      return {
        ...baseStats,
        totalPatients: 2,
        activePatients: 2,
        newPatientsThisMonth: 0,
        patientGrowthPercentage: 0,
        todaySeances: 3,
        upcomingSeances: 6,
        completedSeancesThisMonth: 28,
        seanceCompletionRate: 88.0,
        highRiskPatients: 1,
        averageRiskScore: 81.5
      };
    }
    
    return baseStats;
  }
}

