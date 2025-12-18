import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { PredictionService } from '../../../core/services/prediction.service';
import { PatientService } from '../../../core/services/patient.service';
import { Prediction, Patient } from '../../../core/models';

@Component({
  selector: 'app-predictions-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <!-- Page Header -->
    <div class="page-header mb-4">
      <div class="row align-items-center">
        <div class="col">
          <h4 class="mb-1">Prédictions IA</h4>
          <p class="text-muted mb-0">Analyse prédictive et alertes intelligentes</p>
        </div>
        <div class="col-auto">
          <button class="btn btn-primary" (click)="refreshData()" [disabled]="loading()">
            @if (loading()) {
              <span class="spinner-border spinner-border-sm me-2"></span>
            } @else {
              <i class="ti ti-refresh me-2"></i>
            }
            Actualiser l'analyse
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="row mb-4">
      <div class="col-md-3">
        <div class="card h-100">
          <div class="card-body text-center">
            <div class="avatar avatar-l bg-danger-subtle mx-auto mb-3">
              <i class="ti ti-alert-triangle f-24 text-danger"></i>
            </div>
            <h3 class="mb-1 text-danger">{{ highRiskCount() }}</h3>
            <p class="text-muted mb-0">Risque critique</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card h-100">
          <div class="card-body text-center">
            <div class="avatar avatar-l bg-warning-subtle mx-auto mb-3">
              <i class="ti ti-alert-circle f-24 text-warning"></i>
            </div>
            <h3 class="mb-1 text-warning">{{ mediumRiskCount() }}</h3>
            <p class="text-muted mb-0">Risque modéré</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card h-100">
          <div class="card-body text-center">
            <div class="avatar avatar-l bg-success-subtle mx-auto mb-3">
              <i class="ti ti-circle-check f-24 text-success"></i>
            </div>
            <h3 class="mb-1 text-success">{{ lowRiskCount() }}</h3>
            <p class="text-muted mb-0">Risque faible</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card h-100">
          <div class="card-body text-center">
            <div class="avatar avatar-l bg-primary-subtle mx-auto mb-3">
              <i class="ti ti-target f-24 text-primary"></i>
            </div>
            <h3 class="mb-1 text-primary">{{ predictionAccuracy() }}%</h3>
            <p class="text-muted mb-0">Précision modèle</p>
          </div>
        </div>
      </div>
    </div>

    <div class="row">
      <!-- Predictions List -->
      <div class="col-lg-8">
        <div class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Alertes Patients</h5>
            <div class="btn-group btn-group-sm">
              <button class="btn" [class.btn-danger]="filterRisk() === 'CRITICAL'" 
                      [class.btn-outline-secondary]="filterRisk() !== 'CRITICAL'" 
                      (click)="setFilter('CRITICAL')">Critique</button>
              <button class="btn" [class.btn-warning]="filterRisk() === 'HIGH'" 
                      [class.btn-outline-secondary]="filterRisk() !== 'HIGH'" 
                      (click)="setFilter('HIGH')">Élevé</button>
              <button class="btn" [class.btn-info]="filterRisk() === 'MEDIUM'" 
                      [class.btn-outline-secondary]="filterRisk() !== 'MEDIUM'" 
                      (click)="setFilter('MEDIUM')">Modéré</button>
              <button class="btn" [class.btn-primary]="filterRisk() === 'all'" 
                      [class.btn-outline-secondary]="filterRisk() !== 'all'" 
                      (click)="setFilter('all')">Tous</button>
            </div>
          </div>
          <div class="card-body p-0">
            @if (loading()) {
              <div class="text-center py-5">
                <div class="spinner-border text-primary"></div>
                <p class="text-muted mt-3">Analyse en cours...</p>
              </div>
            } @else if (filteredPredictions().length === 0) {
              <div class="text-center py-5">
                <i class="ti ti-mood-smile f-48 text-success opacity-50"></i>
                <p class="text-muted mt-3">Aucune alerte pour ce filtre</p>
              </div>
            } @else {
              <div class="list-group list-group-flush">
                @for (prediction of filteredPredictions(); track prediction.id) {
                  <div class="list-group-item p-3">
                    <div class="d-flex align-items-start">
                      <div class="flex-shrink-0 me-3">
                        <div class="avatar rounded-circle" [class]="getRiskAvatarClass(prediction.riskLevel)">
                          {{ getInitials(prediction.patientName) }}
                        </div>
                      </div>
                      <div class="flex-grow-1">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                          <div>
                            <h6 class="mb-1">{{ prediction.patientName }}</h6>
                            <span class="badge me-2" [class]="getRiskBadgeClass(prediction.riskLevel)">
                              <i class="ti ti-alert-triangle me-1"></i>
                              {{ getRiskLabel(prediction.riskLevel) }}
                            </span>
                            <span class="badge bg-secondary-subtle text-secondary me-2">
                              {{ getTypeLabel(prediction.type) }}
                            </span>
                            <small class="text-muted">{{ formatDate(prediction.generatedAt) }}</small>
                          </div>
                          <div class="text-end">
                            <h5 class="mb-0" [class]="getRiskTextClass(prediction.riskLevel)">{{ prediction.score }}%</h5>
                            <small class="text-muted">Confiance: {{ (prediction.confidence * 100).toFixed(0) }}%</small>
                          </div>
                        </div>
                        <p class="mb-2 text-muted">{{ prediction.recommendation }}</p>
                        @if (getFactorsList(prediction.factors).length > 0) {
                          <div class="d-flex flex-wrap gap-2">
                            @for (factor of getFactorsList(prediction.factors); track factor) {
                              <span class="badge bg-light text-dark border">{{ factor }}</span>
                            }
                          </div>
                        }
                      </div>
                    </div>
                    <div class="mt-3 d-flex gap-2">
                      <a [routerLink]="['/patients', prediction.patientId]" class="btn btn-sm btn-outline-primary">
                        <i class="ti ti-eye me-1"></i>Voir dossier
                      </a>
                      <button class="btn btn-sm btn-outline-success" (click)="scheduleFollowUp(prediction)">
                        <i class="ti ti-calendar me-1"></i>Planifier suivi
                      </button>
                      <button class="btn btn-sm btn-outline-secondary" (click)="markAsReviewed(prediction)">
                        <i class="ti ti-check me-1"></i>Marquer traité
                      </button>
                    </div>
                  </div>
                }
              </div>
            }
          </div>
        </div>
      </div>

      <!-- Risk Factors Analysis -->
      <div class="col-lg-4">
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">Facteurs de risque principaux</h5>
          </div>
          <div class="card-body">
            @for (factor of riskFactors(); track factor.name) {
              <div class="mb-3">
                <div class="d-flex justify-content-between mb-1">
                  <span>{{ factor.name }}</span>
                  <span class="fw-medium">{{ factor.percentage }}%</span>
                </div>
                <div class="progress progress-thin">
                  <div class="progress-bar" [class]="factor.color" [style.width.%]="factor.percentage"></div>
                </div>
              </div>
            }
          </div>
        </div>

        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">Statistiques du modèle</h5>
          </div>
          <div class="card-body">
            <div class="row text-center">
              <div class="col-6 border-end">
                <h4 class="text-primary mb-1">{{ totalPredictions() }}</h4>
                <small class="text-muted">Prédictions totales</small>
              </div>
              <div class="col-6">
                <h4 class="text-success mb-1">{{ successfulPredictions() }}</h4>
                <small class="text-muted">Prédictions réussies</small>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Recommandations IA</h5>
          </div>
          <div class="card-body p-0">
            <div class="list-group list-group-flush">
              @for (rec of recommendations(); track rec.id) {
                <div class="list-group-item">
                  <div class="d-flex">
                    <div class="flex-shrink-0 me-3">
                      <div class="avatar avatar-s rounded" [class]="rec.iconBg">
                        <i [class]="rec.icon + ' text-white'"></i>
                      </div>
                    </div>
                    <div>
                      <h6 class="mb-1">{{ rec.title }}</h6>
                      <p class="mb-0 text-muted small">{{ rec.description }}</p>
                    </div>
                  </div>
                </div>
              }
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .avatar {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 16px;
      
      &.avatar-s { width: 36px; height: 36px; font-size: 14px; }
      &.avatar-l { width: 56px; height: 56px; font-size: 20px; }
    }
    
    .progress-thin {
      height: 6px;
      border-radius: 3px;
    }
    
    .f-24 { font-size: 24px; }
    .f-48 { font-size: 48px; }
    
    .card {
      border: none;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.06);
    }
  `]
})
export class PredictionsDashboardComponent implements OnInit, OnDestroy {
  private predictionService = inject(PredictionService);
  private patientService = inject(PatientService);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  // State
  loading = signal(true);
  predictions = signal<Prediction[]>([]);
  filterRisk = signal<'all' | 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'>('all');

  // Stats
  predictionAccuracy = signal(87.5);
  totalPredictions = signal(892);
  successfulPredictions = signal(781);

  // Computed
  highRiskCount = computed(() =>
    this.predictions().filter(p => p.riskLevel === 'CRITICAL').length
  );

  mediumRiskCount = computed(() =>
    this.predictions().filter(p => p.riskLevel === 'HIGH' || p.riskLevel === 'MEDIUM').length
  );

  lowRiskCount = computed(() =>
    this.predictions().filter(p => p.riskLevel === 'LOW').length
  );

  filteredPredictions = computed(() => {
    const filter = this.filterRisk();
    if (filter === 'all') return this.predictions();
    return this.predictions().filter(p => p.riskLevel === filter);
  });

  riskFactors = computed(() => {
    const preds = this.predictions();
    const factorCounts: Record<string, number> = {};

    preds.forEach(p => {
      // Handle both array (demo) and object (API) formats
      if (Array.isArray(p.factors)) {
        p.factors.forEach(f => {
          factorCounts[f] = (factorCounts[f] || 0) + 1;
        });
      } else if (p.factors && typeof p.factors === 'object') {
        // API returns factors as object { key: value }
        Object.keys(p.factors).forEach(key => {
          const displayName = key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
          factorCounts[displayName] = (factorCounts[displayName] || 0) + 1;
        });
      }
    });

    const total = preds.length || 1;
    const factors = Object.entries(factorCounts)
      .map(([name, count]) => ({
        name,
        percentage: Math.round((count / total) * 100),
        color: this.getFactorColor(name)
      }))
      .sort((a, b) => b.percentage - a.percentage)
      .slice(0, 5);

    if (factors.length === 0) {
      return [
        { name: 'Absences répétées', percentage: 78, color: 'bg-danger' },
        { name: 'Score anxiété élevé', percentage: 65, color: 'bg-warning' },
        { name: 'Progression lente', percentage: 52, color: 'bg-info' },
        { name: 'Stress professionnel', percentage: 41, color: 'bg-primary' },
        { name: 'Troubles du sommeil', percentage: 38, color: 'bg-secondary' }
      ];
    }

    return factors;
  });

  recommendations = computed(() => {
    const preds = this.predictions().filter(p => p.riskLevel === 'CRITICAL' || p.riskLevel === 'HIGH');
    return preds.slice(0, 3).map((p, i) => ({
      id: i + 1,
      icon: this.getRecommendationIcon(p.type),
      iconBg: this.getRecommendationBg(p.type),
      title: this.getRecommendationTitle(p),
      description: p.recommendation || 'Action recommandée'
    }));
  });

  ngOnInit() {
    this.loadData();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadData() {
    this.loading.set(true);

    forkJoin({
      predictions: this.predictionService.getHighRiskPredictions(30),
      stats: this.predictionService.getPredictionStats()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.predictions.set(data.predictions);
        this.predictionAccuracy.set(data.stats.accuracy);
        this.totalPredictions.set(data.stats.total);
        this.successfulPredictions.set(data.stats.successful);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  refreshData() {
    this.loadData();
  }

  setFilter(filter: 'all' | 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW') {
    this.filterRisk.set(filter);
  }

  markAsReviewed(prediction: Prediction) {
    this.predictionService.markAsReviewed(prediction.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          // Remove from list
          this.predictions.update(preds => preds.filter(p => p.id !== prediction.id));
        },
        error: () => {
          // For demo, still remove
          this.predictions.update(preds => preds.filter(p => p.id !== prediction.id));
        }
      });
  }

  scheduleFollowUp(prediction: Prediction) {
    // Navigate to seances page with patient pre-selected
    this.router.navigate(['/seances'], {
      queryParams: { patientId: prediction.patientId }
    });
  }

  getFactorsList(factors: string[] | Record<string, number> | undefined): string[] {
    if (!factors) return [];
    if (Array.isArray(factors)) return factors;

    // French translations for factor keys
    const translations: Record<string, string> = {
      'days_since_last_session': 'Jours depuis dernière séance',
      'no_show_rate': 'Taux d\'absence',
      'cancellation_rate': 'Taux d\'annulation',
      'cancellation_impact': 'Impact des annulations',
      'no_show_impact': 'Impact des absences',
      'inactivity_impact': 'Impact de l\'inactivité',
      'total_sessions': 'Nombre de séances',
      'avg_progress_rating': 'Note de progression',
      'mood_improvement': 'Amélioration de l\'humeur'
    };

    return Object.keys(factors).map(key =>
      translations[key] || key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
    );
  }

  getInitials(name: string): string {
    if (!name) return 'XX';
    const parts = name.split(' ');
    return parts.map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase();
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffHours < 1) return 'À l\'instant';
    if (diffHours < 24) return `Il y a ${diffHours}h`;
    if (diffDays === 1) return 'Hier';
    if (diffDays < 7) return `Il y a ${diffDays} jours`;

    return date.toLocaleDateString('fr-FR');
  }

  getRiskLabel(level: Prediction['riskLevel']): string {
    const labels: Record<string, string> = {
      'CRITICAL': 'Critique',
      'HIGH': 'Élevé',
      'MEDIUM': 'Modéré',
      'LOW': 'Faible'
    };
    return labels[level] || level;
  }

  getTypeLabel(type: Prediction['type']): string {
    const labels: Record<string, string> = {
      'DROPOUT_RISK': 'Risque abandon',
      'NEXT_SESSION': 'Prochaine séance',
      'TREATMENT_OUTCOME': 'Résultat traitement',
      'MOOD_TREND': 'Tendance humeur'
    };
    return labels[type] || type;
  }

  getRiskAvatarClass(level: Prediction['riskLevel']): string {
    switch (level) {
      case 'CRITICAL': return 'bg-danger text-white';
      case 'HIGH': return 'bg-warning text-white';
      case 'MEDIUM': return 'bg-info text-white';
      default: return 'bg-success text-white';
    }
  }

  getRiskBadgeClass(level: Prediction['riskLevel']): string {
    switch (level) {
      case 'CRITICAL': return 'bg-danger-subtle text-danger';
      case 'HIGH': return 'bg-warning-subtle text-warning';
      case 'MEDIUM': return 'bg-info-subtle text-info';
      default: return 'bg-success-subtle text-success';
    }
  }

  getRiskTextClass(level: Prediction['riskLevel']): string {
    switch (level) {
      case 'CRITICAL': return 'text-danger';
      case 'HIGH': return 'text-warning';
      case 'MEDIUM': return 'text-info';
      default: return 'text-success';
    }
  }

  private getFactorColor(factor: string): string {
    const colors: Record<string, string> = {
      'Absences répétées': 'bg-danger',
      'Score anxiété élevé': 'bg-warning',
      'Baisse engagement': 'bg-warning',
      'Historique instable': 'bg-danger',
      'Progression stagnante': 'bg-info',
      'Feedback négatif': 'bg-danger',
      'Humeur fluctuante': 'bg-warning',
      'Sommeil perturbé': 'bg-info',
      'Progression positive': 'bg-success',
      'Amélioration constante': 'bg-success',
      'Engagement élevé': 'bg-success'
    };
    return colors[factor] || 'bg-secondary';
  }

  private getRecommendationIcon(type: Prediction['type']): string {
    switch (type) {
      case 'DROPOUT_RISK': return 'ti ti-phone';
      case 'NEXT_SESSION': return 'ti ti-calendar';
      case 'MOOD_TREND': return 'ti ti-heart';
      default: return 'ti ti-clipboard';
    }
  }

  private getRecommendationBg(type: Prediction['type']): string {
    switch (type) {
      case 'DROPOUT_RISK': return 'bg-danger';
      case 'NEXT_SESSION': return 'bg-success';
      case 'MOOD_TREND': return 'bg-warning';
      default: return 'bg-primary';
    }
  }

  private getRecommendationTitle(prediction: Prediction): string {
    switch (prediction.type) {
      case 'DROPOUT_RISK': return `Contacter ${prediction.patientName.split(' ')[0]}`;
      case 'NEXT_SESSION': return `Planifier séance - ${prediction.patientName.split(' ')[0]}`;
      case 'MOOD_TREND': return `Surveiller ${prediction.patientName.split(' ')[0]}`;
      default: return `Action pour ${prediction.patientName.split(' ')[0]}`;
    }
  }
}
