import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-therapeutes-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6 animate-fade-in">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-text-primary mb-2">Thérapeutes</h1>
          <p class="text-text-secondary">Gérez votre équipe médicale</p>
        </div>
        <button class="btn-accent flex items-center gap-2">
          <span class="material-icons">person_add</span>
          Ajouter Thérapeute
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <!-- Therapeute Card Example -->
        <div class="card-hover">
          <div class="flex items-start gap-4 mb-4">
            <img src="https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=200" 
                 alt="Dr. Sophie Martin"
                 class="w-16 h-16 rounded-xl object-cover">
            <div>
              <h3 class="font-semibold text-text-primary">Dr. Sophie Martin</h3>
              <p class="text-sm text-text-muted">Psychologie Clinique</p>
              <span class="badge-success mt-2">Disponible</span>
            </div>
          </div>
          <div class="space-y-2 text-sm">
            <div class="flex items-center gap-2">
              <span class="material-icons text-text-muted text-base">people</span>
              <span class="text-text-secondary">12 patients assignés</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="material-icons text-text-muted text-base">star</span>
              <span class="text-text-secondary">4.9 (127 avis)</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="material-icons text-text-muted text-base">schedule</span>
              <span class="text-text-secondary">15 ans d'expérience</span>
            </div>
          </div>
          <div class="flex gap-2 mt-4 pt-4 border-t border-surface-light">
            <button class="flex-1 btn-outline text-sm py-2">Voir profil</button>
            <button class="flex-1 btn-primary text-sm py-2">Planifier</button>
          </div>
        </div>

        <div class="card-hover">
          <div class="flex items-start gap-4 mb-4">
            <img src="https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=200" 
                 alt="Dr. Jean Dubois"
                 class="w-16 h-16 rounded-xl object-cover">
            <div>
              <h3 class="font-semibold text-text-primary">Dr. Jean Dubois</h3>
              <p class="text-sm text-text-muted">Thérapie Familiale</p>
              <span class="badge-success mt-2">Disponible</span>
            </div>
          </div>
          <div class="space-y-2 text-sm">
            <div class="flex items-center gap-2">
              <span class="material-icons text-text-muted text-base">people</span>
              <span class="text-text-secondary">8 patients assignés</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="material-icons text-text-muted text-base">star</span>
              <span class="text-text-secondary">4.7 (89 avis)</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="material-icons text-text-muted text-base">schedule</span>
              <span class="text-text-secondary">10 ans d'expérience</span>
            </div>
          </div>
          <div class="flex gap-2 mt-4 pt-4 border-t border-surface-light">
            <button class="flex-1 btn-outline text-sm py-2">Voir profil</button>
            <button class="flex-1 btn-primary text-sm py-2">Planifier</button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class TherapeutesListComponent {}

