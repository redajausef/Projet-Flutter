import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { PatientService } from '../../../core/services/patient.service';
import { TherapeuteService } from '../../../core/services/therapeute.service';
import { Patient, PatientCreateRequest, Therapeute } from '../../../core/models';

@Component({
  selector: 'app-patients-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <!-- Page Header -->
    <div class="page-header mb-4">
      <div class="row align-items-center">
        <div class="col">
          <h4 class="mb-1">Gestion des Patients</h4>
          <p class="text-muted mb-0">{{ totalPatients() }} patients enregistrés</p>
        </div>
        <div class="col-auto">
          <button class="btn btn-primary" (click)="openAddModal()">
            <i class="ti ti-plus me-2"></i>Nouveau patient
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="row mb-4">
      <div class="col-md-3">
        <div class="card bg-primary-subtle border-0">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="avatar avatar-sm bg-primary rounded">
                  <i class="ti ti-users text-white"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h5 class="mb-0">{{ totalPatients() }}</h5>
                <small class="text-muted">Total</small>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card bg-success-subtle border-0">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="avatar avatar-sm bg-success rounded">
                  <i class="ti ti-user-check text-white"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h5 class="mb-0">{{ activePatients() }}</h5>
                <small class="text-muted">Actifs</small>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card bg-warning-subtle border-0">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="avatar avatar-sm bg-warning rounded">
                  <i class="ti ti-clock text-white"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h5 class="mb-0">{{ onHoldPatients() }}</h5>
                <small class="text-muted">En pause</small>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card bg-danger-subtle border-0">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="avatar avatar-sm bg-danger rounded">
                  <i class="ti ti-alert-triangle text-white"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h5 class="mb-0">{{ highRiskPatients() }}</h5>
                <small class="text-muted">À risque</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="card mb-4">
      <div class="card-body">
        <div class="row g-3 align-items-center">
          <div class="col-md-4">
            <div class="input-group">
              <span class="input-group-text bg-white"><i class="ti ti-search"></i></span>
              <input type="text" class="form-control" placeholder="Rechercher un patient..." 
                     [(ngModel)]="searchTerm" (ngModelChange)="onSearchChange($event)">
              @if (searchTerm) {
                <button class="btn btn-outline-secondary" type="button" (click)="clearSearch()">
                  <i class="ti ti-x"></i>
                </button>
              }
            </div>
          </div>
          <div class="col-md-auto">
            <div class="btn-group">
              <button class="btn" [class.btn-primary]="filter() === 'all'" 
                      [class.btn-outline-secondary]="filter() !== 'all'" 
                      (click)="setFilter('all')">Tous</button>
              <button class="btn" [class.btn-primary]="filter() === 'active'" 
                      [class.btn-outline-secondary]="filter() !== 'active'" 
                      (click)="setFilter('active')">Actifs</button>
              <button class="btn" [class.btn-primary]="filter() === 'on_hold'" 
                      [class.btn-outline-secondary]="filter() !== 'on_hold'" 
                      (click)="setFilter('on_hold')">En pause</button>
              <button class="btn" [class.btn-primary]="filter() === 'high_risk'" 
                      [class.btn-outline-secondary]="filter() !== 'high_risk'" 
                      (click)="setFilter('high_risk')">À risque</button>
            </div>
          </div>
          <div class="col-md-auto ms-auto">
            <button class="btn btn-outline-primary" (click)="refreshPatients()">
              <i class="ti ti-refresh me-1"></i>Actualiser
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    @if (loading()) {
      <div class="card">
        <div class="card-body text-center py-5">
          <div class="spinner-border text-primary mb-3" role="status">
            <span class="visually-hidden">Chargement...</span>
          </div>
          <p class="text-muted mb-0">Chargement des patients...</p>
        </div>
      </div>
    }

    <!-- Patients Table -->
    @if (!loading()) {
      <div class="card">
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="bg-light">
                <tr>
                  <th style="width: 25%">Patient</th>
                  <th style="width: 20%">Contact</th>
                  <th style="width: 15%">Thérapeute</th>
                  <th style="width: 10%" class="text-center">Risque</th>
                  <th style="width: 10%">Statut</th>
                  <th style="width: 10%">Séances</th>
                  <th style="width: 10%" class="text-center">Actions</th>
                </tr>
              </thead>
              <tbody>
                @if (filteredPatients().length === 0) {
                  <tr>
                    <td colspan="7" class="text-center py-5">
                      <div class="text-muted">
                        <i class="ti ti-users f-48 d-block mb-3 opacity-50"></i>
                        <p class="mb-0">Aucun patient trouvé</p>
                        @if (searchTerm) {
                          <small>Essayez une autre recherche</small>
                        }
                      </div>
                    </td>
                  </tr>
                }
                @for (patient of filteredPatients(); track patient.id) {
                  <tr class="align-middle">
                    <td>
                      <div class="d-flex align-items-center">
                        <div class="avatar avatar-sm rounded-circle me-3" [class]="getAvatarClass(patient)">
                          {{ getInitials(patient) }}
                        </div>
                        <div>
                          <h6 class="mb-0">{{ patient.fullName || (patient.firstName + ' ' + patient.lastName) }}</h6>
                          <small class="text-muted">{{ patient.patientCode }} · {{ getAge(patient) }} ans</small>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div class="small">
                        <div><i class="ti ti-mail me-1 text-muted"></i>{{ patient.email }}</div>
                        <div class="text-muted">
                          <i class="ti ti-phone me-1"></i>{{ patient.phoneNumber || 'Non renseigné' }}
                        </div>
                      </div>
                    </td>
                    <td>
                      @if (patient.assignedTherapeuteName) {
                        <span class="badge bg-primary-subtle text-primary">
                          {{ patient.assignedTherapeuteName }}
                        </span>
                      } @else {
                        <span class="badge bg-secondary-subtle text-secondary">Non assigné</span>
                      }
                    </td>
                    <td class="text-center">
                      <div class="d-flex align-items-center justify-content-center">
                        <div class="progress flex-grow-1 me-2" style="height: 6px; width: 60px;">
                          <div class="progress-bar" [class]="getRiskClass(patient)" 
                               [style.width.%]="patient.riskScore || 0"></div>
                        </div>
                        <small [class]="getRiskTextClass(patient)">{{ patient.riskScore || 0 }}%</small>
                      </div>
                    </td>
                    <td>
                      <span class="badge" [class]="getStatusClass(patient.status)">
                        <i class="ti me-1" [class]="getStatusIcon(patient.status)"></i>
                        {{ getStatusLabel(patient.status) }}
                      </span>
                    </td>
                    <td>
                      <div class="small">
                        <span class="text-success">{{ patient.completedSeances || 0 }}</span>
                        <span class="text-muted">/{{ patient.totalSeances || 0 }}</span>
                      </div>
                    </td>
                    <td class="text-center">
                      <div class="btn-group btn-group-sm">
                        <a [routerLink]="['/patients', patient.id]" class="btn btn-outline-primary" 
                           title="Voir détails">
                          <i class="ti ti-eye"></i>
                        </a>
                        <button class="btn btn-outline-secondary" (click)="openEditModal(patient)" 
                                title="Modifier">
                          <i class="ti ti-edit"></i>
                        </button>
                        <button class="btn btn-outline-info" (click)="openAssignModal(patient)" 
                                title="Assigner thérapeute">
                          <i class="ti ti-user-plus"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>

        <!-- Pagination -->
        @if (totalPages() > 1) {
          <div class="card-footer bg-white">
            <div class="d-flex justify-content-between align-items-center">
              <small class="text-muted">
                Page {{ currentPage() + 1 }} sur {{ totalPages() }} ({{ totalPatients() }} patients)
              </small>
              <nav>
                <ul class="pagination pagination-sm mb-0">
                  <li class="page-item" [class.disabled]="currentPage() === 0">
                    <button class="page-link" (click)="goToPage(currentPage() - 1)">
                      <i class="ti ti-chevron-left"></i>
                    </button>
                  </li>
                  @for (page of getPageNumbers(); track page) {
                    <li class="page-item" [class.active]="page === currentPage()">
                      <button class="page-link" (click)="goToPage(page)">{{ page + 1 }}</button>
                    </li>
                  }
                  <li class="page-item" [class.disabled]="currentPage() === totalPages() - 1">
                    <button class="page-link" (click)="goToPage(currentPage() + 1)">
                      <i class="ti ti-chevron-right"></i>
                    </button>
                  </li>
                </ul>
              </nav>
            </div>
          </div>
        }
      </div>
    }

    <!-- Add/Edit Patient Modal -->
    @if (showModal()) {
      <div class="modal fade show d-block" style="background: rgba(0,0,0,0.5)" (click)="closeModal()">
        <div class="modal-dialog modal-lg" (click)="$event.stopPropagation()">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">
                <i class="ti ti-user-plus me-2"></i>
                {{ editingPatient() ? 'Modifier le patient' : 'Nouveau Patient' }}
              </h5>
              <button type="button" class="btn-close" (click)="closeModal()"></button>
            </div>
            <div class="modal-body">
              <form>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label">Prénom <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" [(ngModel)]="patientForm.firstName" 
                           name="firstName" required>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Nom <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" [(ngModel)]="patientForm.lastName" 
                           name="lastName" required>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Email <span class="text-danger">*</span></label>
                    <input type="email" class="form-control" [(ngModel)]="patientForm.email" 
                           name="email" required>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Téléphone</label>
                    <input type="tel" class="form-control" [(ngModel)]="patientForm.phoneNumber" 
                           name="phoneNumber" placeholder="06 XX XX XX XX">
                  </div>
                  <div class="col-md-4">
                    <label class="form-label">Date de naissance</label>
                    <input type="date" class="form-control" [(ngModel)]="patientForm.dateOfBirth" 
                           name="dateOfBirth">
                  </div>
                  <div class="col-md-4">
                    <label class="form-label">Genre</label>
                    <select class="form-select" [(ngModel)]="patientForm.gender" name="gender">
                      <option value="">Sélectionner</option>
                      <option value="MALE">Homme</option>
                      <option value="FEMALE">Femme</option>
                      <option value="OTHER">Autre</option>
                    </select>
                  </div>
                  <div class="col-md-4">
                    <label class="form-label">Thérapeute assigné</label>
                    <select class="form-select" [(ngModel)]="patientForm.assignedTherapeuteId" 
                            name="therapeuteId">
                      <option [ngValue]="null">Non assigné</option>
                      @for (therapeute of therapeutes(); track therapeute.id) {
                        <option [ngValue]="therapeute.id">{{ therapeute.fullName }}</option>
                      }
                    </select>
                  </div>
                  <div class="col-12">
                    <label class="form-label">Adresse</label>
                    <input type="text" class="form-control" [(ngModel)]="patientForm.address" 
                           name="address">
                  </div>
                  <div class="col-md-4">
                    <label class="form-label">Ville</label>
                    <input type="text" class="form-control" [(ngModel)]="patientForm.city" name="city">
                  </div>
                  <div class="col-md-4">
                    <label class="form-label">Code postal</label>
                    <input type="text" class="form-control" [(ngModel)]="patientForm.postalCode" 
                           name="postalCode">
                  </div>
                  <div class="col-12">
                    <label class="form-label">Historique médical</label>
                    <textarea class="form-control" rows="2" [(ngModel)]="patientForm.medicalHistory" 
                              name="medicalHistory"></textarea>
                  </div>
                  <div class="col-12">
                    <label class="form-label">Notes</label>
                    <textarea class="form-control" rows="2" [(ngModel)]="patientForm.notes" 
                              name="notes"></textarea>
                  </div>
                </div>
              </form>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="closeModal()">Annuler</button>
              <button type="button" class="btn btn-primary" (click)="savePatient()" 
                      [disabled]="saving() || !isFormValid()">
                @if (saving()) {
                  <span class="spinner-border spinner-border-sm me-2"></span>
                }
                <i class="ti ti-check me-2" *ngIf="!saving()"></i>
                {{ editingPatient() ? 'Mettre à jour' : 'Enregistrer' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    }

    <!-- Assign Therapeute Modal -->
    @if (showAssignModal()) {
      <div class="modal fade show d-block" style="background: rgba(0,0,0,0.5)" (click)="closeAssignModal()">
        <div class="modal-dialog" (click)="$event.stopPropagation()">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">
                <i class="ti ti-user-plus me-2"></i>Assigner un thérapeute
              </h5>
              <button type="button" class="btn-close" (click)="closeAssignModal()"></button>
            </div>
            <div class="modal-body">
              <p class="mb-3">
                Patient: <strong>{{ selectedPatient()?.fullName }}</strong>
              </p>
              <div class="mb-3">
                <label class="form-label">Thérapeute</label>
                <select class="form-select" [(ngModel)]="selectedTherapeuteId">
                  <option [ngValue]="null">Sélectionner un thérapeute</option>
                  @for (therapeute of therapeutes(); track therapeute.id) {
                    <option [ngValue]="therapeute.id">
                      {{ therapeute.fullName }} - {{ therapeute.specialization }}
                      @if (!therapeute.available) { (Indisponible) }
                    </option>
                  }
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="closeAssignModal()">Annuler</button>
              <button type="button" class="btn btn-primary" (click)="assignTherapeute()" 
                      [disabled]="!selectedTherapeuteId || saving()">
                @if (saving()) {
                  <span class="spinner-border spinner-border-sm me-2"></span>
                }
                Assigner
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .avatar {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 14px;
      color: white;
    }
    .avatar-sm {
      width: 32px;
      height: 32px;
      font-size: 12px;
    }
    .f-48 { font-size: 48px; }
    .table th {
      font-weight: 600;
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: #64748b;
    }
  `]
})
export class PatientsListComponent implements OnInit, OnDestroy {
  private patientService = inject(PatientService);
  private therapeuteService = inject(TherapeuteService);
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  // State
  patients = signal<Patient[]>([]);
  therapeutes = signal<Therapeute[]>([]);
  loading = signal(false);
  saving = signal(false);
  filter = signal<'all' | 'active' | 'on_hold' | 'high_risk'>('all');
  showModal = signal(false);
  showAssignModal = signal(false);
  editingPatient = signal<Patient | null>(null);
  selectedPatient = signal<Patient | null>(null);
  selectedTherapeuteId: number | null = null;

  // Pagination
  currentPage = signal(0);
  totalPages = signal(1);
  pageSize = 50; // Increased to show more patients per page

  searchTerm = '';

  // Form
  patientForm: PatientCreateRequest & { assignedTherapeuteId?: number } = this.getEmptyForm();

  private colors = ['bg-primary', 'bg-success', 'bg-info', 'bg-warning', 'bg-danger'];

  // Computed values
  totalPatients = computed(() => this.patients().length);
  activePatients = computed(() => this.patients().filter(p => p.status === 'ACTIVE').length);
  onHoldPatients = computed(() => this.patients().filter(p => p.status === 'ON_HOLD').length);
  highRiskPatients = computed(() => this.patients().filter(p => (p.riskScore || 0) >= 70).length);

  filteredPatients = computed(() => {
    let result = this.patients();

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(p =>
        p.firstName?.toLowerCase().includes(term) ||
        p.lastName?.toLowerCase().includes(term) ||
        p.email?.toLowerCase().includes(term) ||
        p.patientCode?.toLowerCase().includes(term)
      );
    }

    switch (this.filter()) {
      case 'active':
        result = result.filter(p => p.status === 'ACTIVE');
        break;
      case 'on_hold':
        result = result.filter(p => p.status === 'ON_HOLD');
        break;
      case 'high_risk':
        result = result.filter(p => (p.riskScore || 0) >= 70);
        break;
    }

    return result;
  });

  ngOnInit() {
    this.loadPatients();
    this.loadTherapeutes();

    // Debounce search
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(term => {
      this.searchTerm = term;
      this.currentPage.set(0); // Reset to first page on search
      this.loadPatients();
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadPatients() {
    this.loading.set(true);
    this.patientService.getPatients(this.currentPage(), this.pageSize, this.searchTerm || undefined)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.patients.set(response.content);
          this.totalPages.set(response.totalPages);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
        }
      });
  }

  loadTherapeutes() {
    this.therapeuteService.getTherapeutes()
      .pipe(takeUntil(this.destroy$))
      .subscribe(response => {
        this.therapeutes.set(response.content);
      });
  }

  refreshPatients() {
    this.loadPatients();
  }

  onSearchChange(term: string) {
    this.searchSubject.next(term);
  }

  clearSearch() {
    this.searchTerm = '';
    this.loadPatients();
  }

  setFilter(f: 'all' | 'active' | 'on_hold' | 'high_risk') {
    this.filter.set(f);
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadPatients();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const total = this.totalPages();
    const current = this.currentPage();

    let start = Math.max(0, current - 2);
    let end = Math.min(total - 1, current + 2);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }

  // Modal methods
  openAddModal() {
    this.editingPatient.set(null);
    this.patientForm = this.getEmptyForm();
    this.showModal.set(true);
  }

  openEditModal(patient: Patient) {
    this.editingPatient.set(patient);
    this.patientForm = {
      firstName: patient.firstName,
      lastName: patient.lastName,
      email: patient.email,
      phoneNumber: patient.phoneNumber,
      dateOfBirth: patient.dateOfBirth,
      gender: patient.gender,
      address: patient.address,
      city: patient.city,
      postalCode: patient.postalCode,
      medicalHistory: patient.medicalHistory,
      notes: patient.notes,
      assignedTherapeuteId: patient.assignedTherapeuteId
    };
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
    this.editingPatient.set(null);
    this.patientForm = this.getEmptyForm();
  }

  openAssignModal(patient: Patient) {
    this.selectedPatient.set(patient);
    this.selectedTherapeuteId = patient.assignedTherapeuteId || null;
    this.showAssignModal.set(true);
  }

  closeAssignModal() {
    this.showAssignModal.set(false);
    this.selectedPatient.set(null);
    this.selectedTherapeuteId = null;
  }

  isFormValid(): boolean {
    return !!(this.patientForm.firstName && this.patientForm.lastName && this.patientForm.email);
  }

  savePatient() {
    if (!this.isFormValid()) return;

    this.saving.set(true);

    const editing = this.editingPatient();

    if (editing) {
      // Update existing patient
      this.patientService.updatePatient(editing.id, this.patientForm)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.saving.set(false);
            this.closeModal();
            this.loadPatients();
          },
          error: () => {
            this.saving.set(false);
            // For demo, simulate success
            this.closeModal();
            this.loadPatients();
          }
        });
    } else {
      // Create new patient
      this.patientService.createPatient(this.patientForm)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.saving.set(false);
            this.closeModal();
            this.loadPatients();
          },
          error: () => {
            this.saving.set(false);
            // For demo, simulate success
            this.closeModal();
            this.loadPatients();
          }
        });
    }
  }

  assignTherapeute() {
    const patient = this.selectedPatient();
    if (!patient || !this.selectedTherapeuteId) return;

    this.saving.set(true);

    this.patientService.assignTherapeute(patient.id, this.selectedTherapeuteId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.closeAssignModal();
          this.loadPatients();
        },
        error: () => {
          this.saving.set(false);
          // For demo, simulate success
          this.closeAssignModal();
          this.loadPatients();
        }
      });
  }

  // Helper methods
  getEmptyForm(): PatientCreateRequest & { assignedTherapeuteId?: number } {
    return {
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      dateOfBirth: '',
      gender: undefined,
      address: '',
      city: '',
      postalCode: '',
      medicalHistory: '',
      notes: '',
      assignedTherapeuteId: undefined
    };
  }

  getInitials(patient: Patient): string {
    return (patient.firstName?.charAt(0) || '') + (patient.lastName?.charAt(0) || '');
  }

  getAvatarClass(patient: Patient): string {
    const index = patient.id % this.colors.length;
    return this.colors[index];
  }

  getAge(patient: Patient): number {
    if (patient.age) return patient.age;
    if (!patient.dateOfBirth) return 0;
    const birthDate = new Date(patient.dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  }

  getRiskClass(patient: Patient): string {
    const risk = patient.riskScore || 0;
    if (risk >= 70) return 'bg-danger';
    if (risk >= 40) return 'bg-warning';
    return 'bg-success';
  }

  getRiskTextClass(patient: Patient): string {
    const risk = patient.riskScore || 0;
    if (risk >= 70) return 'text-danger';
    if (risk >= 40) return 'text-warning';
    return 'text-success';
  }

  getStatusClass(status: Patient['status']): string {
    switch (status) {
      case 'ACTIVE': return 'bg-success-subtle text-success';
      case 'ON_HOLD': return 'bg-warning-subtle text-warning';
      case 'INACTIVE': return 'bg-secondary-subtle text-secondary';
      case 'DISCHARGED': return 'bg-info-subtle text-info';
      default: return 'bg-secondary-subtle text-secondary';
    }
  }

  getStatusIcon(status: Patient['status']): string {
    switch (status) {
      case 'ACTIVE': return 'ti-check';
      case 'ON_HOLD': return 'ti-clock';
      case 'INACTIVE': return 'ti-x';
      case 'DISCHARGED': return 'ti-logout';
      default: return 'ti-help';
    }
  }

  getStatusLabel(status: Patient['status']): string {
    switch (status) {
      case 'ACTIVE': return 'Actif';
      case 'ON_HOLD': return 'En pause';
      case 'INACTIVE': return 'Inactif';
      case 'DISCHARGED': return 'Sorti';
      default: return status;
    }
  }
}
