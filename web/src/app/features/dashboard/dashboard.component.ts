import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="space-y-6 animate-fade-in">
      <!-- Welcome Section -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-text-primary mb-2">
            Bonjour, {{ authService.currentUser()?.firstName }} 👋
          </h1>
          <p class="text-text-secondary">Voici le résumé de votre activité aujourd'hui</p>
        </div>
        <button class="btn-accent flex items-center gap-2">
          <span class="material-icons">add</span>
          Nouvelle Séance
        </button>
      </div>

      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="card-hover animate-fade-in animate-delay-100">
          <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 rounded-xl bg-primary/20 flex items-center justify-center">
              <span class="material-icons text-primary-light">people</span>
            </div>
            <span class="badge-success">+12%</span>
          </div>
          <p class="text-text-muted text-sm mb-1">Total Patients</p>
          <p class="text-3xl font-bold text-text-primary">{{ stats()?.totalPatients || 0 }}</p>
        </div>

        <div class="card-hover animate-fade-in animate-delay-200">
          <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 rounded-xl bg-accent/20 flex items-center justify-center">
              <span class="material-icons text-accent">event_available</span>
            </div>
            <span class="text-text-muted text-sm">Aujourd'hui</span>
          </div>
          <p class="text-text-muted text-sm mb-1">Séances du jour</p>
          <p class="text-3xl font-bold text-text-primary">{{ stats()?.todaySeances || 0 }}</p>
        </div>

        <div class="card-hover animate-fade-in animate-delay-300">
          <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 rounded-xl bg-success/20 flex items-center justify-center">
              <span class="material-icons text-success">trending_up</span>
            </div>
            <span class="badge-success">{{ stats()?.seanceCompletionRate?.toFixed(0) || 0 }}%</span>
          </div>
          <p class="text-text-muted text-sm mb-1">Taux de complétion</p>
          <p class="text-3xl font-bold text-text-primary">{{ stats()?.completedSeancesThisMonth || 0 }}</p>
        </div>

        <div class="card-hover animate-fade-in animate-delay-400">
          <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 rounded-xl bg-error/20 flex items-center justify-center">
              <span class="material-icons text-error">warning</span>
            </div>
            <span class="badge-warning">Attention</span>
          </div>
          <p class="text-text-muted text-sm mb-1">Patients à risque</p>
          <p class="text-3xl font-bold text-text-primary">{{ stats()?.highRiskPatients || 0 }}</p>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Upcoming Sessions -->
        <div class="lg:col-span-2 card">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-semibold text-text-primary">Prochaines Séances</h2>
            <a routerLink="/seances" class="text-accent hover:text-accent-light text-sm font-medium">
              Voir tout →
            </a>
          </div>
          
          <div class="space-y-4">
            <div *ngFor="let seance of stats()?.upcomingSeancesList; let i = index"
                 class="flex items-center gap-4 p-4 bg-surface-light rounded-xl hover:bg-surface transition-colors">
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-primary to-primary-light 
                          flex items-center justify-center text-white font-bold">
                {{ seance.patientName?.charAt(0) }}
              </div>
              <div class="flex-1">
                <p class="font-semibold text-text-primary">{{ seance.patientName }}</p>
                <p class="text-sm text-text-muted">avec {{ seance.therapeuteName }}</p>
              </div>
              <div class="text-right">
                <p class="font-medium text-text-primary">{{ formatDate(seance.scheduledAt) }}</p>
                <p class="text-sm text-text-muted">{{ formatTime(seance.scheduledAt) }}</p>
              </div>
              <span class="px-3 py-1 rounded-full text-xs font-semibold"
                    [class.bg-info/20]="seance.type === 'VIDEO_CALL'"
                    [class.text-info]="seance.type === 'VIDEO_CALL'"
                    [class.bg-success/20]="seance.type !== 'VIDEO_CALL'"
                    [class.text-success]="seance.type !== 'VIDEO_CALL'">
                {{ seance.type === 'VIDEO_CALL' ? 'Vidéo' : 'Présentiel' }}
              </span>
            </div>

            <div *ngIf="!stats()?.upcomingSeancesList?.length" 
                 class="text-center py-8 text-text-muted">
              <span class="material-icons text-4xl mb-2">event_busy</span>
              <p>Aucune séance prévue</p>
            </div>
          </div>
        </div>

        <!-- Quick Stats & Predictions -->
        <div class="space-y-6">
          <!-- Predictions Card -->
          <div class="card">
            <h2 class="text-xl font-semibold text-text-primary mb-4">Alertes Prédictions</h2>
            <div class="space-y-3">
              <div *ngFor="let prediction of stats()?.recentPredictions?.slice(0, 3)"
                   class="p-3 bg-surface-light rounded-xl">
                <div class="flex items-center gap-3 mb-2">
                  <span class="material-icons text-sm"
                        [class.text-success]="prediction.riskCategory === 'LOW'"
                        [class.text-warning]="prediction.riskCategory === 'MODERATE'"
                        [class.text-error]="prediction.riskCategory === 'HIGH'">
                    {{ prediction.riskCategory === 'LOW' ? 'check_circle' : 
                       prediction.riskCategory === 'MODERATE' ? 'warning' : 'error' }}
                  </span>
                  <span class="text-sm font-medium text-text-primary">{{ prediction.patientName }}</span>
                </div>
                <p class="text-xs text-text-muted line-clamp-2">{{ prediction.prediction }}</p>
              </div>

              <a routerLink="/predictions" 
                 class="block text-center text-accent hover:text-accent-light text-sm font-medium pt-2">
                Voir toutes les prédictions →
              </a>
            </div>
          </div>

          <!-- Therapeutes Status -->
          <div class="card">
            <h2 class="text-xl font-semibold text-text-primary mb-4">Équipe Médicale</h2>
            <div class="flex items-center justify-between mb-4">
              <div>
                <p class="text-3xl font-bold gradient-text">{{ stats()?.totalTherapeutes || 0 }}</p>
                <p class="text-sm text-text-muted">Thérapeutes</p>
              </div>
              <div class="w-16 h-16 rounded-full border-4 border-success flex items-center justify-center">
                <span class="text-success font-bold">
                  {{ stats()?.availableTherapeutes || 0 }}
                </span>
              </div>
            </div>
            <p class="text-sm text-text-muted">
              <span class="text-success font-medium">{{ stats()?.availableTherapeutes }}</span> disponibles
            </p>
          </div>
        </div>
      </div>

      <!-- Recent Patients -->
      <div class="card">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-semibold text-text-primary">Patients Récents</h2>
          <a routerLink="/patients" class="text-accent hover:text-accent-light text-sm font-medium">
            Voir tout →
          </a>
        </div>
        
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-surface-light">
                <th class="text-left py-3 px-4 text-text-muted font-medium text-sm">Patient</th>
                <th class="text-left py-3 px-4 text-text-muted font-medium text-sm">Email</th>
                <th class="text-left py-3 px-4 text-text-muted font-medium text-sm">Thérapeute</th>
                <th class="text-left py-3 px-4 text-text-muted font-medium text-sm">Statut</th>
                <th class="text-left py-3 px-4 text-text-muted font-medium text-sm">Risque</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let patient of stats()?.recentPatients"
                  class="border-b border-surface-light/50 hover:bg-surface-light/30 transition-colors">
                <td class="py-4 px-4">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-gradient-to-br from-accent to-accent-light 
                                flex items-center justify-center text-background font-bold text-sm">
                      {{ patient.firstName?.charAt(0) }}{{ patient.lastName?.charAt(0) }}
                    </div>
                    <div>
                      <p class="font-medium text-text-primary">{{ patient.fullName }}</p>
                      <p class="text-xs text-text-muted">{{ patient.patientCode }}</p>
                    </div>
                  </div>
                </td>
                <td class="py-4 px-4 text-text-secondary">{{ patient.email }}</td>
                <td class="py-4 px-4 text-text-secondary">{{ patient.assignedTherapeuteName || 'Non assigné' }}</td>
                <td class="py-4 px-4">
                  <span class="badge-success" *ngIf="patient.status === 'ACTIVE'">Actif</span>
                  <span class="badge-warning" *ngIf="patient.status === 'ON_HOLD'">En pause</span>
                  <span class="badge-error" *ngIf="patient.status === 'INACTIVE'">Inactif</span>
                </td>
                <td class="py-4 px-4">
                  <div class="flex items-center gap-2">
                    <div class="w-16 h-2 bg-surface-light rounded-full overflow-hidden">
                      <div class="h-full rounded-full"
                           [style.width.%]="patient.riskScore || 0"
                           [class.bg-success]="(patient.riskScore || 0) < 30"
                           [class.bg-warning]="(patient.riskScore || 0) >= 30 && (patient.riskScore || 0) < 70"
                           [class.bg-error]="(patient.riskScore || 0) >= 70">
                      </div>
                    </div>
                    <span class="text-sm text-text-muted">{{ patient.riskScore || 0 }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  private apiService = inject(ApiService);
  authService = inject(AuthService);

  stats = signal<any>(null);

  ngOnInit() {
    this.loadDashboardStats();
  }

  loadDashboardStats() {
    this.apiService.getDashboardStats().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error('Error loading dashboard:', err)
    });
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'short'
    });
  }

  formatTime(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}

