import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, of, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Seance, SeanceCreateRequest, PageResponse } from '../models';
import { TherapeuteService } from './therapeute.service';

@Injectable({
  providedIn: 'root'
})
export class SeanceService {
  private readonly apiUrl = `${environment.apiUrl}/seances`;
  private http = inject(HttpClient);
  private therapeuteService = inject(TherapeuteService);
  
  // State management
  private seancesSubject = new BehaviorSubject<Seance[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  
  seances$ = this.seancesSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();

  /**
   * Get all seances with pagination - filtered by current therapeute
   */
  getSeances(page = 0, size = 10): Observable<PageResponse<Seance>> {
    this.loadingSubject.next(true);
    
    // Check if current user is a therapeute - filter by their seances only
    const therapeuteId = this.therapeuteService.getCurrentTherapeuteId();
    
    if (therapeuteId) {
      return this.getSeancesByTherapeute(therapeuteId).pipe(
        map(seances => {
          const start = page * size;
          const paged = seances.slice(start, start + size);
          
          this.seancesSubject.next(paged);
          this.loadingSubject.next(false);
          
          return {
            content: paged,
            totalElements: seances.length,
            totalPages: Math.ceil(seances.length / size),
            size: size,
            number: page,
            first: page === 0,
            last: start + size >= seances.length
          };
        }),
        catchError(error => {
          console.error('Error fetching seances:', error);
          this.loadingSubject.next(false);
          throw error;
        })
      );
    }
    
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<Seance>>(this.apiUrl, { params }).pipe(
      tap(response => {
        this.seancesSubject.next(response.content);
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        console.error('Error fetching seances:', error);
        this.loadingSubject.next(false);
        throw error;
      })
    );
  }

  /**
   * Get upcoming seances
   */
  getUpcomingSeances(): Observable<Seance[]> {
    return this.http.get<Seance[]>(`${this.apiUrl}/upcoming`).pipe(
      catchError(error => {
        console.error('Error fetching upcoming seances:', error);
        throw error;
      })
    );
  }

  /**
   * Get seances by date range
   */
  getSeancesByDateRange(start: string, end: string): Observable<Seance[]> {
    return this.http.get<Seance[]>(`${this.apiUrl}/range`, {
      params: new HttpParams()
        .set('start', start)
        .set('end', end)
    }).pipe(
      catchError(error => {
        console.error('Error fetching seances by date range:', error);
        throw error;
      })
    );
  }

  /**
   * Get seance by ID
   */
  getSeanceById(id: number): Observable<Seance> {
    return this.http.get<Seance>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get seances by patient
   */
  getSeancesByPatient(patientId: number): Observable<Seance[]> {
    return this.http.get<Seance[]>(`${this.apiUrl}/patient/${patientId}`).pipe(
      catchError(error => {
        console.error('Error fetching seances by patient:', error);
        throw error;
      })
    );
  }

  /**
   * Get seances by therapeute
   */
  getSeancesByTherapeute(therapeuteId: number): Observable<Seance[]> {
    return this.http.get<Seance[]>(`${this.apiUrl}/therapeute/${therapeuteId}`).pipe(
      catchError(error => {
        console.error('Error fetching seances by therapeute:', error);
        throw error;
      })
    );
  }

  /**
   * Get pending approval seances
   */
  getPendingApprovalSeances(): Observable<Seance[]> {
    const therapeuteId = this.therapeuteService.getCurrentTherapeuteId();
    
    if (therapeuteId) {
      return this.http.get<Seance[]>(`${this.apiUrl}/therapeute/${therapeuteId}`).pipe(
        map(seances => seances.filter(s => s.status === 'PENDING_APPROVAL')),
        catchError(error => {
          console.error('Error fetching pending approval seances:', error);
          throw error;
        })
      );
    }
    
    // Fallback: get all seances and filter
    return this.http.get<Seance[]>(`${this.apiUrl}`).pipe(
      map(seances => seances.filter(s => s.status === 'PENDING_APPROVAL')),
      catchError(error => {
        console.error('Error fetching pending approval seances:', error);
        throw error;
      })
    );
  }

  /**
   * Get today's seances - filtered by therapeute if applicable
   */
  getTodaySeances(): Observable<Seance[]> {
    const therapeuteId = this.therapeuteService.getCurrentTherapeuteId();
    
    if (therapeuteId) {
      return this.http.get<Seance[]>(`${this.apiUrl}/therapeute/${therapeuteId}/today`).pipe(
        catchError(error => {
          console.error('Error fetching today seances for therapeute:', error);
          throw error;
        })
      );
    }
    
    return this.http.get<Seance[]>(`${this.apiUrl}/today`).pipe(
      catchError(error => {
        console.error('Error fetching today seances:', error);
        throw error;
      })
    );
  }

  /**
   * Create a new seance
   */
  createSeance(seance: SeanceCreateRequest): Observable<Seance> {
    return this.http.post<Seance>(this.apiUrl, seance).pipe(
      tap(newSeance => {
        const currentSeances = this.seancesSubject.getValue();
        this.seancesSubject.next([newSeance, ...currentSeances]);
      })
    );
  }

  /**
   * Update seance
   */
  updateSeance(id: number, seance: Partial<Seance>): Observable<Seance> {
    return this.http.put<Seance>(`${this.apiUrl}/${id}`, seance).pipe(
      tap(updatedSeance => {
        const currentSeances = this.seancesSubject.getValue();
        const index = currentSeances.findIndex(s => s.id === id);
        if (index !== -1) {
          currentSeances[index] = updatedSeance;
          this.seancesSubject.next([...currentSeances]);
        }
      })
    );
  }

  /**
   * Update seance status
   */
  updateSeanceStatus(id: number, status: Seance['status']): Observable<Seance> {
    return this.http.patch<Seance>(`${this.apiUrl}/${id}/status`, null, {
      params: new HttpParams().set('status', status)
    }).pipe(
      tap(updatedSeance => {
        const currentSeances = this.seancesSubject.getValue();
        const index = currentSeances.findIndex(s => s.id === id);
        if (index !== -1) {
          currentSeances[index] = updatedSeance;
          this.seancesSubject.next([...currentSeances]);
        }
      })
    );
  }

  /**
   * Start seance
   */
  startSeance(id: number): Observable<Seance> {
    return this.updateSeanceStatus(id, 'IN_PROGRESS');
  }

  /**
   * Complete seance
   */
  completeSeance(id: number, notes?: string, outcomes?: string): Observable<Seance> {
    return this.http.patch<Seance>(`${this.apiUrl}/${id}/complete`, { notes, outcomes });
  }

  /**
   * Cancel seance
   */
  cancelSeance(id: number, reason?: string): Observable<Seance> {
    return this.http.patch<Seance>(`${this.apiUrl}/${id}/cancel`, { reason });
  }

  // Demo data for development - Moroccan names
  private getDemoData(): PageResponse<Seance> {
    const today = new Date();
    const demoSeances: Seance[] = [
      {
        id: 1,
        seanceCode: 'SEA-001',
        patientId: 1,
        patientName: 'Sara Ouazzani',
        therapeuteId: 1,
        therapeuteName: 'Dr. Fatima Benali',
        type: 'THERAPY',
        status: 'COMPLETED',
        scheduledAt: new Date(new Date().setHours(9, 0)).toISOString(),
        durationMinutes: 60,
        notes: 'Bonne progression, Sara montre une amélioration significative',
        isVideoSession: false,
        createdAt: '2024-12-10T10:00:00'
      },
      {
        id: 2,
        seanceCode: 'SEA-002',
        patientId: 2,
        patientName: 'Karim Benjelloun',
        therapeuteId: 1,
        therapeuteName: 'Dr. Fatima Benali',
        type: 'CONSULTATION',
        status: 'COMPLETED',
        scheduledAt: new Date(new Date().setHours(10, 30)).toISOString(),
        durationMinutes: 45,
        notes: 'Première consultation de suivi',
        isVideoSession: false,
        createdAt: '2024-12-11T09:00:00'
      },
      {
        id: 3,
        seanceCode: 'SEA-003',
        patientId: 4,
        patientName: 'Mehdi Fassi Fihri',
        therapeuteId: 1,
        therapeuteName: 'Dr. Fatima Benali',
        type: 'THERAPY',
        status: 'IN_PROGRESS',
        scheduledAt: new Date().toISOString(),
        durationMinutes: 60,
        notes: 'Séance de thérapie EMDR',
        isVideoSession: false,
        createdAt: '2024-12-12T11:00:00'
      },
      {
        id: 4,
        seanceCode: 'SEA-004',
        patientId: 7,
        patientName: 'Zineb Idrissi',
        therapeuteId: 1,
        therapeuteName: 'Dr. Fatima Benali',
        type: 'FOLLOW_UP',
        status: 'SCHEDULED',
        scheduledAt: new Date(new Date().getTime() + 3600000).toISOString(),
        durationMinutes: 45,
        notes: 'Suivi mensuel TOC',
        isVideoSession: false,
        createdAt: '2024-12-13T08:00:00'
      },
      {
        id: 5,
        seanceCode: 'SEA-005',
        patientId: 10,
        patientName: 'Yassine Kettani',
        therapeuteId: 1,
        therapeuteName: 'Dr. Fatima Benali',
        type: 'CONSULTATION',
        status: 'SCHEDULED',
        scheduledAt: new Date(new Date().getTime() + 7200000).toISOString(),
        durationMinutes: 45,
        notes: 'Évaluation du sommeil',
        isVideoSession: false,
        createdAt: '2024-12-14T09:00:00'
      },
      {
        id: 6,
        seanceCode: 'SEA-006',
        patientId: 3,
        patientName: 'Leila Cherkaoui',
        therapeuteId: 2,
        therapeuteName: 'Dr. Youssef Alaoui',
        type: 'THERAPY',
        status: 'SCHEDULED',
        scheduledAt: new Date(new Date().getTime() + 10800000).toISOString(),
        durationMinutes: 60,
        notes: 'Gestion de la crise - agoraphobie',
        isVideoSession: false,
        createdAt: '2024-12-15T10:00:00'
      },
      {
        id: 7,
        seanceCode: 'SEA-007',
        patientId: 5,
        patientName: 'Nadia Berrada',
        therapeuteId: 2,
        therapeuteName: 'Dr. Youssef Alaoui',
        type: 'VIDEO',
        status: 'SCHEDULED',
        scheduledAt: new Date(new Date().getTime() + 14400000).toISOString(),
        durationMinutes: 60,
        notes: 'Suivi téléconsultation',
        isVideoSession: true,
        videoSessionUrl: 'https://meet.clinassist.ma/session-007',
        createdAt: '2024-12-15T11:00:00'
      },
      {
        id: 8,
        seanceCode: 'SEA-008',
        patientId: 8,
        patientName: 'Rachid Bennani',
        therapeuteId: 2,
        therapeuteName: 'Dr. Youssef Alaoui',
        type: 'CONSULTATION',
        status: 'SCHEDULED',
        scheduledAt: new Date(new Date().getTime() + 18000000).toISOString(),
        durationMinutes: 45,
        notes: 'Bilan de progression burnout',
        isVideoSession: false,
        createdAt: '2024-12-15T14:00:00'
      }
    ];

    return {
      content: demoSeances,
      totalElements: 8,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true
    };
  }

  /**
   * Get demo data filtered by therapeute ID
   */
  private getDemoDataForTherapeute(therapeuteId: number): PageResponse<Seance> {
    const allData = this.getDemoData();
    const filtered = allData.content.filter(s => s.therapeuteId === therapeuteId);
    
    return {
      content: filtered,
      totalElements: filtered.length,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true
    };
  }
}

