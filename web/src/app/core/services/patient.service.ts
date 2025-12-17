import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, of, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Patient, PatientCreateRequest, PageResponse } from '../models';
import { AuthService } from './auth.service';
import { TherapeuteService } from './therapeute.service';

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private readonly apiUrl = `${environment.apiUrl}/patients`;
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private therapeuteService = inject(TherapeuteService);
  
  // State management
  private patientsSubject = new BehaviorSubject<Patient[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private selectedPatientSubject = new BehaviorSubject<Patient | null>(null);
  
  patients$ = this.patientsSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();
  selectedPatient$ = this.selectedPatientSubject.asObservable();

  /**
   * Get all patients with pagination - filtered by current therapeute
   */
  getPatients(page = 0, size = 10, search?: string): Observable<PageResponse<Patient>> {
    this.loadingSubject.next(true);
    
    // Check if current user is a therapeute - filter by their patients only
    const therapeuteId = this.therapeuteService.getCurrentTherapeuteId();
    
    if (therapeuteId) {
      // Therapeute sees only their patients
      return this.getPatientsByTherapeute(therapeuteId).pipe(
        map(patients => {
          let filtered = patients;
          
          // Apply search filter
          if (search) {
            const term = search.toLowerCase();
            filtered = patients.filter(p => 
              p.firstName?.toLowerCase().includes(term) || 
              p.lastName?.toLowerCase().includes(term) ||
              p.email?.toLowerCase().includes(term) ||
              p.patientCode?.toLowerCase().includes(term)
            );
          }
          
          // Apply pagination
          const start = page * size;
          const paged = filtered.slice(start, start + size);
          
          this.patientsSubject.next(paged);
          this.loadingSubject.next(false);
          
          return {
            content: paged,
            totalElements: filtered.length,
            totalPages: Math.ceil(filtered.length / size),
            size: size,
            number: page,
            first: page === 0,
            last: start + size >= filtered.length
          };
        }),
        catchError(error => {
          console.error('Error fetching patients:', error);
          this.loadingSubject.next(false);
          throw error;
        })
      );
    }
    
    // Admin or no therapeute context - get all patients
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    const endpoint = search 
      ? `${this.apiUrl}/search` 
      : this.apiUrl;
    
    if (search) {
      params = params.set('q', search);
    }
    
    return this.http.get<PageResponse<Patient>>(endpoint, { params }).pipe(
      tap(response => {
        this.patientsSubject.next(response.content);
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        console.error('Error fetching patients:', error);
        this.loadingSubject.next(false);
        throw error;
      })
    );
  }

  /**
   * Get patient by ID
   */
  getPatientById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.apiUrl}/${id}`).pipe(
      tap(patient => this.selectedPatientSubject.next(patient)),
      catchError(error => {
        console.error('Error fetching patient:', error);
        throw error;
      })
    );
  }

  /**
   * Get patient by code
   */
  getPatientByCode(code: string): Observable<Patient> {
    return this.http.get<Patient>(`${this.apiUrl}/code/${code}`);
  }

  /**
   * Get patients by therapeute
   */
  getPatientsByTherapeute(therapeuteId: number): Observable<Patient[]> {
    return this.http.get<Patient[]>(`${this.apiUrl}/therapeute/${therapeuteId}`).pipe(
      catchError(error => {
        console.error('Error fetching patients by therapeute:', error);
        throw error;
      })
    );
  }

  /**
   * Get high risk patients
   */
  getHighRiskPatients(minRisk = 70): Observable<Patient[]> {
    return this.http.get<Patient[]>(`${this.apiUrl}/high-risk`, {
      params: new HttpParams().set('minRisk', minRisk.toString())
    }).pipe(
      catchError(() => of([]))
    );
  }

  /**
   * Create a new patient
   */
  createPatient(patient: PatientCreateRequest): Observable<Patient> {
    return this.http.post<Patient>(this.apiUrl, patient).pipe(
      tap(newPatient => {
        const currentPatients = this.patientsSubject.getValue();
        this.patientsSubject.next([newPatient, ...currentPatients]);
      })
    );
  }

  /**
   * Update patient
   */
  updatePatient(id: number, patient: Partial<Patient>): Observable<Patient> {
    return this.http.put<Patient>(`${this.apiUrl}/${id}`, patient).pipe(
      tap(updatedPatient => {
        const currentPatients = this.patientsSubject.getValue();
        const index = currentPatients.findIndex(p => p.id === id);
        if (index !== -1) {
          currentPatients[index] = updatedPatient;
          this.patientsSubject.next([...currentPatients]);
        }
        if (this.selectedPatientSubject.getValue()?.id === id) {
          this.selectedPatientSubject.next(updatedPatient);
        }
      })
    );
  }

  /**
   * Update patient status
   */
  updatePatientStatus(id: number, status: Patient['status']): Observable<Patient> {
    return this.http.patch<Patient>(`${this.apiUrl}/${id}/status`, null, {
      params: new HttpParams().set('status', status)
    }).pipe(
      tap(updatedPatient => {
        const currentPatients = this.patientsSubject.getValue();
        const index = currentPatients.findIndex(p => p.id === id);
        if (index !== -1) {
          currentPatients[index] = updatedPatient;
          this.patientsSubject.next([...currentPatients]);
        }
      })
    );
  }

  /**
   * Assign therapeute to patient
   */
  assignTherapeute(patientId: number, therapeuteId: number): Observable<Patient> {
    return this.http.patch<Patient>(`${this.apiUrl}/${patientId}/assign/${therapeuteId}`, null);
  }

  /**
   * Clear selected patient
   */
  clearSelection(): void {
    this.selectedPatientSubject.next(null);
  }

  // Demo data for development - Moroccan names
  private getDemoData(): PageResponse<Patient> {
    const demoPatients: Patient[] = [
      {
        id: 1,
        patientCode: 'PAT-001',
        userId: 6,
        username: 'sara.ouazzani',
        email: 'sara.ouazzani@gmail.com',
        firstName: 'Sara',
        lastName: 'Ouazzani',
        fullName: 'Sara Ouazzani',
        phoneNumber: '0665-123456',
        dateOfBirth: '1992-03-15',
        age: 32,
        gender: 'FEMALE',
        address: '25 Avenue Hassan II',
        city: 'Casablanca',
        postalCode: '20000',
        status: 'ACTIVE',
        assignedTherapeuteId: 1,
        assignedTherapeuteName: 'Dr. Fatima Benali',
        riskScore: 25,
        riskCategory: 'LOW',
        totalSeances: 12,
        completedSeances: 10,
        createdAt: '2024-01-15T10:00:00',
        updatedAt: '2024-12-01T14:30:00'
      },
      {
        id: 2,
        patientCode: 'PAT-002',
        userId: 7,
        username: 'karim.benjelloun',
        email: 'karim.benjelloun@gmail.com',
        firstName: 'Karim',
        lastName: 'Benjelloun',
        fullName: 'Karim Benjelloun',
        phoneNumber: '0666-234567',
        dateOfBirth: '1988-07-22',
        age: 36,
        gender: 'MALE',
        address: '14 Rue Ibn Batouta',
        city: 'Rabat',
        status: 'ACTIVE',
        assignedTherapeuteId: 1,
        assignedTherapeuteName: 'Dr. Fatima Benali',
        riskScore: 45,
        riskCategory: 'MEDIUM',
        totalSeances: 8,
        completedSeances: 6,
        createdAt: '2024-02-20T09:00:00',
        updatedAt: '2024-11-28T16:00:00'
      },
      {
        id: 3,
        patientCode: 'PAT-003',
        userId: 8,
        username: 'leila.cherkaoui',
        email: 'leila.cherkaoui@gmail.com',
        firstName: 'Leila',
        lastName: 'Cherkaoui',
        fullName: 'Leila Cherkaoui',
        phoneNumber: '0667-345678',
        dateOfBirth: '1985-11-08',
        age: 39,
        gender: 'FEMALE',
        address: '8 Boulevard Zerktouni',
        city: 'Marrakech',
        status: 'ON_HOLD',
        assignedTherapeuteId: 2,
        assignedTherapeuteName: 'Dr. Youssef Alaoui',
        riskScore: 78,
        riskCategory: 'HIGH',
        totalSeances: 20,
        completedSeances: 18,
        createdAt: '2023-06-10T11:00:00',
        updatedAt: '2024-10-15T10:00:00'
      },
      {
        id: 4,
        patientCode: 'PAT-004',
        userId: 9,
        username: 'mehdi.fassi',
        email: 'mehdi.fassi@gmail.com',
        firstName: 'Mehdi',
        lastName: 'Fassi Fihri',
        fullName: 'Mehdi Fassi Fihri',
        phoneNumber: '0668-456789',
        dateOfBirth: '1995-01-30',
        age: 29,
        gender: 'MALE',
        address: '33 Avenue Mohammed V',
        city: 'Fès',
        status: 'ACTIVE',
        assignedTherapeuteId: 1,
        assignedTherapeuteName: 'Dr. Fatima Benali',
        riskScore: 15,
        riskCategory: 'LOW',
        totalSeances: 4,
        completedSeances: 4,
        createdAt: '2024-10-01T14:00:00',
        updatedAt: '2024-12-10T09:00:00'
      },
      {
        id: 5,
        patientCode: 'PAT-005',
        userId: 10,
        username: 'nadia.berrada',
        email: 'nadia.berrada@gmail.com',
        firstName: 'Nadia',
        lastName: 'Berrada',
        fullName: 'Nadia Berrada',
        phoneNumber: '0669-567890',
        dateOfBirth: '1990-09-12',
        age: 34,
        gender: 'FEMALE',
        address: '7 Rue Allal Ben Abdellah',
        city: 'Tanger',
        status: 'ACTIVE',
        assignedTherapeuteId: 2,
        assignedTherapeuteName: 'Dr. Youssef Alaoui',
        riskScore: 85,
        riskCategory: 'CRITICAL',
        totalSeances: 15,
        completedSeances: 12,
        createdAt: '2024-03-05T08:00:00',
        updatedAt: '2024-12-14T11:00:00'
      },
      {
        id: 6,
        patientCode: 'PAT-006',
        userId: 11,
        username: 'amine.lahlou',
        email: 'amine.lahlou@gmail.com',
        firstName: 'Amine',
        lastName: 'Lahlou',
        fullName: 'Amine Lahlou',
        phoneNumber: '0670-678901',
        dateOfBirth: '1993-05-25',
        age: 31,
        gender: 'MALE',
        address: '19 Avenue des FAR',
        city: 'Agadir',
        status: 'ACTIVE',
        assignedTherapeuteId: 3,
        assignedTherapeuteName: 'Dr. Amina Hassani',
        riskScore: 35,
        riskCategory: 'MEDIUM',
        totalSeances: 10,
        completedSeances: 8,
        createdAt: '2024-04-10T09:00:00',
        updatedAt: '2024-12-12T10:00:00'
      },
      {
        id: 7,
        patientCode: 'PAT-007',
        userId: 12,
        username: 'zineb.idrissi',
        email: 'zineb.idrissi@gmail.com',
        firstName: 'Zineb',
        lastName: 'Idrissi',
        fullName: 'Zineb Idrissi',
        phoneNumber: '0671-789012',
        dateOfBirth: '1987-12-03',
        age: 37,
        gender: 'FEMALE',
        address: '42 Rue de la Liberté',
        city: 'Meknès',
        status: 'ACTIVE',
        assignedTherapeuteId: 1,
        assignedTherapeuteName: 'Dr. Fatima Benali',
        riskScore: 55,
        riskCategory: 'MEDIUM',
        totalSeances: 14,
        completedSeances: 12,
        createdAt: '2024-02-15T11:00:00',
        updatedAt: '2024-12-10T15:00:00'
      }
    ];

    return {
      content: demoPatients,
      totalElements: 12,
      totalPages: 2,
      size: 10,
      number: 0,
      first: true,
      last: false
    };
  }

  private getDemoPatient(id: number): Patient {
    return this.getDemoData().content.find(p => p.id === id) || this.getDemoData().content[0];
  }

  /**
   * Get demo data filtered by therapeute ID
   */
  private getDemoDataForTherapeute(therapeuteId: number): PageResponse<Patient> {
    const allData = this.getDemoData();
    const filtered = allData.content.filter(p => p.assignedTherapeuteId === therapeuteId);
    
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

