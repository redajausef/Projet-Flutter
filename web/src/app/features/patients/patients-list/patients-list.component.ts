import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

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
          <p class="text-muted mb-0">{{ patients().length }} patients enregistrés</p>
        </div>
        <div class="col-auto">
          <button class="btn btn-primary" (click)="showAddModal = true">
            <i class="ti ti-plus me-2"></i>Nouveau patient
          </button>
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
                     [(ngModel)]="searchTerm" (input)="filterPatients()">
            </div>
          </div>
          <div class="col-md-auto">
            <div class="btn-group">
              <button class="btn" [class.btn-primary]="filter === 'all'" [class.btn-outline-secondary]="filter !== 'all'" (click)="setFilter('all')">Tous</button>
              <button class="btn" [class.btn-primary]="filter === 'active'" [class.btn-outline-secondary]="filter !== 'active'" (click)="setFilter('active')">Actifs</button>
              <button class="btn" [class.btn-primary]="filter === 'paused'" [class.btn-outline-secondary]="filter !== 'paused'" (click)="setFilter('paused')">En pause</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Patients Table -->
    <div class="card">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead>
              <tr>
                <th style="width: 30%">Patient</th>
                <th style="width: 20%">Contact</th>
                <th style="width: 20%">Thérapeute</th>
                <th style="width: 15%">Statut</th>
                <th style="width: 15%" class="text-center">Actions</th>
              </tr>
            </thead>
            <tbody>
              @if (filteredPatients().length === 0) {
                <tr>
                  <td colspan="5" class="text-center py-5">
                    <div class="text-muted">
                      <i class="ti ti-users f-48 d-block mb-3 opacity-50"></i>
                      <p class="mb-0">Aucun patient trouvé</p>
                    </div>
                  </td>
                </tr>
              }
              @for (patient of filteredPatients(); track patient.id) {
                <tr>
                  <td>
                    <div class="d-flex align-items-center">
                      <div class="avatar avatar-s rounded-circle me-3" [class]="getAvatarClass(patient)">
                        {{ getInitials(patient) }}
                      </div>
                      <div>
                        <h6 class="mb-0">{{ patient.firstName }} {{ patient.lastName }}</h6>
                        <small class="text-muted">{{ patient.dateNaissance | date:'dd/MM/yyyy' }}</small>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div>
                      <i class="ti ti-mail me-1 text-muted"></i>{{ patient.email }}
                    </div>
                    <small class="text-muted">
                      <i class="ti ti-phone me-1"></i>{{ patient.telephone || 'Non renseigné' }}
                    </small>
                  </td>
                  <td>
                    <span class="badge bg-primary-subtle text-primary">
                      Dr. {{ patient.therapeute?.lastName || 'Non assigné' }}
                    </span>
                  </td>
                  <td>
                    <span class="badge" [class]="patient.actif ? 'bg-success-subtle text-success' : 'bg-warning-subtle text-warning'">
                      <i class="ti me-1" [class]="patient.actif ? 'ti-check' : 'ti-clock'"></i>
                      {{ patient.actif ? 'Actif' : 'En pause' }}
                    </span>
                  </td>
                  <td class="text-center">
                    <div class="btn-group btn-group-sm">
                      <a [routerLink]="['/patients', patient.id]" class="btn btn-outline-primary">
                        <i class="ti ti-eye"></i>
                      </a>
                      <button class="btn btn-outline-secondary">
                        <i class="ti ti-edit"></i>
                      </button>
                      <button class="btn btn-outline-danger">
                        <i class="ti ti-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Add Patient Modal -->
    @if (showAddModal) {
      <div class="modal fade show d-block" style="background: rgba(0,0,0,0.5)">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Nouveau Patient</h5>
              <button type="button" class="btn-close" (click)="showAddModal = false"></button>
            </div>
            <div class="modal-body">
              <form>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label">Prénom</label>
                    <input type="text" class="form-control" [(ngModel)]="newPatient.firstName" name="firstName">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Nom</label>
                    <input type="text" class="form-control" [(ngModel)]="newPatient.lastName" name="lastName">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Email</label>
                    <input type="email" class="form-control" [(ngModel)]="newPatient.email" name="email">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Téléphone</label>
                    <input type="tel" class="form-control" [(ngModel)]="newPatient.telephone" name="telephone">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Date de naissance</label>
                    <input type="date" class="form-control" [(ngModel)]="newPatient.dateNaissance" name="dateNaissance">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Adresse</label>
                    <input type="text" class="form-control" [(ngModel)]="newPatient.adresse" name="adresse">
                  </div>
                </div>
              </form>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showAddModal = false">Annuler</button>
              <button type="button" class="btn btn-primary" (click)="savePatient()">
                <i class="ti ti-check me-2"></i>Enregistrer
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
    .f-48 { font-size: 48px; }
  `]
})
export class PatientsListComponent implements OnInit {
  private apiService = inject(ApiService);
  
  patients = signal<any[]>([]);
  filteredPatients = signal<any[]>([]);
  searchTerm = '';
  filter = 'all';
  showAddModal = false;
  
  newPatient = {
    firstName: '',
    lastName: '',
    email: '',
    telephone: '',
    dateNaissance: '',
    adresse: ''
  };

  private colors = ['bg-primary', 'bg-success', 'bg-warning', 'bg-info', 'bg-danger'];

  ngOnInit() {
    this.loadPatients();
  }

  loadPatients() {
    this.apiService.getPatients().subscribe({
      next: (data) => {
        this.patients.set(data);
        this.filterPatients();
      },
      error: () => {
        // Demo data
        this.patients.set([
          { id: 1, firstName: 'Marie', lastName: 'Dupont', email: 'marie.dupont@email.com', telephone: '06 12 34 56 78', dateNaissance: '1985-03-15', actif: true, therapeute: { lastName: 'Martin' } },
          { id: 2, firstName: 'Jean', lastName: 'Martin', email: 'jean.martin@email.com', telephone: '06 98 76 54 32', dateNaissance: '1990-07-22', actif: true, therapeute: { lastName: 'Martin' } },
          { id: 3, firstName: 'Sophie', lastName: 'Bernard', email: 'sophie.b@email.com', telephone: '06 11 22 33 44', dateNaissance: '1978-11-08', actif: false, therapeute: { lastName: 'Martin' } },
          { id: 4, firstName: 'Pierre', lastName: 'Leroy', email: 'p.leroy@email.com', telephone: '06 55 44 33 22', dateNaissance: '1995-01-30', actif: true, therapeute: { lastName: 'Martin' } },
          { id: 5, firstName: 'Claire', lastName: 'Moreau', email: 'claire.moreau@email.com', telephone: '06 77 88 99 00', dateNaissance: '1982-09-12', actif: true, therapeute: { lastName: 'Martin' } }
        ]);
        this.filterPatients();
      }
    });
  }

  filterPatients() {
    let result = this.patients();
    
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(p => 
        p.firstName.toLowerCase().includes(term) || 
        p.lastName.toLowerCase().includes(term) ||
        p.email.toLowerCase().includes(term)
      );
    }
    
    if (this.filter === 'active') {
      result = result.filter(p => p.actif);
    } else if (this.filter === 'paused') {
      result = result.filter(p => !p.actif);
    }
    
    this.filteredPatients.set(result);
  }

  setFilter(f: string) {
    this.filter = f;
    this.filterPatients();
  }

  getInitials(patient: any): string {
    return (patient.firstName?.charAt(0) || '') + (patient.lastName?.charAt(0) || '');
  }

  getAvatarClass(patient: any): string {
    const index = patient.id % this.colors.length;
    return this.colors[index];
  }

  savePatient() {
    // TODO: Implement save
    this.showAddModal = false;
  }
}
