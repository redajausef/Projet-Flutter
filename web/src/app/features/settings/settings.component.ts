import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6 animate-fade-in max-w-4xl">
      <div>
        <h1 class="text-3xl font-bold text-text-primary mb-2">Paramètres</h1>
        <p class="text-text-secondary">Gérez votre compte et vos préférences</p>
      </div>

      <!-- Profile Section -->
      <div class="card">
        <h2 class="text-lg font-semibold text-text-primary mb-6">Profil</h2>
        <div class="flex items-start gap-6">
          <div class="w-24 h-24 rounded-2xl bg-gradient-to-br from-accent to-accent-light 
                      flex items-center justify-center text-background font-bold text-3xl">
            {{ authService.currentUser()?.firstName?.charAt(0) }}{{ authService.currentUser()?.lastName?.charAt(0) }}
          </div>
          <div class="flex-1 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-text-muted mb-2">Prénom</label>
                <input type="text" 
                       [value]="authService.currentUser()?.firstName"
                       class="w-full px-4 py-3 bg-surface-light rounded-xl text-text-primary border border-transparent focus:border-primary focus:outline-none">
              </div>
              <div>
                <label class="block text-sm text-text-muted mb-2">Nom</label>
                <input type="text" 
                       [value]="authService.currentUser()?.lastName"
                       class="w-full px-4 py-3 bg-surface-light rounded-xl text-text-primary border border-transparent focus:border-primary focus:outline-none">
              </div>
            </div>
            <div>
              <label class="block text-sm text-text-muted mb-2">Email</label>
              <input type="email" 
                     [value]="authService.currentUser()?.email"
                     class="w-full px-4 py-3 bg-surface-light rounded-xl text-text-primary border border-transparent focus:border-primary focus:outline-none">
            </div>
          </div>
        </div>
        <div class="flex justify-end mt-6">
          <button class="btn-primary">Sauvegarder</button>
        </div>
      </div>

      <!-- Security Section -->
      <div class="card">
        <h2 class="text-lg font-semibold text-text-primary mb-6">Sécurité</h2>
        <div class="space-y-4">
          <div class="flex items-center justify-between p-4 bg-surface-light rounded-xl">
            <div class="flex items-center gap-4">
              <span class="material-icons text-text-muted">lock</span>
              <div>
                <p class="font-medium text-text-primary">Mot de passe</p>
                <p class="text-sm text-text-muted">Dernière modification il y a 30 jours</p>
              </div>
            </div>
            <button class="btn-outline text-sm py-2">Modifier</button>
          </div>
          <div class="flex items-center justify-between p-4 bg-surface-light rounded-xl">
            <div class="flex items-center gap-4">
              <span class="material-icons text-text-muted">security</span>
              <div>
                <p class="font-medium text-text-primary">Authentification à deux facteurs</p>
                <p class="text-sm text-text-muted">Renforcez la sécurité de votre compte</p>
              </div>
            </div>
            <button class="btn-primary text-sm py-2">Activer</button>
          </div>
        </div>
      </div>

      <!-- Notifications Section -->
      <div class="card">
        <h2 class="text-lg font-semibold text-text-primary mb-6">Notifications</h2>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="font-medium text-text-primary">Notifications par email</p>
              <p class="text-sm text-text-muted">Recevez des mises à jour par email</p>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked class="sr-only peer">
              <div class="w-11 h-6 bg-surface-light rounded-full peer peer-checked:bg-primary transition-colors"></div>
              <div class="absolute left-1 top-1 w-4 h-4 bg-white rounded-full transition-transform peer-checked:translate-x-5"></div>
            </label>
          </div>
          <div class="flex items-center justify-between">
            <div>
              <p class="font-medium text-text-primary">Rappels de séances</p>
              <p class="text-sm text-text-muted">Notifications avant chaque séance</p>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked class="sr-only peer">
              <div class="w-11 h-6 bg-surface-light rounded-full peer peer-checked:bg-primary transition-colors"></div>
              <div class="absolute left-1 top-1 w-4 h-4 bg-white rounded-full transition-transform peer-checked:translate-x-5"></div>
            </label>
          </div>
          <div class="flex items-center justify-between">
            <div>
              <p class="font-medium text-text-primary">Alertes prédictives</p>
              <p class="text-sm text-text-muted">Notifications pour les alertes IA</p>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked class="sr-only peer">
              <div class="w-11 h-6 bg-surface-light rounded-full peer peer-checked:bg-primary transition-colors"></div>
              <div class="absolute left-1 top-1 w-4 h-4 bg-white rounded-full transition-transform peer-checked:translate-x-5"></div>
            </label>
          </div>
        </div>
      </div>

      <!-- Danger Zone -->
      <div class="card border-error/30">
        <h2 class="text-lg font-semibold text-error mb-4">Zone de danger</h2>
        <p class="text-sm text-text-secondary mb-4">
          Ces actions sont irréversibles. Veuillez procéder avec précaution.
        </p>
        <button class="px-4 py-2 bg-error/20 text-error rounded-xl hover:bg-error/30 transition-colors">
          Supprimer mon compte
        </button>
      </div>
    </div>
  `
})
export class SettingsComponent {
  authService = inject(AuthService);
}

