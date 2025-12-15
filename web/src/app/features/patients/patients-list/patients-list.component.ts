import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-patients-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="space-y-6 animate-fade-in">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-text-primary mb-2">Patients</h1>
          <p class="text-text-secondary">Gérez vos patients et leur suivi</p>
        </div>
        <button class="btn-accent flex items-center gap-2">
          <span class="material-icons">person_add</span>
          Nouveau Patient
        </button>
      </div>

      <!-- Filters & Search -->
      <div class="card">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex-1 min-w-[300px] relative">
            <span class="material-icons absolute left-4 top-1/2 -translate-y-1/2 text-text-muted">search</span>
            <input type="text"
                   [(ngModel)]="searchTerm"
                   (input)="onSearch()"
                   placeholder="Rechercher un patient..."
                   class="w-full pl-12 pr-4 py-3 bg-surface-light rounded-xl text-text-primary 
                          border border-transparent focus:border-primary focus:outline-none">
          </div>
          
          <div class="flex items-center gap-2">
            <button (click)="filterStatus = ''"
                    [class.bg-primary]="!filterStatus"
                    [class.text-white]="!filterStatus"
                    class="px-4 py-2 rounded-lg text-sm font-medium transition-colors
                           hover:bg-surface-light text-text-secondary">
              Tous
            </button>
            <button (click)="filterStatus = 'ACTIVE'"
                    [class.bg-success/20]="filterStatus === 'ACTIVE'"
                    [class.text-success]="filterStatus === 'ACTIVE'"
                    class="px-4 py-2 rounded-lg text-sm font-medium transition-colors
                           hover:bg-surface-light text-text-secondary">
              Actifs
            </button>
            <button (click)="filterStatus = 'ON_HOLD'"
                    [class.bg-warning/20]="filterStatus === 'ON_HOLD'"
                    [class.text-warning]="filterStatus === 'ON_HOLD'"
                    class="px-4 py-2 rounded-lg text-sm font-medium transition-colors
                           hover:bg-surface-light text-text-secondary">
              En pause
            </button>
          </div>
        </div>
      </div>

      <!-- Patients Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div *ngFor="let patient of patients()?.content; let i = index"
             [routerLink]="['/patients', patient.id]"
             class="card-hover cursor-pointer animate-fade-in"
             [style.animation-delay.ms]="i * 50">
          <div class="flex items-start justify-between mb-4">
            <div class="flex items-center gap-4">
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-accent to-accent-light 
                          flex items-center justify-center text-background font-bold text-lg">
                {{ patient.firstName?.charAt(0) }}{{ patient.lastName?.charAt(0) }}
              </div>
              <div>
                <h3 class="font-semibold text-text-primary">{{ patient.fullName }}</h3>
                <p class="text-sm text-text-muted">{{ patient.patientCode }}</p>
              </div>
            </div>
            <span class="badge-success" *ngIf="patient.status === 'ACTIVE'">Actif</span>
            <span class="badge-warning" *ngIf="patient.status === 'ON_HOLD'">En pause</span>
          </div>

          <div class="space-y-3 mb-4">
            <div class="flex items-center gap-2 text-sm">
              <span class="material-icons text-text-muted text-base">email</span>
              <span class="text-text-secondary">{{ patient.email }}</span>
            </div>
            <div class="flex items-center gap-2 text-sm">
              <span class="material-icons text-text-muted text-base">psychology</span>
              <span class="text-text-secondary">{{ patient.assignedTherapeuteName || 'Non assigné' }}</span>
            </div>
            <div class="flex items-center gap-2 text-sm">
              <span class="material-icons text-text-muted text-base">event</span>
              <span class="text-text-secondary">{{ patient.totalSeances || 0 }} séances</span>
            </div>
          </div>

          <!-- Risk Score -->
          <div class="pt-4 border-t border-surface-light">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm text-text-muted">Score de risque</span>
              <span class="text-sm font-medium"
                    [class.text-success]="(patient.riskScore || 0) < 30"
                    [class.text-warning]="(patient.riskScore || 0) >= 30 && (patient.riskScore || 0) < 70"
                    [class.text-error]="(patient.riskScore || 0) >= 70">
                {{ patient.riskScore || 0 }}%
              </span>
            </div>
            <div class="w-full h-2 bg-surface-light rounded-full overflow-hidden">
              <div class="h-full rounded-full transition-all duration-500"
                   [style.width.%]="patient.riskScore || 0"
                   [class.bg-success]="(patient.riskScore || 0) < 30"
                   [class.bg-warning]="(patient.riskScore || 0) >= 30 && (patient.riskScore || 0) < 70"
                   [class.bg-error]="(patient.riskScore || 0) >= 70">
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div *ngIf="patients()?.content?.length === 0" 
           class="card text-center py-16">
        <span class="material-icons text-6xl text-text-muted mb-4">person_search</span>
        <h3 class="text-xl font-semibold text-text-primary mb-2">Aucun patient trouvé</h3>
        <p class="text-text-secondary mb-6">Commencez par ajouter un nouveau patient</p>
        <button class="btn-primary">
          <span class="material-icons mr-2">person_add</span>
          Ajouter un patient
        </button>
      </div>

      <!-- Pagination -->
      <div *ngIf="patients()?.totalPages > 1" 
           class="flex items-center justify-center gap-2">
        <button (click)="changePage(currentPage - 1)"
                [disabled]="currentPage === 0"
                class="p-2 rounded-lg hover:bg-surface-light disabled:opacity-50 disabled:cursor-not-allowed">
          <span class="material-icons">chevron_left</span>
        </button>
        
        <button *ngFor="let page of getPageNumbers()"
                (click)="changePage(page)"
                [class.bg-primary]="page === currentPage"
                [class.text-white]="page === currentPage"
                class="w-10 h-10 rounded-lg text-sm font-medium transition-colors
                       hover:bg-surface-light text-text-secondary">
          {{ page + 1 }}
        </button>
        
        <button (click)="changePage(currentPage + 1)"
                [disabled]="currentPage >= (patients()?.totalPages || 1) - 1"
                class="p-2 rounded-lg hover:bg-surface-light disabled:opacity-50 disabled:cursor-not-allowed">
          <span class="material-icons">chevron_right</span>
        </button>
      </div>
    </div>
  `
})
export class PatientsListComponent implements OnInit {
  private apiService = inject(ApiService);

  patients = signal<any>(null);
  searchTerm = '';
  filterStatus = '';
  currentPage = 0;
  pageSize = 9;

  ngOnInit() {
    this.loadPatients();
  }

  loadPatients() {
    this.apiService.getPatients(this.currentPage, this.pageSize, this.searchTerm).subscribe({
      next: (data) => this.patients.set(data),
      error: (err) => console.error('Error loading patients:', err)
    });
  }

  onSearch() {
    this.currentPage = 0;
    this.loadPatients();
  }

  changePage(page: number) {
    this.currentPage = page;
    this.loadPatients();
  }

  getPageNumbers(): number[] {
    const totalPages = this.patients()?.totalPages || 0;
    return Array.from({ length: Math.min(totalPages, 5) }, (_, i) => i);
  }
}

