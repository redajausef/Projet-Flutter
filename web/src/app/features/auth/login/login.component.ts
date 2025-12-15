import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen flex">
      <!-- Left Panel - Branding -->
      <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary to-primary-dark 
                  relative overflow-hidden">
        <!-- Background Pattern -->
        <div class="absolute inset-0 opacity-10">
          <div class="absolute top-20 left-20 w-64 h-64 rounded-full bg-white/20 blur-3xl"></div>
          <div class="absolute bottom-20 right-20 w-96 h-96 rounded-full bg-accent/20 blur-3xl"></div>
        </div>
        
        <div class="relative z-10 flex flex-col justify-center px-16">
          <div class="mb-8">
            <div class="w-20 h-20 rounded-2xl bg-white/10 backdrop-blur flex items-center justify-center mb-6 shadow-glow">
              <span class="material-icons text-4xl text-white">medical_services</span>
            </div>
            <h1 class="text-5xl font-bold text-white mb-4">ClinAssist</h1>
            <p class="text-xl text-white/70">Assistant Clinique Prédictif</p>
          </div>
          
          <div class="space-y-6">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-xl bg-white/10 flex items-center justify-center">
                <span class="material-icons text-accent">insights</span>
              </div>
              <div>
                <h3 class="text-white font-semibold">Analyse Prédictive</h3>
                <p class="text-white/60 text-sm">IA avancée pour anticiper les besoins</p>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-xl bg-white/10 flex items-center justify-center">
                <span class="material-icons text-accent">calendar_month</span>
              </div>
              <div>
                <h3 class="text-white font-semibold">Planification Intelligente</h3>
                <p class="text-white/60 text-sm">Optimisation automatique des séances</p>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-xl bg-white/10 flex items-center justify-center">
                <span class="material-icons text-accent">security</span>
              </div>
              <div>
                <h3 class="text-white font-semibold">Sécurité Maximale</h3>
                <p class="text-white/60 text-sm">Données protégées et chiffrées</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Panel - Login Form -->
      <div class="flex-1 flex items-center justify-center p-8 bg-background">
        <div class="w-full max-w-md">
          <!-- Mobile Logo -->
          <div class="lg:hidden flex items-center gap-3 mb-10">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-primary to-primary-light flex items-center justify-center shadow-glow">
              <span class="material-icons text-white">medical_services</span>
            </div>
            <span class="text-2xl font-bold gradient-text">ClinAssist</span>
          </div>

          <div class="mb-8">
            <h2 class="text-3xl font-bold text-text-primary mb-2">Connexion</h2>
            <p class="text-text-secondary">Accédez à votre espace de gestion</p>
          </div>

          <!-- Error Message -->
          <div *ngIf="error()" 
               class="mb-6 p-4 bg-error/10 border border-error/30 rounded-xl flex items-center gap-3">
            <span class="material-icons text-error">error</span>
            <span class="text-error text-sm">{{ error() }}</span>
          </div>

          <!-- Form -->
          <form (ngSubmit)="onSubmit()" class="space-y-5">
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-2">
                Email ou nom d'utilisateur
              </label>
              <div class="relative">
                <span class="material-icons absolute left-4 top-1/2 -translate-y-1/2 text-text-muted">person</span>
                <input type="text"
                       [(ngModel)]="username"
                       name="username"
                       required
                       class="w-full pl-12 pr-4 py-4 bg-surface-light rounded-xl text-text-primary 
                              border border-transparent focus:border-primary focus:outline-none
                              placeholder-text-muted transition-colors"
                       placeholder="Entrez votre identifiant">
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-2">
                Mot de passe
              </label>
              <div class="relative">
                <span class="material-icons absolute left-4 top-1/2 -translate-y-1/2 text-text-muted">lock</span>
                <input [type]="showPassword() ? 'text' : 'password'"
                       [(ngModel)]="password"
                       name="password"
                       required
                       class="w-full pl-12 pr-12 py-4 bg-surface-light rounded-xl text-text-primary 
                              border border-transparent focus:border-primary focus:outline-none
                              placeholder-text-muted transition-colors"
                       placeholder="Entrez votre mot de passe">
                <button type="button"
                        (click)="showPassword.set(!showPassword())"
                        class="absolute right-4 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-secondary">
                  <span class="material-icons">{{ showPassword() ? 'visibility_off' : 'visibility' }}</span>
                </button>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <label class="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" class="w-4 h-4 rounded border-surface-light bg-surface-light 
                                              text-primary focus:ring-primary">
                <span class="text-sm text-text-secondary">Se souvenir de moi</span>
              </label>
              <a href="#" class="text-sm text-accent hover:text-accent-light">Mot de passe oublié ?</a>
            </div>

            <button type="submit"
                    [disabled]="loading()"
                    class="w-full py-4 bg-gradient-to-r from-primary to-primary-light text-white font-semibold
                           rounded-xl hover:shadow-glow transition-all duration-300 disabled:opacity-50
                           flex items-center justify-center gap-2">
              <span *ngIf="loading()" class="material-icons animate-spin">refresh</span>
              <span>{{ loading() ? 'Connexion...' : 'Se connecter' }}</span>
            </button>
          </form>

          <!-- Demo Credentials -->
          <div class="mt-8 p-4 bg-surface-light/50 rounded-xl border border-primary/20">
            <div class="flex items-center gap-2 text-accent mb-3">
              <span class="material-icons text-lg">info</span>
              <span class="font-semibold text-sm">Accès Démo</span>
            </div>
            <div class="space-y-2 text-sm">
              <div class="flex items-center gap-3">
                <span class="px-2 py-1 bg-primary/20 rounded text-text-primary text-xs">Admin</span>
                <code class="text-text-secondary">admin / admin123</code>
              </div>
              <div class="flex items-center gap-3">
                <span class="px-2 py-1 bg-primary/20 rounded text-text-primary text-xs">Thérapeute</span>
                <code class="text-text-secondary">dr.martin / password123</code>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  showPassword = signal(false);
  loading = signal(false);
  error = signal<string | null>(null);

  onSubmit() {
    if (!this.username || !this.password) {
      this.error.set('Veuillez remplir tous les champs');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Identifiants incorrects');
      }
    });
  }
}

