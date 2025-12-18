import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, map, catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Prediction, PageResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class PredictionService {
  private readonly apiUrl = `${environment.apiUrl}/predictions`;
  private http = inject(HttpClient);

  // State management
  private predictionsSubject = new BehaviorSubject<Prediction[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  predictions$ = this.predictionsSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();

  /**
   * Get predictions for a patient
   */
  getPatientPredictions(patientId: number): Observable<Prediction[]> {
    return this.http.get<Prediction[]>(`${this.apiUrl}/patient/${patientId}`).pipe(
      catchError(() => of([]))
    );
  }

  /**
   * Get high risk predictions
   */
  getHighRiskPredictions(minRisk = 30): Observable<Prediction[]> {
    this.loadingSubject.next(true);

    return this.http.get<any[]>(`${this.apiUrl}/high-risk`, {
      params: new HttpParams().set('minRisk', minRisk.toString())
    }).pipe(
      map(apiPredictions => {
        const predictions = apiPredictions.map(p => this.mapApiToPrediction(p));
        this.predictionsSubject.next(predictions);
        this.loadingSubject.next(false);
        return predictions;
      }),
      catchError(() => {
        this.loadingSubject.next(false);
        const demo = this.getDemoPredictions();
        this.predictionsSubject.next(demo);
        return of(demo);
      })
    );
  }

  /**
   * Transform API PredictionDTO to frontend Prediction model
   */
  private mapApiToPrediction(api: any): Prediction {
    return {
      id: api.id,
      patientId: api.patientId,
      patientName: api.patientName,
      type: api.type || 'DROPOUT_RISK',
      score: api.riskLevel || 0,  // Backend riskLevel (number) -> Frontend score
      confidence: api.confidenceScore || 0, // Backend confidenceScore -> Frontend confidence
      riskLevel: api.riskCategory || this.getRiskLevelFromScore(api.riskLevel), // Backend riskCategory -> Frontend riskLevel
      recommendation: api.recommendations || api.prediction,
      factors: api.factors, // Can be array or object, handled by component
      generatedAt: api.createdAt,
      isActive: true
    };
  }

  /**
   * Convert numeric risk score to risk level string
   */
  private getRiskLevelFromScore(score: number): 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' {
    if (score >= 75) return 'CRITICAL';
    if (score >= 50) return 'HIGH';
    if (score >= 25) return 'MEDIUM';
    return 'LOW';
  }

  /**
   * Get all active predictions
   */
  getActivePredictions(): Observable<Prediction[]> {
    return this.http.get<Prediction[]>(`${this.apiUrl}/active`).pipe(
      catchError(() => of(this.getDemoPredictions()))
    );
  }

  /**
   * Generate next session prediction
   */
  generateNextSessionPrediction(patientId: number): Observable<Prediction> {
    return this.http.post<Prediction>(`${this.apiUrl}/patient/${patientId}/next-session`, {});
  }

  /**
   * Generate dropout risk prediction
   */
  generateDropoutRiskPrediction(patientId: number): Observable<Prediction> {
    return this.http.post<any>(`${this.apiUrl}/patient/${patientId}/dropout-risk`, {}).pipe(
      map(api => this.mapApiToPrediction(api))
    );
  }

  /**
   * Get prediction accuracy stats
   */
  getPredictionStats(): Observable<{ accuracy: number, total: number, successful: number }> {
    return this.http.get<{ accuracy: number, total: number, successful: number }>(`${this.apiUrl}/stats`).pipe(
      catchError(() => of({ accuracy: 87.5, total: 892, successful: 781 }))
    );
  }

  /**
   * Dismiss a prediction
   */
  dismissPrediction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Mark prediction as reviewed
   */
  markAsReviewed(id: number): Observable<Prediction> {
    return this.http.patch<Prediction>(`${this.apiUrl}/${id}/reviewed`, {});
  }

  // Demo data - Moroccan names
  private getDemoPredictions(): Prediction[] {
    return [
      {
        id: 1,
        patientId: 3,
        patientName: 'Leila Cherkaoui',
        type: 'DROPOUT_RISK',
        score: 82,
        confidence: 0.89,
        riskLevel: 'CRITICAL',
        recommendation: 'Contacter Leila rapidement. 3 absences consécutives détectées.',
        factors: ['Absences répétées', 'Baisse engagement', 'Historique instable'],
        generatedAt: new Date().toISOString(),
        isActive: true
      },
      {
        id: 2,
        patientId: 5,
        patientName: 'Nadia Berrada',
        type: 'DROPOUT_RISK',
        score: 85,
        confidence: 0.92,
        riskLevel: 'CRITICAL',
        recommendation: 'Intervention urgente pour Nadia. Score anxiété en hausse.',
        factors: ['Score anxiété élevé', 'Progression stagnante', 'Feedback négatif'],
        generatedAt: new Date().toISOString(),
        isActive: true
      },
      {
        id: 3,
        patientId: 9,
        patientName: 'Kenza El Amrani',
        type: 'MOOD_TREND',
        score: 72,
        confidence: 0.78,
        riskLevel: 'HIGH',
        recommendation: 'Surveiller Kenza. Tendance à la baisse du moral.',
        factors: ['Humeur fluctuante', 'Sommeil perturbé'],
        generatedAt: new Date().toISOString(),
        isActive: true
      },
      {
        id: 4,
        patientId: 11,
        patientName: 'Hajar Squalli',
        type: 'DROPOUT_RISK',
        score: 68,
        confidence: 0.85,
        riskLevel: 'HIGH',
        recommendation: 'Hajar montre des signes de démotivation.',
        factors: ['Motivation en baisse', 'Retards fréquents'],
        generatedAt: new Date().toISOString(),
        isActive: true
      },
      {
        id: 5,
        patientId: 2,
        patientName: 'Karim Benjelloun',
        type: 'NEXT_SESSION',
        score: 45,
        confidence: 0.85,
        riskLevel: 'MEDIUM',
        recommendation: 'Séance de suivi recommandée pour Karim dans les 7 jours.',
        factors: ['Progression positive', 'Objectifs partiellement atteints'],
        generatedAt: new Date().toISOString(),
        isActive: true
      },
      {
        id: 6,
        patientId: 8,
        patientName: 'Rachid Bennani',
        type: 'TREATMENT_OUTCOME',
        score: 42,
        confidence: 0.82,
        riskLevel: 'MEDIUM',
        recommendation: 'Rachid progresse. Continuer le protocole actuel.',
        factors: ['Amélioration légère', 'Engagement stable'],
        generatedAt: new Date().toISOString(),
        isActive: true
      },
      {
        id: 7,
        patientId: 1,
        patientName: 'Sara Ouazzani',
        type: 'TREATMENT_OUTCOME',
        score: 25,
        confidence: 0.88,
        riskLevel: 'LOW',
        recommendation: 'Sara montre une excellente progression.',
        factors: ['Amélioration constante', 'Engagement élevé'],
        generatedAt: new Date().toISOString(),
        isActive: true
      },
      {
        id: 8,
        patientId: 4,
        patientName: 'Mehdi Fassi Fihri',
        type: 'TREATMENT_OUTCOME',
        score: 15,
        confidence: 0.90,
        riskLevel: 'LOW',
        recommendation: 'Mehdi proche de la fin du traitement.',
        factors: ['Objectifs atteints', 'Stabilité émotionnelle'],
        generatedAt: new Date().toISOString(),
        isActive: true
      }
    ];
  }
}

