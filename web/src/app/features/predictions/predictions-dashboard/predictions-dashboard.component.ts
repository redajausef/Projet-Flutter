import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-predictions-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Page Header -->
    <div class="page-header mb-4">
      <div class="row align-items-center">
        <div class="col">
          <h4 class="mb-1">Prédictions IA</h4>
          <p class="text-muted mb-0">Analyse prédictive et alertes intelligentes</p>
        </div>
        <div class="col-auto">
          <button class="btn btn-primary">
            <i class="ti ti-refresh me-2"></i>Actualiser l'analyse
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="row mb-4">
      <div class="col-md-3">
        <div class="card">
          <div class="card-body text-center">
            <div class="avatar avatar-l bg-danger-subtle mx-auto mb-3">
              <i class="ti ti-alert-triangle f-24 text-danger"></i>
            </div>
            <h3 class="mb-1 text-danger">{{ highRiskCount() }}</h3>
            <p class="text-muted mb-0">Risque élevé</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card">
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
        <div class="card">
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
        <div class="card">
          <div class="card-body text-center">
            <div class="avatar avatar-l bg-primary-subtle mx-auto mb-3">
              <i class="ti ti-brain f-24 text-primary"></i>
            </div>
            <h3 class="mb-1 text-primary">94%</h3>
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
              <button class="btn" [class.btn-danger]="filterRisk === 'high'" [class.btn-outline-secondary]="filterRisk !== 'high'" (click)="filterRisk = 'high'">Élevé</button>
              <button class="btn" [class.btn-warning]="filterRisk === 'medium'" [class.btn-outline-secondary]="filterRisk !== 'medium'" (click)="filterRisk = 'medium'">Modéré</button>
              <button class="btn" [class.btn-success]="filterRisk === 'low'" [class.btn-outline-secondary]="filterRisk !== 'low'" (click)="filterRisk = 'low'">Faible</button>
              <button class="btn" [class.btn-primary]="filterRisk === 'all'" [class.btn-outline-secondary]="filterRisk !== 'all'" (click)="filterRisk = 'all'">Tous</button>
            </div>
          </div>
          <div class="card-body p-0">
            <div class="list-group list-group-flush">
              @for (prediction of filteredPredictions(); track prediction.id) {
                <div class="list-group-item p-3">
                  <div class="d-flex align-items-start">
                    <div class="flex-shrink-0 me-3">
                      <div class="avatar rounded-circle" [class]="getRiskAvatarClass(prediction.riskLevel)">
                        {{ prediction.initials }}
                      </div>
                    </div>
                    <div class="flex-grow-1">
                      <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                          <h6 class="mb-1">{{ prediction.patientName }}</h6>
                          <span class="badge me-2" [class]="getRiskBadgeClass(prediction.riskLevel)">
                            <i class="ti ti-alert-triangle me-1"></i>
                            Risque {{ prediction.riskLevel === 'high' ? 'élevé' : prediction.riskLevel === 'medium' ? 'modéré' : 'faible' }}
                          </span>
                          <small class="text-muted">{{ prediction.date }}</small>
                        </div>
                        <div class="text-end">
                          <h5 class="mb-0" [class]="getRiskTextClass(prediction.riskLevel)">{{ prediction.score }}%</h5>
                          <small class="text-muted">Score de risque</small>
                        </div>
                      </div>
                      <p class="mb-2 text-muted">{{ prediction.message }}</p>
                      <div class="d-flex gap-2">
                        @for (factor of prediction.factors; track factor) {
                          <span class="badge bg-light text-dark">{{ factor }}</span>
                        }
                      </div>
                    </div>
                  </div>
                  <div class="mt-3 d-flex gap-2">
                    <button class="btn btn-sm btn-outline-primary">
                      <i class="ti ti-eye me-1"></i>Voir dossier
                    </button>
                    <button class="btn btn-sm btn-outline-success">
                      <i class="ti ti-calendar me-1"></i>Planifier suivi
                    </button>
                    <button class="btn btn-sm btn-outline-secondary">
                      <i class="ti ti-check me-1"></i>Marquer traité
                    </button>
                  </div>
                </div>
              }
            </div>
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
            @for (factor of riskFactors; track factor.name) {
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

        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Recommandations IA</h5>
          </div>
          <div class="card-body p-0">
            <div class="list-group list-group-flush">
              @for (rec of recommendations; track rec.id) {
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
  `]
})
export class PredictionsDashboardComponent {
  filterRisk = 'all';

  predictions = [
    { id: 1, patientName: 'Sophie Bernard', initials: 'SB', score: 82, riskLevel: 'high', date: 'Il y a 2h', message: 'Risque d\'abandon thérapeutique détecté - 3 absences consécutives et baisse d\'engagement', factors: ['Absences', 'Engagement', 'Historique'] },
    { id: 2, patientName: 'Thomas Petit', initials: 'TP', score: 68, riskLevel: 'high', date: 'Il y a 4h', message: 'Score d\'anxiété en hausse significative depuis les 2 dernières séances', factors: ['Anxiété', 'Progression', 'Sommeil'] },
    { id: 3, patientName: 'Emma Garcia', initials: 'EG', score: 54, riskLevel: 'medium', date: 'Hier', message: 'Progression ralentie détectée - stagnation depuis 4 semaines', factors: ['Progression', 'Motivation'] },
    { id: 4, patientName: 'Lucas Martin', initials: 'LM', score: 45, riskLevel: 'medium', date: 'Hier', message: 'Indicateurs de stress professionnel en augmentation', factors: ['Stress', 'Travail'] },
    { id: 5, patientName: 'Marie Dupont', initials: 'MD', score: 25, riskLevel: 'low', date: 'Il y a 3 jours', message: 'Évolution positive maintenue - continuer le protocole actuel', factors: ['Progression'] },
    { id: 6, patientName: 'Jean Martin', initials: 'JM', score: 18, riskLevel: 'low', date: 'Il y a 3 jours', message: 'Stabilisation des indicateurs - proche de la fin de suivi', factors: ['Stabilité'] },
  ];

  riskFactors = [
    { name: 'Absences répétées', percentage: 78, color: 'bg-danger' },
    { name: 'Score anxiété élevé', percentage: 65, color: 'bg-warning' },
    { name: 'Progression lente', percentage: 52, color: 'bg-info' },
    { name: 'Stress professionnel', percentage: 41, color: 'bg-primary' },
    { name: 'Troubles du sommeil', percentage: 38, color: 'bg-secondary' },
  ];

  recommendations = [
    { id: 1, icon: 'ti ti-phone', iconBg: 'bg-primary', title: 'Contacter Sophie B.', description: 'Appel de suivi recommandé suite aux absences' },
    { id: 2, icon: 'ti ti-calendar', iconBg: 'bg-success', title: 'Ajuster fréquence', description: 'Augmenter la fréquence pour Thomas P.' },
    { id: 3, icon: 'ti ti-clipboard', iconBg: 'bg-warning', title: 'Évaluation complète', description: 'Planifier bilan pour Emma G.' },
  ];

  highRiskCount = signal(2);
  mediumRiskCount = signal(2);
  lowRiskCount = signal(2);

  filteredPredictions() {
    if (this.filterRisk === 'all') return this.predictions;
    return this.predictions.filter(p => p.riskLevel === this.filterRisk);
  }

  getRiskAvatarClass(level: string): string {
    switch (level) {
      case 'high': return 'bg-danger text-white';
      case 'medium': return 'bg-warning text-white';
      default: return 'bg-success text-white';
    }
  }

  getRiskBadgeClass(level: string): string {
    switch (level) {
      case 'high': return 'bg-danger-subtle text-danger';
      case 'medium': return 'bg-warning-subtle text-warning';
      default: return 'bg-success-subtle text-success';
    }
  }

  getRiskTextClass(level: string): string {
    switch (level) {
      case 'high': return 'text-danger';
      case 'medium': return 'text-warning';
      default: return 'text-success';
    }
  }
}
