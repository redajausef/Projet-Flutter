import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-therapeutes-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="animate-fade-in">
      <div class="flex items-center justify-between mb-6">
        <div class="page-header mb-0">
          <h1 class="page-title">Équipe Médicale</h1>
          <p class="page-subtitle">Gestion des thérapeutes de la plateforme</p>
        </div>
        <button class="btn-primary">
          <span class="material-icons text-lg">person_add</span>
          Nouveau Thérapeute
        </button>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <div class="stat-card">
          <div class="stat-icon primary">
            <span class="material-icons">medical_services</span>
          </div>
          <div class="stat-value">{{ therapeutes().length }}</div>
          <div class="stat-label">Thérapeutes</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon success">
            <span class="material-icons">check_circle</span>
          </div>
          <div class="stat-value">{{ getAvailableCount() }}</div>
          <div class="stat-label">Disponibles</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon info">
            <span class="material-icons">people</span>
          </div>
          <div class="stat-value">{{ getTotalPatients() }}</div>
          <div class="stat-label">Patients suivis</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon warning">
            <span class="material-icons">star</span>
          </div>
          <div class="stat-value">4.8</div>
          <div class="stat-label">Note moyenne</div>
        </div>
      </div>

      <!-- Search -->
      <div class="card mb-6">
        <div class="flex items-center gap-4">
          <div class="flex-1 relative">
            <span class="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">search</span>
            <input type="text"
                   [(ngModel)]="searchTerm"
                   class="input pl-10"
                   placeholder="Rechercher un thérapeute...">
          </div>
          <select class="input w-48">
            <option value="">Toutes spécialités</option>
            <option value="psychologie">Psychologie</option>
            <option value="psychiatrie">Psychiatrie</option>
            <option value="therapie">Thérapie familiale</option>
          </select>
        </div>
      </div>

      <!-- Therapeutes Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        @for (therapeute of filteredTherapeutes(); track therapeute.id) {
          <div class="card card-hover">
            <div class="flex items-start gap-4 mb-4">
              <div class="w-16 h-16 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white font-bold text-xl">
                {{ therapeute.firstName?.charAt(0) }}{{ therapeute.lastName?.charAt(0) }}
              </div>
              <div class="flex-1">
                <h3 class="font-semibold text-gray-800">Dr. {{ therapeute.fullName }}</h3>
                <p class="text-sm text-gray-500">{{ therapeute.specialization || 'Psychologue' }}</p>
                <div class="flex items-center gap-1 mt-1">
                  @for (star of [1,2,3,4,5]; track star) {
                    <span class="material-icons text-sm text-amber-400">star</span>
                  }
                  <span class="text-xs text-gray-500 ml-1">4.9</span>
                </div>
              </div>
            </div>

            <div class="space-y-3 mb-4">
              <div class="flex items-center gap-2 text-sm">
                <span class="material-icons text-gray-400 text-lg">email</span>
                <span class="text-gray-600">{{ therapeute.email }}</span>
              </div>
              <div class="flex items-center gap-2 text-sm">
                <span class="material-icons text-gray-400 text-lg">phone</span>
                <span class="text-gray-600">{{ therapeute.phoneNumber || 'Non renseigné' }}</span>
              </div>
              <div class="flex items-center gap-2 text-sm">
                <span class="material-icons text-gray-400 text-lg">people</span>
                <span class="text-gray-600">{{ therapeute.patientCount || 0 }} patients</span>
              </div>
            </div>

            <div class="flex items-center justify-between pt-4 border-t">
              @if (therapeute.isAvailable) {
                <span class="badge-success">Disponible</span>
              } @else {
                <span class="badge-warning">Occupé</span>
              }
              <div class="flex gap-1">
                <button class="p-2 hover:bg-gray-100 rounded-lg" title="Voir profil">
                  <span class="material-icons text-gray-500 text-lg">visibility</span>
                </button>
                <button class="p-2 hover:bg-gray-100 rounded-lg" title="Modifier">
                  <span class="material-icons text-gray-500 text-lg">edit</span>
                </button>
              </div>
            </div>
          </div>
        } @empty {
          <div class="col-span-3 card text-center py-12">
            <span class="material-icons text-5xl text-gray-300 mb-3">medical_services</span>
            <p class="text-gray-500">Aucun thérapeute trouvé</p>
          </div>
        }
      </div>
    </div>
  `
})
export class TherapeutesListComponent implements OnInit {
  private apiService = inject(ApiService);

  therapeutes = signal<any[]>([]);
  searchTerm = '';

  ngOnInit() {
    this.loadTherapeutes();
  }

  loadTherapeutes() {
    this.apiService.getTherapeutes().subscribe({
      next: (data) => this.therapeutes.set(data),
      error: (err) => console.error('Error loading therapeutes:', err)
    });
  }

  filteredTherapeutes() {
    const list = this.therapeutes();
    if (!this.searchTerm) return list;
    const term = this.searchTerm.toLowerCase();
    return list.filter((t: any) => 
      t.fullName?.toLowerCase().includes(term) || 
      t.email?.toLowerCase().includes(term)
    );
  }

  getAvailableCount(): number {
    return this.therapeutes().filter((t: any) => t.isAvailable).length;
  }

  getTotalPatients(): number {
    return this.therapeutes().reduce((sum: number, t: any) => sum + (t.patientCount || 0), 0);
  }
}
