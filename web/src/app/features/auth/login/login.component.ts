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
    <div class="auth-main">
      <div class="auth-wrapper v3">
        <div class="auth-form">
          <div class="auth-header text-center mb-4">
            <img src="assets/images/logo-dark.svg" alt="logo" style="height: 40px;" />
            <h4 class="mt-3 mb-1">ClinAssist</h4>
            <p class="text-muted">Assistant Clinique Prédictif</p>
          </div>

          <div class="card">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-end mb-4">
                <h4 class="mb-0"><b>Connexion</b></h4>
              </div>

              @if (error()) {
                <div class="alert alert-danger d-flex align-items-center" role="alert">
                  <i class="ti ti-alert-circle me-2"></i>
                  <div>{{ error() }}</div>
                </div>
              }

              <form (ngSubmit)="login()">
                <div class="mb-3">
                  <label class="form-label" for="email">Email ou nom d'utilisateur</label>
                  <input type="text" 
                         class="form-control" 
                         id="email" 
                         [(ngModel)]="username"
                         name="username"
                         placeholder="Entrez votre identifiant" />
                </div>

                <div class="mb-3">
                  <label class="form-label" for="password">Mot de passe</label>
                  <div class="input-group">
                    <input [type]="showPwd ? 'text' : 'password'" 
                           class="form-control" 
                           id="password"
                           [(ngModel)]="password"
                           name="password" 
                           placeholder="••••••••" />
                    <button class="btn btn-outline-secondary" type="button" (click)="showPwd = !showPwd">
                      <i [class]="showPwd ? 'ti ti-eye-off' : 'ti ti-eye'"></i>
                    </button>
                  </div>
                </div>

                <div class="d-flex mt-1 justify-content-between mb-4">
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="remember" />
                    <label class="form-check-label text-muted" for="remember">Se souvenir de moi</label>
                  </div>
                </div>

                <div class="d-grid">
                  <button type="submit" class="btn btn-primary" [disabled]="loading()">
                    @if (loading()) {
                      <span class="spinner-border spinner-border-sm me-2"></span>
                    }
                    Se connecter
                  </button>
                </div>
              </form>

              <div class="saprator my-4">
                <span>Accès Démo</span>
              </div>

              <div class="d-grid">
                <button type="button" class="btn btn-outline-success" (click)="setDemo('therapeute')">
                  <i class="ti ti-stethoscope me-2"></i>Accès Démo Thérapeute
                </button>
              </div>
            </div>
          </div>

          <div class="auth-footer text-center mt-4">
            <p class="text-muted mb-0">
              © 2024 ClinAssist - Projet Académique
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-main {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }
    
    .auth-wrapper {
      width: 100%;
      max-width: 420px;
    }
    
    .auth-form {
      width: 100%;
    }
    
    .auth-header {
      color: white;
      
      h4 { color: white; font-weight: 700; }
      p { color: rgba(255,255,255,0.8); }
    }
    
    .card {
      border: none;
      border-radius: 16px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
    }
    
    .card-body {
      padding: 40px;
    }
    
    .form-control {
      padding: 12px 16px;
      border-radius: 10px;
      border: 1px solid #e0e0e0;
      
      &:focus {
        border-color: #667eea;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      }
    }
    
    .btn-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      padding: 12px;
      border-radius: 10px;
      font-weight: 600;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
      }
    }
    
    .btn-outline-primary, .btn-outline-success {
      border-radius: 10px;
      padding: 10px;
      font-weight: 500;
    }
    
    .saprator {
      position: relative;
      text-align: center;
      
      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 0;
        right: 0;
        height: 1px;
        background: #e0e0e0;
      }
      
      span {
        background: white;
        padding: 0 15px;
        position: relative;
        color: #6c757d;
        font-size: 13px;
      }
    }
    
    .auth-footer p {
      font-size: 13px;
    }
  `]
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  showPwd = false;
  loading = signal(false);
  error = signal<string | null>(null);

  // Demo credentials for therapist login
  demoCredentials = {
    therapeute: { username: 'dr.martin', password: 'password123' }
  };

  setDemo(type: string) {
    this.username = this.demoCredentials.therapeute.username;
    this.password = this.demoCredentials.therapeute.password;
    this.error.set(null);
  }

  login() {
    if (!this.username || !this.password) {
      this.error.set('Veuillez remplir tous les champs');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.loading.set(false);
        this.error.set('Identifiants invalides');
      }
    });
  }
}
