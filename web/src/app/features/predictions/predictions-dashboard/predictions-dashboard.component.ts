import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-predictions-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6 animate-fade-in">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-text-primary mb-2">Analyses Prédictives</h1>
          <p class="text-text-secondary">Intelligence artificielle au service du soin</p>
        </div>
        <button class="btn-accent flex items-center gap-2">
          <span class="material-icons">psychology</span>
          Nouvelle Analyse
        </button>
      </div>

      <!-- Stats Overview -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="card text-center">
          <div class="w-14 h-14 rounded-xl bg-primary/20 flex items-center justify-center mx-auto mb-4">
            <span class="material-icons text-primary text-2xl">analytics</span>
          </div>
          <p class="text-3xl font-bold text-text-primary">94%</p>
          <p class="text-sm text-text-muted">Précision globale</p>
        </div>
        <div class="card text-center">
          <div class="w-14 h-14 rounded-xl bg-success/20 flex items-center justify-center mx-auto mb-4">
            <span class="material-icons text-success text-2xl">check_circle</span>
          </div>
          <p class="text-3xl font-bold text-text-primary">247</p>
          <p class="text-sm text-text-muted">Prédictions validées</p>
        </div>
        <div class="card text-center">
          <div class="w-14 h-14 rounded-xl bg-warning/20 flex items-center justify-center mx-auto mb-4">
            <span class="material-icons text-warning text-2xl">warning</span>
          </div>
          <p class="text-3xl font-bold text-text-primary">{{ highRiskPredictions().length }}</p>
          <p class="text-sm text-text-muted">Alertes actives</p>
        </div>
        <div class="card text-center">
          <div class="w-14 h-14 rounded-xl bg-info/20 flex items-center justify-center mx-auto mb-4">
            <span class="material-icons text-info text-2xl">schedule</span>
          </div>
          <p class="text-3xl font-bold text-text-primary">12</p>
          <p class="text-sm text-text-muted">Analyses en cours</p>
        </div>
      </div>

      <!-- High Risk Alerts -->
      <div class="card">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-error/20 flex items-center justify-center">
              <span class="material-icons text-error">priority_high</span>
            </div>
            <h2 class="text-xl font-semibold text-text-primary">Alertes Prioritaires</h2>
          </div>
          <span class="badge-error">{{ highRiskPredictions().length }} en attente</span>
        </div>

        <div class="space-y-4">
          <div *ngFor="let prediction of highRiskPredictions()"
               class="p-4 bg-surface-light rounded-xl border-l-4"
               [class.border-error]="prediction.riskCategory === 'HIGH' || prediction.riskCategory === 'CRITICAL'"
               [class.border-warning]="prediction.riskCategory === 'MODERATE'">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <span class="font-semibold text-text-primary">{{ prediction.patientName }}</span>
                  <span class="text-xs px-2 py-1 rounded-full"
                        [class.bg-error/20]="prediction.riskCategory === 'HIGH' || prediction.riskCategory === 'CRITICAL'"
                        [class.text-error]="prediction.riskCategory === 'HIGH' || prediction.riskCategory === 'CRITICAL'"
                        [class.bg-warning/20]="prediction.riskCategory === 'MODERATE'"
                        [class.text-warning]="prediction.riskCategory === 'MODERATE'">
                    {{ prediction.riskCategory }}
                  </span>
                </div>
                <p class="text-sm text-text-secondary mb-2">{{ prediction.prediction }}</p>
                <p class="text-xs text-text-muted">{{ prediction.recommendations }}</p>
              </div>
              <div class="text-right ml-4">
                <p class="text-2xl font-bold"
                   [class.text-error]="prediction.riskLevel >= 70"
                   [class.text-warning]="prediction.riskLevel >= 30 && prediction.riskLevel < 70">
                  {{ prediction.riskLevel }}%
                </p>
                <p class="text-xs text-text-muted">Niveau de risque</p>
              </div>
            </div>
            <div class="flex gap-2 mt-4">
              <button class="btn-primary text-sm py-2 px-4">Prendre action</button>
              <button class="btn-outline text-sm py-2 px-4">Voir détails</button>
            </div>
          </div>

          <div *ngIf="highRiskPredictions().length === 0" 
               class="text-center py-8 text-text-muted">
            <span class="material-icons text-4xl text-success mb-2">check_circle</span>
            <p>Aucune alerte prioritaire</p>
          </div>
        </div>
      </div>

      <!-- Prediction Types -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="card">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-info/20 flex items-center justify-center">
              <span class="material-icons text-info">event_upcoming</span>
            </div>
            <h3 class="font-semibold text-text-primary">Planification Optimale</h3>
          </div>
          <p class="text-sm text-text-secondary mb-4">
            L'IA analyse les patterns de chaque patient pour recommander le meilleur moment pour la prochaine séance.
          </p>
          <button class="w-full btn-outline text-sm">Voir les recommandations</button>
        </div>

        <div class="card">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-warning/20 flex items-center justify-center">
              <span class="material-icons text-warning">person_off</span>
            </div>
            <h3 class="font-semibold text-text-primary">Risque d'Abandon</h3>
          </div>
          <p class="text-sm text-text-secondary mb-4">
            Identification précoce des patients susceptibles d'abandonner leur traitement.
          </p>
          <button class="w-full btn-outline text-sm">Analyser les risques</button>
        </div>

        <div class="card">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-success/20 flex items-center justify-center">
              <span class="material-icons text-success">trending_up</span>
            </div>
            <h3 class="font-semibold text-text-primary">Progression</h3>
          </div>
          <p class="text-sm text-text-secondary mb-4">
            Évaluation continue de la progression des patients avec prédiction des résultats.
          </p>
          <button class="w-full btn-outline text-sm">Voir les progressions</button>
        </div>
      </div>
    </div>
  `
})
export class PredictionsDashboardComponent implements OnInit {
  private apiService = inject(ApiService);

  highRiskPredictions = signal<any[]>([]);

  ngOnInit() {
    this.loadHighRiskPredictions();
  }

  loadHighRiskPredictions() {
    this.apiService.getHighRiskPredictions(50).subscribe({
      next: (data) => this.highRiskPredictions.set(data),
      error: (err) => console.error('Error loading predictions:', err)
    });
  }
}

