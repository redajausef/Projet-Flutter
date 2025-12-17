import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PatientService } from '../../../core/services/patient.service';
import { SeanceService } from '../../../core/services/seance.service';
import { Patient, Seance } from '../../../core/models';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <!-- Loading State -->
    @if (loading()) {
      <div class="d-flex justify-content-center align-items-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Chargement...</span>
        </div>
      </div>
    }

    @if (patient(); as p) {
      <!-- Page Header -->
      <div class="page-header mb-4">
        <div class="d-flex align-items-center justify-content-between">
          <div class="d-flex align-items-center gap-3">
            <a routerLink="/patients" class="btn btn-outline-secondary btn-sm">
              <i class="ti ti-arrow-left me-1"></i>Retour
            </a>
            <div>
              <h4 class="mb-1">{{ p.fullName || (p.firstName + ' ' + p.lastName) }}</h4>
              <p class="text-muted mb-0">{{ p.patientCode }}</p>
            </div>
          </div>
          <div class="d-flex gap-2">
            <button class="btn btn-outline-primary" (click)="openEditModal()">
              <i class="ti ti-edit me-1"></i>Modifier
            </button>
            <button class="btn btn-primary" (click)="openNewSeanceModal()">
              <i class="ti ti-calendar-plus me-1"></i>Nouvelle séance
            </button>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <!-- Left Column -->
        <div class="col-lg-8">
          <!-- Patient Info Card -->
          <div class="card mb-4">
            <div class="card-body">
              <div class="d-flex align-items-start gap-4">
                <div class="avatar avatar-xl" [class]="getAvatarClass()">
                  {{ getInitials() }}
                </div>
                <div class="flex-grow-1">
                  <div class="d-flex align-items-center gap-2 mb-2">
                    <h5 class="mb-0">{{ p.fullName || (p.firstName + ' ' + p.lastName) }}</h5>
                    <span class="badge" [class]="getStatusClass()">
                      {{ getStatusLabel() }}
                    </span>
                  </div>
                  
                  <div class="row g-3">
                    <div class="col-md-6">
                      <div class="info-item">
                        <i class="ti ti-mail text-muted"></i>
                        <span>{{ p.email }}</span>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="info-item">
                        <i class="ti ti-phone text-muted"></i>
                        <span>{{ p.phoneNumber || 'Non renseigné' }}</span>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="info-item">
                        <i class="ti ti-calendar text-muted"></i>
                        <span>{{ p.dateOfBirth || 'Non renseigné' }} ({{ getAge() }} ans)</span>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="info-item">
                        <i class="ti ti-gender-bigender text-muted"></i>
                        <span>{{ getGenderLabel() }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Address Section -->
              <div class="mt-4 p-3 bg-light rounded-3">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <i class="ti ti-map-pin text-muted"></i>
                  <small class="text-muted fw-medium">Adresse</small>
                </div>
                <p class="mb-0">
                  {{ p.address || 'Non renseignée' }}
                  {{ p.city ? ', ' + p.city : '' }}
                  {{ p.postalCode ? ' ' + p.postalCode : '' }}
                </p>
              </div>
            </div>
          </div>

          <!-- Medical History Card -->
          <div class="card mb-4">
            <div class="card-header bg-transparent py-3">
              <h6 class="mb-0">
                <i class="ti ti-report-medical me-2"></i>Historique Médical
              </h6>
            </div>
            <div class="card-body">
              <p class="text-muted">
                {{ p.medicalHistory || 'Aucun historique médical enregistré.' }}
              </p>
              
              <div class="row g-3 mt-2">
                <div class="col-md-6">
                  <div class="p-3 bg-light rounded-3">
                    <small class="text-muted d-block mb-1">Médicaments actuels</small>
                    <span>{{ p.currentMedications || 'Aucun' }}</span>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="p-3 bg-light rounded-3">
                    <small class="text-muted d-block mb-1">Allergies</small>
                    <span>{{ p.allergies || 'Aucune connue' }}</span>
                  </div>
                </div>
              </div>

              @if (p.notes) {
                <div class="mt-3 p-3 bg-warning-subtle rounded-3">
                  <small class="text-muted d-block mb-1">
                    <i class="ti ti-note me-1"></i>Notes
                  </small>
                  <span>{{ p.notes }}</span>
                </div>
              }
            </div>
          </div>

          <!-- Sessions History Card -->
          <div class="card">
            <div class="card-header bg-transparent py-3 d-flex justify-content-between align-items-center">
              <h6 class="mb-0">
                <i class="ti ti-calendar me-2"></i>Historique des Séances
              </h6>
              <span class="badge bg-primary-subtle text-primary">
                {{ seances().length }} séances
              </span>
            </div>
            <div class="card-body p-0">
              @if (seances().length === 0) {
                <div class="text-center py-5">
                  <i class="ti ti-calendar-off f-48 text-muted opacity-50 d-block mb-3"></i>
                  <p class="text-muted mb-0">Aucune séance enregistrée</p>
                </div>
              } @else {
                <div class="table-responsive">
                  <table class="table table-hover mb-0">
                    <thead class="bg-light">
                      <tr>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Statut</th>
                        <th>Durée</th>
                        <th class="text-end">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (seance of seances(); track seance.id) {
                        <tr>
                          <td>{{ formatDate(seance.scheduledAt) }}</td>
                          <td>
                            <span class="badge bg-secondary-subtle text-secondary">
                              {{ getSeanceTypeLabel(seance.type) }}
                            </span>
                          </td>
                          <td>
                            <span class="badge" [class]="getSeanceStatusClass(seance.status)">
                              {{ getSeanceStatusLabel(seance.status) }}
                            </span>
                          </td>
                          <td>{{ seance.durationMinutes }} min</td>
                          <td class="text-end">
                            <button class="btn btn-sm btn-outline-primary" title="Voir détails">
                              <i class="ti ti-eye"></i>
                            </button>
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="col-lg-4">
          <!-- Risk Assessment Card -->
          <div class="card mb-4">
            <div class="card-header bg-transparent py-3">
              <h6 class="mb-0">
                <i class="ti ti-chart-pie me-2"></i>Évaluation du Risque
              </h6>
            </div>
            <div class="card-body text-center">
              <div class="risk-chart mx-auto mb-3">
                <svg class="risk-svg" viewBox="0 0 100 100">
                  <circle cx="50" cy="50" r="45" fill="none" stroke="#f0f0f0" stroke-width="8"/>
                  <circle cx="50" cy="50" r="45" fill="none" [attr.stroke]="getRiskColor()" 
                          stroke-width="8" stroke-linecap="round" 
                          [style.strokeDasharray]="getRiskDasharray()"
                          transform="rotate(-90 50 50)"/>
                </svg>
                <div class="risk-value">
                  <span class="risk-score">{{ p.riskScore || 0 }}</span>
                  <span class="risk-max">/ 100</span>
                </div>
              </div>
              
              <span class="badge rounded-pill px-3 py-2" [class]="getRiskBadgeClass()">
                {{ getRiskLabel() }}
              </span>
            </div>
          </div>

          <!-- Assigned Therapist Card -->
          <div class="card mb-4">
            <div class="card-header bg-transparent py-3">
              <h6 class="mb-0">
                <i class="ti ti-stethoscope me-2"></i>Thérapeute Assigné
              </h6>
            </div>
            <div class="card-body">
              @if (p.assignedTherapeuteName) {
                <div class="d-flex align-items-center gap-3">
                  <div class="avatar bg-primary rounded-3">
                    {{ p.assignedTherapeuteName?.charAt(0) || 'T' }}
                  </div>
                  <div>
                    <h6 class="mb-0">{{ p.assignedTherapeuteName }}</h6>
                    <small class="text-muted">Thérapeute</small>
                  </div>
                </div>
              } @else {
                <div class="text-center py-3">
                  <i class="ti ti-user-off f-32 text-muted opacity-50 d-block mb-2"></i>
                  <p class="text-muted mb-2">Aucun thérapeute assigné</p>
                  <button class="btn btn-outline-primary btn-sm">
                    <i class="ti ti-user-plus me-1"></i>Assigner
                  </button>
                </div>
              }
            </div>
          </div>

          <!-- Quick Stats -->
          <div class="card mb-4">
            <div class="card-header bg-transparent py-3">
              <h6 class="mb-0">
                <i class="ti ti-chart-bar me-2"></i>Statistiques
              </h6>
            </div>
            <div class="card-body">
              <div class="stat-row">
                <span class="text-muted">Total séances</span>
                <span class="fw-bold">{{ p.totalSeances || 0 }}</span>
              </div>
              <div class="stat-row">
                <span class="text-muted">Complétées</span>
                <span class="fw-bold text-success">{{ p.completedSeances || 0 }}</span>
              </div>
              <div class="stat-row">
                <span class="text-muted">Taux complétion</span>
                <span class="fw-bold text-primary">{{ getCompletionRate() }}%</span>
              </div>
              <div class="stat-row">
                <span class="text-muted">Inscrit depuis</span>
                <span class="fw-bold">{{ getCreatedAgo() }}</span>
              </div>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="card">
            <div class="card-header bg-transparent py-3">
              <h6 class="mb-0">
                <i class="ti ti-bolt me-2"></i>Actions Rapides
              </h6>
            </div>
            <div class="card-body p-2">
              <button class="quick-action" (click)="openNewSeanceModal()">
                <i class="ti ti-calendar-plus text-primary"></i>
                <span>Planifier une séance</span>
              </button>
              <button class="quick-action">
                <i class="ti ti-chart-dots text-warning"></i>
                <span>Générer prédiction IA</span>
              </button>
              <button class="quick-action">
                <i class="ti ti-file-text text-info"></i>
                <span>Voir les documents</span>
              </button>
              <button class="quick-action text-danger" (click)="confirmDelete()">
                <i class="ti ti-trash text-danger"></i>
                <span>Supprimer le patient</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    }

    @if (!patient() && !loading()) {
      <div class="card">
        <div class="card-body text-center py-5">
          <i class="ti ti-user-off f-48 text-muted opacity-50 d-block mb-3"></i>
          <h5 class="text-muted">Patient non trouvé</h5>
          <p class="text-muted mb-3">Le patient demandé n'existe pas ou a été supprimé.</p>
          <a routerLink="/patients" class="btn btn-primary">
            <i class="ti ti-arrow-left me-1"></i>Retour à la liste
          </a>
        </div>
      </div>
    }

    <!-- Edit Modal -->
    @if (showEditModal()) {
      <div class="modal fade show d-block" style="background: rgba(0,0,0,0.5)" (click)="closeEditModal()">
        <div class="modal-dialog modal-lg" (click)="$event.stopPropagation()">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">
                <i class="ti ti-edit me-2"></i>Modifier le patient
              </h5>
              <button type="button" class="btn-close" (click)="closeEditModal()"></button>
            </div>
            <div class="modal-body">
              <form>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label">Prénom</label>
                    <input type="text" class="form-control" [(ngModel)]="editForm.firstName" name="firstName">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Nom</label>
                    <input type="text" class="form-control" [(ngModel)]="editForm.lastName" name="lastName">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Email</label>
                    <input type="email" class="form-control" [(ngModel)]="editForm.email" name="email">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Téléphone</label>
                    <input type="tel" class="form-control" [(ngModel)]="editForm.phoneNumber" name="phoneNumber">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Date de naissance</label>
                    <input type="date" class="form-control" [(ngModel)]="editForm.dateOfBirth" name="dateOfBirth">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Genre</label>
                    <select class="form-select" [(ngModel)]="editForm.gender" name="gender">
                      <option value="">Sélectionner</option>
                      <option value="MALE">Homme</option>
                      <option value="FEMALE">Femme</option>
                      <option value="OTHER">Autre</option>
                    </select>
                  </div>
                  <div class="col-12">
                    <label class="form-label">Adresse</label>
                    <input type="text" class="form-control" [(ngModel)]="editForm.address" name="address">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Ville</label>
                    <input type="text" class="form-control" [(ngModel)]="editForm.city" name="city">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Code postal</label>
                    <input type="text" class="form-control" [(ngModel)]="editForm.postalCode" name="postalCode">
                  </div>
                  <div class="col-12">
                    <label class="form-label">Historique médical</label>
                    <textarea class="form-control" rows="2" [(ngModel)]="editForm.medicalHistory" 
                              name="medicalHistory"></textarea>
                  </div>
                  <div class="col-12">
                    <label class="form-label">Notes</label>
                    <textarea class="form-control" rows="2" [(ngModel)]="editForm.notes" name="notes"></textarea>
                  </div>
                </div>
              </form>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="closeEditModal()">Annuler</button>
              <button type="button" class="btn btn-primary" (click)="savePatient()" [disabled]="saving()">
                @if (saving()) {
                  <span class="spinner-border spinner-border-sm me-2"></span>
                }
                <i class="ti ti-check me-1" *ngIf="!saving()"></i>
                Enregistrer
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .avatar {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 16px;
      color: white;
    }
    
    .avatar-xl {
      width: 80px;
      height: 80px;
      font-size: 24px;
    }
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      
      i { font-size: 18px; }
    }
    
    .f-48 { font-size: 48px; }
    .f-32 { font-size: 32px; }
    
    .risk-chart {
      position: relative;
      width: 120px;
      height: 120px;
    }
    
    .risk-svg {
      width: 100%;
      height: 100%;
    }
    
    .risk-value {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      text-align: center;
    }
    
    .risk-score {
      display: block;
      font-size: 28px;
      font-weight: 700;
      line-height: 1;
    }
    
    .risk-max {
      font-size: 12px;
      color: #6b7280;
    }
    
    .stat-row {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child { border-bottom: none; }
    }
    
    .quick-action {
      display: flex;
      align-items: center;
      gap: 12px;
      width: 100%;
      padding: 12px 16px;
      border: none;
      background: none;
      border-radius: 8px;
      text-align: left;
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover { background: #f8f9fa; }
      
      i { font-size: 20px; }
      span { color: #1f2937; }
    }
    
    .table th {
      font-size: 11px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: #6b7280;
      padding: 12px 16px;
      border: none;
    }
    
    .table td {
      padding: 12px 16px;
      vertical-align: middle;
    }
    
    .card {
      border: none;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    }
  `]
})
export class PatientDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private patientService = inject(PatientService);
  private seanceService = inject(SeanceService);

  patient = signal<Patient | null>(null);
  seances = signal<Seance[]>([]);
  loading = signal(true);
  saving = signal(false);
  showEditModal = signal(false);
  
  editForm: Partial<Patient> = {};
  
  private colors = ['bg-primary', 'bg-success', 'bg-info', 'bg-warning', 'bg-danger'];

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPatient(Number(id));
    }
  }

  loadPatient(id: number) {
    this.loading.set(true);
    this.patientService.getPatientById(id).subscribe({
      next: (patient) => {
        this.patient.set(patient);
        this.loadSeances(id);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading patient:', err);
        this.loading.set(false);
      }
    });
  }

  loadSeances(patientId: number) {
    this.seanceService.getSeancesByPatient(patientId).subscribe({
      next: (seances) => this.seances.set(seances),
      error: (err) => console.error('Error loading seances:', err)
    });
  }

  getInitials(): string {
    const p = this.patient();
    return p ? (p.firstName?.charAt(0) || '') + (p.lastName?.charAt(0) || '') : '';
  }

  getAvatarClass(): string {
    const p = this.patient();
    return p ? this.colors[p.id % this.colors.length] : 'bg-primary';
  }

  getAge(): number {
    const p = this.patient();
    if (p?.age) return p.age;
    if (!p?.dateOfBirth) return 0;
    const birthDate = new Date(p.dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) age--;
    return age;
  }

  getGenderLabel(): string {
    const g = this.patient()?.gender;
    switch (g) {
      case 'MALE': return 'Homme';
      case 'FEMALE': return 'Femme';
      case 'OTHER': return 'Autre';
      default: return 'Non renseigné';
    }
  }

  getStatusClass(): string {
    const status = this.patient()?.status;
    switch (status) {
      case 'ACTIVE': return 'bg-success-subtle text-success';
      case 'ON_HOLD': return 'bg-warning-subtle text-warning';
      case 'INACTIVE': return 'bg-secondary-subtle text-secondary';
      case 'DISCHARGED': return 'bg-info-subtle text-info';
      default: return 'bg-secondary-subtle text-secondary';
    }
  }

  getStatusLabel(): string {
    const status = this.patient()?.status;
    switch (status) {
      case 'ACTIVE': return 'Actif';
      case 'ON_HOLD': return 'En pause';
      case 'INACTIVE': return 'Inactif';
      case 'DISCHARGED': return 'Sorti';
      default: return status || 'Inconnu';
    }
  }

  getRiskColor(): string {
    const score = this.patient()?.riskScore || 0;
    if (score < 30) return '#10B981';
    if (score < 70) return '#F59E0B';
    return '#EF4444';
  }

  getRiskDasharray(): string {
    const score = this.patient()?.riskScore || 0;
    const circumference = 2 * Math.PI * 45;
    const progress = (score / 100) * circumference;
    return `${progress} ${circumference}`;
  }

  getRiskBadgeClass(): string {
    const score = this.patient()?.riskScore || 0;
    if (score < 30) return 'bg-success-subtle text-success';
    if (score < 70) return 'bg-warning-subtle text-warning';
    return 'bg-danger-subtle text-danger';
  }

  getRiskLabel(): string {
    const p = this.patient();
    if (p?.riskCategory) return p.riskCategory;
    const score = p?.riskScore || 0;
    if (score < 30) return 'Risque Faible';
    if (score < 70) return 'Risque Modéré';
    return 'Risque Élevé';
  }

  getCompletionRate(): number {
    const p = this.patient();
    if (!p?.totalSeances || p.totalSeances === 0) return 0;
    return Math.round((p.completedSeances || 0) / p.totalSeances * 100);
  }

  getCreatedAgo(): string {
    const p = this.patient();
    if (!p?.createdAt) return 'Inconnu';
    const created = new Date(p.createdAt);
    const now = new Date();
    const months = Math.floor((now.getTime() - created.getTime()) / (1000 * 60 * 60 * 24 * 30));
    if (months < 1) return 'Ce mois';
    if (months === 1) return '1 mois';
    if (months < 12) return `${months} mois`;
    const years = Math.floor(months / 12);
    return years === 1 ? '1 an' : `${years} ans`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { 
      day: '2-digit', 
      month: 'short', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSeanceTypeLabel(type: Seance['type']): string {
    const labels: Record<string, string> = {
      'CONSULTATION': 'Consultation',
      'THERAPY': 'Thérapie',
      'FOLLOW_UP': 'Suivi',
      'VIDEO': 'Vidéo',
      'EMERGENCY': 'Urgence'
    };
    return labels[type] || type;
  }

  getSeanceStatusClass(status: Seance['status']): string {
    const classes: Record<string, string> = {
      'SCHEDULED': 'bg-secondary-subtle text-secondary',
      'CONFIRMED': 'bg-info-subtle text-info',
      'IN_PROGRESS': 'bg-warning-subtle text-warning',
      'COMPLETED': 'bg-success-subtle text-success',
      'CANCELLED': 'bg-danger-subtle text-danger',
      'NO_SHOW': 'bg-danger-subtle text-danger'
    };
    return classes[status] || 'bg-secondary-subtle text-secondary';
  }

  getSeanceStatusLabel(status: Seance['status']): string {
    const labels: Record<string, string> = {
      'SCHEDULED': 'Planifiée',
      'CONFIRMED': 'Confirmée',
      'IN_PROGRESS': 'En cours',
      'COMPLETED': 'Terminée',
      'CANCELLED': 'Annulée',
      'NO_SHOW': 'Absent'
    };
    return labels[status] || status;
  }

  openEditModal(): void {
    const p = this.patient();
    if (p) {
      this.editForm = {
        firstName: p.firstName,
        lastName: p.lastName,
        email: p.email,
        phoneNumber: p.phoneNumber,
        dateOfBirth: p.dateOfBirth,
        gender: p.gender,
        address: p.address,
        city: p.city,
        postalCode: p.postalCode,
        medicalHistory: p.medicalHistory,
        notes: p.notes
      };
      this.showEditModal.set(true);
    }
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
  }

  savePatient(): void {
    const p = this.patient();
    if (!p) return;
    
    this.saving.set(true);
    this.patientService.updatePatient(p.id, this.editForm).subscribe({
      next: (updated) => {
        this.patient.set(updated);
        this.saving.set(false);
        this.closeEditModal();
      },
      error: (err) => {
        console.error('Error updating patient:', err);
        this.saving.set(false);
        // For demo purposes, update locally
        this.patient.set({ ...p, ...this.editForm } as Patient);
        this.closeEditModal();
      }
    });
  }

  openNewSeanceModal(): void {
    // Navigate to seances page with patient context
    this.router.navigate(['/seances'], { 
      queryParams: { patientId: this.patient()?.id } 
    });
  }

  confirmDelete(): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce patient ? Cette action est irréversible.')) {
      // Handle delete
      console.log('Delete patient:', this.patient()?.id);
      this.router.navigate(['/patients']);
    }
  }
}
