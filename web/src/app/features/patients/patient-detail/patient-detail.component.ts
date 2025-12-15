import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="space-y-6 animate-fade-in" *ngIf="patient()">
      <!-- Header -->
      <div class="flex items-center gap-4 mb-6">
        <a routerLink="/patients" class="p-2 rounded-lg hover:bg-surface-light transition-colors">
          <span class="material-icons text-text-secondary">arrow_back</span>
        </a>
        <div class="flex-1">
          <h1 class="text-3xl font-bold text-text-primary">{{ patient().fullName }}</h1>
          <p class="text-text-secondary">{{ patient().patientCode }}</p>
        </div>
        <button class="btn-outline flex items-center gap-2">
          <span class="material-icons">edit</span>
          Modifier
        </button>
      </div>

      <!-- Main Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Left Column -->
        <div class="lg:col-span-2 space-y-6">
          <!-- Patient Info Card -->
          <div class="card">
            <div class="flex items-start gap-6 mb-6">
              <div class="w-24 h-24 rounded-2xl bg-gradient-to-br from-accent to-accent-light 
                          flex items-center justify-center text-background font-bold text-3xl">
                {{ patient().firstName?.charAt(0) }}{{ patient().lastName?.charAt(0) }}
              </div>
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <h2 class="text-2xl font-bold text-text-primary">{{ patient().fullName }}</h2>
                  <span class="badge-success" *ngIf="patient().status === 'ACTIVE'">Actif</span>
                </div>
                <div class="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span class="text-text-muted">Email:</span>
                    <span class="text-text-primary ml-2">{{ patient().email }}</span>
                  </div>
                  <div>
                    <span class="text-text-muted">Téléphone:</span>
                    <span class="text-text-primary ml-2">{{ patient().phoneNumber || 'N/A' }}</span>
                  </div>
                  <div>
                    <span class="text-text-muted">Date de naissance:</span>
                    <span class="text-text-primary ml-2">{{ patient().dateOfBirth || 'N/A' }}</span>
                  </div>
                  <div>
                    <span class="text-text-muted">Âge:</span>
                    <span class="text-text-primary ml-2">{{ patient().age }} ans</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Address -->
            <div class="p-4 bg-surface-light rounded-xl">
              <div class="flex items-center gap-2 text-text-muted mb-2">
                <span class="material-icons text-sm">location_on</span>
                <span class="text-sm font-medium">Adresse</span>
              </div>
              <p class="text-text-primary">
                {{ patient().address || 'Non renseignée' }}
                {{ patient().city ? ', ' + patient().city : '' }}
                {{ patient().postalCode ? ' ' + patient().postalCode : '' }}
              </p>
            </div>
          </div>

          <!-- Medical History -->
          <div class="card">
            <h3 class="text-lg font-semibold text-text-primary mb-4">Historique Médical</h3>
            <p class="text-text-secondary leading-relaxed">
              {{ patient().medicalHistory || 'Aucun historique médical enregistré.' }}
            </p>
            
            <div class="grid grid-cols-2 gap-4 mt-6">
              <div class="p-4 bg-surface-light rounded-xl">
                <span class="text-text-muted text-sm">Médicaments actuels</span>
                <p class="text-text-primary mt-1">{{ patient().currentMedications || 'Aucun' }}</p>
              </div>
              <div class="p-4 bg-surface-light rounded-xl">
                <span class="text-text-muted text-sm">Allergies</span>
                <p class="text-text-primary mt-1">{{ patient().allergies || 'Aucune' }}</p>
              </div>
            </div>
          </div>

          <!-- Sessions History -->
          <div class="card">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-text-primary">Historique des Séances</h3>
              <button class="btn-primary text-sm">Nouvelle séance</button>
            </div>
            
            <div class="text-center py-8 text-text-muted">
              <span class="material-icons text-4xl mb-2">event</span>
              <p>{{ patient().totalSeances || 0 }} séances au total</p>
            </div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="space-y-6">
          <!-- Risk Assessment -->
          <div class="card">
            <h3 class="text-lg font-semibold text-text-primary mb-4">Évaluation du Risque</h3>
            <div class="flex items-center justify-center mb-6">
              <div class="relative w-32 h-32">
                <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                  <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" 
                          class="text-surface-light" stroke-width="8"/>
                  <circle cx="50" cy="50" r="45" fill="none" 
                          [attr.stroke]="getRiskColor(patient().riskScore)"
                          stroke-width="8" stroke-linecap="round"
                          [attr.stroke-dasharray]="getStrokeDasharray(patient().riskScore)"
                          stroke-dashoffset="0"/>
                </svg>
                <div class="absolute inset-0 flex items-center justify-center">
                  <div class="text-center">
                    <span class="text-3xl font-bold text-text-primary">{{ patient().riskScore || 0 }}</span>
                    <span class="text-text-muted text-sm block">/ 100</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="text-center">
              <span class="px-4 py-2 rounded-full text-sm font-semibold"
                    [class.bg-success/20]="(patient().riskScore || 0) < 30"
                    [class.text-success]="(patient().riskScore || 0) < 30"
                    [class.bg-warning/20]="(patient().riskScore || 0) >= 30 && (patient().riskScore || 0) < 70"
                    [class.text-warning]="(patient().riskScore || 0) >= 30 && (patient().riskScore || 0) < 70"
                    [class.bg-error/20]="(patient().riskScore || 0) >= 70"
                    [class.text-error]="(patient().riskScore || 0) >= 70">
                {{ patient().riskCategory || 'Non évalué' }}
              </span>
            </div>
          </div>

          <!-- Assigned Therapist -->
          <div class="card">
            <h3 class="text-lg font-semibold text-text-primary mb-4">Thérapeute Assigné</h3>
            <div *ngIf="patient().assignedTherapeuteName" class="flex items-center gap-4">
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-primary to-primary-light 
                          flex items-center justify-center text-white font-bold">
                {{ patient().assignedTherapeuteName?.charAt(0) }}
              </div>
              <div>
                <p class="font-semibold text-text-primary">{{ patient().assignedTherapeuteName }}</p>
                <p class="text-sm text-text-muted">Psychologue</p>
              </div>
            </div>
            <div *ngIf="!patient().assignedTherapeuteName" class="text-center py-4">
              <span class="text-text-muted">Aucun thérapeute assigné</span>
              <button class="btn-outline w-full mt-4">Assigner un thérapeute</button>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="card">
            <h3 class="text-lg font-semibold text-text-primary mb-4">Actions Rapides</h3>
            <div class="space-y-2">
              <button class="w-full p-3 bg-surface-light rounded-xl text-left hover:bg-surface transition-colors flex items-center gap-3">
                <span class="material-icons text-primary">event</span>
                <span class="text-text-primary">Planifier une séance</span>
              </button>
              <button class="w-full p-3 bg-surface-light rounded-xl text-left hover:bg-surface transition-colors flex items-center gap-3">
                <span class="material-icons text-accent">insights</span>
                <span class="text-text-primary">Générer prédiction</span>
              </button>
              <button class="w-full p-3 bg-surface-light rounded-xl text-left hover:bg-surface transition-colors flex items-center gap-3">
                <span class="material-icons text-info">description</span>
                <span class="text-text-primary">Voir les documents</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class PatientDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);

  patient = signal<any>(null);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPatient(Number(id));
    }
  }

  loadPatient(id: number) {
    this.apiService.getPatientById(id).subscribe({
      next: (data) => this.patient.set(data),
      error: (err) => console.error('Error loading patient:', err)
    });
  }

  getRiskColor(score: number): string {
    if (score < 30) return '#10B981'; // success
    if (score < 70) return '#F59E0B'; // warning
    return '#EF4444'; // error
  }

  getStrokeDasharray(score: number): string {
    const circumference = 2 * Math.PI * 45;
    const progress = (score / 100) * circumference;
    return `${progress} ${circumference}`;
  }
}

