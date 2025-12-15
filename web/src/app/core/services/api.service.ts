import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Dashboard
  getDashboardStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/dashboard/stats`);
  }

  // Patients
  getPatients(page = 0, size = 10, search?: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (search) {
      return this.http.get(`${this.apiUrl}/patients/search`, {
        params: params.set('q', search)
      });
    }
    
    return this.http.get(`${this.apiUrl}/patients`, { params });
  }

  getPatientById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/patients/${id}`);
  }

  getHighRiskPatients(minRisk = 70): Observable<any> {
    return this.http.get(`${this.apiUrl}/patients/high-risk`, {
      params: new HttpParams().set('minRisk', minRisk.toString())
    });
  }

  // Therapeutes
  getTherapeutes(page = 0, size = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/therapeutes`, {
      params: new HttpParams()
        .set('page', page.toString())
        .set('size', size.toString())
    });
  }

  // Seances
  getSeances(page = 0, size = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/seances`, {
      params: new HttpParams()
        .set('page', page.toString())
        .set('size', size.toString())
    });
  }

  getUpcomingSeances(): Observable<any> {
    return this.http.get(`${this.apiUrl}/seances/upcoming`);
  }

  getSeancesByDateRange(start: string, end: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/seances/range`, {
      params: new HttpParams()
        .set('start', start)
        .set('end', end)
    });
  }

  createSeance(seance: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/seances`, seance);
  }

  updateSeanceStatus(id: number, status: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/seances/${id}/status`, null, {
      params: new HttpParams().set('status', status)
    });
  }

  // Predictions
  getPatientPredictions(patientId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/predictions/patient/${patientId}`);
  }

  getHighRiskPredictions(minRisk = 70): Observable<any> {
    return this.http.get(`${this.apiUrl}/predictions/high-risk`, {
      params: new HttpParams().set('minRisk', minRisk.toString())
    });
  }

  generateNextSessionPrediction(patientId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/predictions/patient/${patientId}/next-session`, {});
  }

  generateDropoutRiskPrediction(patientId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/predictions/patient/${patientId}/dropout-risk`, {});
  }
}

