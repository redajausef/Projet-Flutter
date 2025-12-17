import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, of, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Therapeute, PageResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class TherapeuteService {
  private readonly apiUrl = `${environment.apiUrl}/therapeutes`;
  private http = inject(HttpClient);

  // State management
  private therapeutesSubject = new BehaviorSubject<Therapeute[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private currentTherapeuteSubject = new BehaviorSubject<Therapeute | null>(null);

  therapeutes$ = this.therapeutesSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();
  currentTherapeute$ = this.currentTherapeuteSubject.asObservable();

  // Signal for current therapeute
  currentTherapeute = signal<Therapeute | null>(null);

  /**
   * Get all therapeutes with pagination
   */
  getTherapeutes(page = 0, size = 10): Observable<PageResponse<Therapeute>> {
    this.loadingSubject.next(true);

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<Therapeute>>(this.apiUrl, { params }).pipe(
      tap(response => {
        this.therapeutesSubject.next(response.content);
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        console.error('Error fetching therapeutes:', error);
        this.loadingSubject.next(false);
        return of(this.getDemoData());
      })
    );
  }

  /**
   * Get therapeute by ID
   */
  getTherapeuteById(id: number): Observable<Therapeute> {
    return this.http.get<Therapeute>(`${this.apiUrl}/${id}`).pipe(
      catchError(() => of(this.getDemoData().content[0]))
    );
  }

  /**
   * Get available therapeutes
   */
  getAvailableTherapeutes(): Observable<Therapeute[]> {
    return this.http.get<Therapeute[]>(`${this.apiUrl}/available`).pipe(
      catchError(() => of(this.getDemoData().content.filter(t => t.available)))
    );
  }

  /**
   * Get therapeute schedule
   */
  getTherapeuteSchedule(id: number, date: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/schedule`, {
      params: new HttpParams().set('date', date)
    });
  }

  /**
   * Update therapeute availability
   */
  updateAvailability(id: number, available: boolean): Observable<Therapeute> {
    return this.http.patch<Therapeute>(`${this.apiUrl}/${id}/availability`, null, {
      params: new HttpParams().set('available', available.toString())
    });
  }

  /**
   * Get current logged-in therapeute profile
   */
  getCurrentTherapeute(): Observable<Therapeute> {
    return this.http.get<Therapeute>(`${this.apiUrl}/me`).pipe(
      tap(therapeute => {
        this.currentTherapeuteSubject.next(therapeute);
        this.currentTherapeute.set(therapeute);
        // Store in localStorage for persistence
        localStorage.setItem('currentTherapeute', JSON.stringify(therapeute));
      }),
      catchError(error => {
        console.error('Error fetching current therapeute:', error);
        // Try to load from localStorage
        const stored = localStorage.getItem('currentTherapeute');
        if (stored) {
          const therapeute = JSON.parse(stored) as Therapeute;
          this.currentTherapeute.set(therapeute);
          return of(therapeute);
        }
        return of(this.getDemoData().content[0]);
      })
    );
  }

  /**
   * Get therapeute by user ID
   */
  getTherapeuteByUserId(userId: number): Observable<Therapeute> {
    return this.http.get<Therapeute>(`${this.apiUrl}/user/${userId}`).pipe(
      tap(therapeute => {
        this.currentTherapeuteSubject.next(therapeute);
        this.currentTherapeute.set(therapeute);
        localStorage.setItem('currentTherapeute', JSON.stringify(therapeute));
      }),
      catchError(error => {
        console.error('Error fetching therapeute by user ID:', error);
        return of(this.getDemoData().content[0]);
      })
    );
  }

  /**
   * Get current therapeute ID (from signal, localStorage currentTherapeute, or localStorage therapeuteId)
   */
  getCurrentTherapeuteId(): number | null {
    // 1. Check signal first
    const current = this.currentTherapeute();
    if (current) return current.id;

    // 2. Check localStorage for full therapeute object
    const stored = localStorage.getItem('currentTherapeute');
    if (stored) {
      try {
        const therapeute = JSON.parse(stored) as Therapeute;
        this.currentTherapeute.set(therapeute);
        return therapeute.id;
      } catch (e) {
        console.error('Error parsing currentTherapeute:', e);
      }
    }

    // 3. Check localStorage for therapeuteId (set by AuthService after login)
    const therapeuteIdStr = localStorage.getItem('therapeuteId');
    if (therapeuteIdStr) {
      return parseInt(therapeuteIdStr, 10);
    }

    return null;
  }

  /**
   * Load current therapeute on app init
   */
  loadCurrentTherapeute(): void {
    const stored = localStorage.getItem('currentTherapeute');
    if (stored) {
      const therapeute = JSON.parse(stored) as Therapeute;
      this.currentTherapeute.set(therapeute);
      this.currentTherapeuteSubject.next(therapeute);
    }
  }

  /**
   * Clear current therapeute (on logout)
   */
  clearCurrentTherapeute(): void {
    this.currentTherapeute.set(null);
    this.currentTherapeuteSubject.next(null);
    localStorage.removeItem('currentTherapeute');
  }

  // Demo data for development - Moroccan names
  private getDemoData(): PageResponse<Therapeute> {
    const demoTherapeutes: Therapeute[] = [
      {
        id: 1,
        userId: 2,
        username: 'dr.benali',
        email: 'fatima.benali@clinassist.ma',
        firstName: 'Fatima',
        lastName: 'Benali',
        fullName: 'Dr. Fatima Benali',
        phoneNumber: '0661-234567',
        profileImageUrl: '',
        specialization: 'Psychologie clinique',
        licenseNumber: 'PSY-MA-2015-001',
        yearsOfExperience: 9,
        bio: 'Spécialiste en thérapie cognitive comportementale. Formée à l\'Université Mohammed V de Rabat.',
        available: true,
        totalPatients: 45,
        activePatients: 32,
        todaySeances: 4,
        weekSeances: 18,
        rating: 4.8,
        createdAt: '2020-03-15T10:00:00'
      },
      {
        id: 2,
        userId: 3,
        username: 'dr.alaoui',
        email: 'youssef.alaoui@clinassist.ma',
        firstName: 'Youssef',
        lastName: 'Alaoui',
        fullName: 'Dr. Youssef Alaoui',
        phoneNumber: '0662-345678',
        profileImageUrl: '',
        specialization: 'Psychiatrie',
        licenseNumber: 'PSY-MA-2012-042',
        yearsOfExperience: 12,
        bio: 'Psychiatre spécialisé dans les troubles de l\'humeur. Chef de service au CHU Ibn Sina.',
        available: true,
        totalPatients: 38,
        activePatients: 28,
        todaySeances: 3,
        weekSeances: 15,
        rating: 4.9,
        createdAt: '2018-06-20T09:00:00'
      },
      {
        id: 3,
        userId: 4,
        username: 'dr.hassani',
        email: 'amina.hassani@clinassist.ma',
        firstName: 'Amina',
        lastName: 'Hassani',
        fullName: 'Dr. Amina Hassani',
        phoneNumber: '0663-456789',
        profileImageUrl: '',
        specialization: 'Psychothérapie familiale',
        licenseNumber: 'PSY-MA-2018-089',
        yearsOfExperience: 6,
        bio: 'Thérapeute familiale et de couple, approche systémique.',
        available: true,
        totalPatients: 25,
        activePatients: 18,
        todaySeances: 2,
        weekSeances: 12,
        rating: 4.7,
        createdAt: '2021-01-10T14:00:00'
      },
      {
        id: 4,
        userId: 5,
        username: 'dr.tazi',
        email: 'omar.tazi@clinassist.ma',
        firstName: 'Omar',
        lastName: 'Tazi',
        fullName: 'Dr. Omar Tazi',
        phoneNumber: '0664-567890',
        profileImageUrl: '',
        specialization: 'Neuropsychologie',
        licenseNumber: 'PSY-MA-2016-056',
        yearsOfExperience: 8,
        bio: 'Neuropsychologue spécialisé dans l\'évaluation cognitive. CHU Hassan II de Fès.',
        available: false,
        totalPatients: 30,
        activePatients: 22,
        todaySeances: 0,
        weekSeances: 10,
        rating: 4.6,
        createdAt: '2019-09-05T11:00:00'
      }
    ];

    return {
      content: demoTherapeutes,
      totalElements: 4,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true
    };
  }
}

