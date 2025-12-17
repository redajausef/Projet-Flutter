import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TherapeuteService } from '../../core/services/therapeute.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <!-- Sidebar -->
    <aside class="sidebar" [class.collapsed]="collapsed()">
      <div class="sidebar-header">
        <a routerLink="/dashboard" class="logo">
          <div class="logo-icon">
            <i class="ti ti-heart-rate-monitor"></i>
          </div>
          <span class="logo-text">ClinAssist</span>
        </a>
      </div>
      
      <nav class="sidebar-nav">
        <div class="nav-section">
          <span class="nav-section-title">Navigation</span>
        </div>
        
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">
          <i class="ti ti-dashboard"></i>
          <span>Tableau de bord</span>
        </a>
        
        <a routerLink="/patients" routerLinkActive="active" class="nav-item">
          <i class="ti ti-users"></i>
          <span>Patients</span>
        </a>
        
        <a routerLink="/seances" routerLinkActive="active" class="nav-item">
          <i class="ti ti-calendar"></i>
          <span>Séances</span>
        </a>
        
        <a routerLink="/predictions" routerLinkActive="active" class="nav-item">
          <i class="ti ti-chart-dots"></i>
          <span>Prédictions IA</span>
        </a>
      </nav>
    </aside>

    <!-- Header -->
    <header class="header">
      <div class="header-left">
        <button class="menu-toggle" (click)="toggleSidebar()">
          <i class="ti ti-menu-2"></i>
        </button>
        <div class="search-box">
          <i class="ti ti-search"></i>
          <input type="text" placeholder="Rechercher..." />
        </div>
      </div>
      
      <div class="header-right">
        <button class="header-btn">
          <i class="ti ti-bell"></i>
          <span class="badge">3</span>
        </button>
        
        <div class="user-dropdown dropdown">
          <button class="user-btn dropdown-toggle" data-bs-toggle="dropdown">
            <div class="user-avatar">
              {{ getInitials() }}
            </div>
            <div class="user-info">
              <span class="user-name">{{ authService.currentUser()?.firstName }} {{ authService.currentUser()?.lastName }}</span>
              <span class="user-role">{{ getRole() }}</span>
            </div>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li><a class="dropdown-item" href="#"><i class="ti ti-user me-2"></i>Mon profil</a></li>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item text-danger" (click)="logout()"><i class="ti ti-logout me-2"></i>Déconnexion</a></li>
          </ul>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="main-content">
      <div class="content-wrapper">
        <router-outlet></router-outlet>
      </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
      <span>ClinAssist © 2024 - Projet Académique</span>
    </footer>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
    }
    
    /* Sidebar */
    .sidebar {
      position: fixed;
      top: 0;
      left: 0;
      width: 260px;
      height: 100vh;
      background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
      z-index: 1000;
      transition: all 0.3s ease;
    }
    
    .sidebar.collapsed {
      width: 70px;
      
      .logo-text, .nav-section-title, .nav-item span {
        display: none;
      }
    }
    
    .sidebar-header {
      padding: 20px;
      border-bottom: 1px solid rgba(255,255,255,0.1);
    }
    
    .logo {
      display: flex;
      align-items: center;
      text-decoration: none;
      gap: 12px;
    }
    
    .logo-icon {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      i {
        font-size: 22px;
        color: white;
      }
    }
    
    .logo-text {
      color: white;
      font-size: 20px;
      font-weight: 700;
    }
    
    .sidebar-nav {
      padding: 20px 12px;
    }
    
    .nav-section {
      padding: 0 12px;
      margin-bottom: 8px;
    }
    
    .nav-section-title {
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: 1px;
      color: rgba(255,255,255,0.4);
    }
    
    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      color: rgba(255,255,255,0.7);
      text-decoration: none;
      border-radius: 8px;
      margin-bottom: 4px;
      transition: all 0.2s;
      
      i {
        font-size: 20px;
        width: 24px;
        text-align: center;
      }
      
      &:hover {
        background: rgba(255,255,255,0.1);
        color: white;
      }
      
      &.active {
        background: rgba(79, 70, 229, 0.3);
        color: white;
      }
    }
    
    /* Header */
    .header {
      position: fixed;
      top: 0;
      left: 260px;
      right: 0;
      height: 70px;
      background: white;
      border-bottom: 1px solid #e5e7eb;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 24px;
      z-index: 999;
      transition: all 0.3s ease;
    }
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 20px;
    }
    
    .menu-toggle {
      background: none;
      border: none;
      font-size: 24px;
      color: #6b7280;
      cursor: pointer;
      padding: 8px;
      border-radius: 8px;
      transition: all 0.2s;
      
      &:hover {
        background: #f3f4f6;
        color: #4F46E5;
      }
    }
    
    .search-box {
      display: flex;
      align-items: center;
      background: #f3f4f6;
      border-radius: 10px;
      padding: 10px 16px;
      gap: 10px;
      min-width: 280px;
      
      i {
        color: #9ca3af;
        font-size: 18px;
      }
      
      input {
        border: none;
        background: none;
        outline: none;
        width: 100%;
        font-size: 14px;
        
        &::placeholder {
          color: #9ca3af;
        }
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    
    .header-btn {
      position: relative;
      background: none;
      border: none;
      font-size: 22px;
      color: #6b7280;
      cursor: pointer;
      padding: 10px;
      border-radius: 10px;
      transition: all 0.2s;
      
      &:hover {
        background: #f3f4f6;
        color: #4F46E5;
      }
      
      .badge {
        position: absolute;
        top: 4px;
        right: 4px;
        background: #EF4444;
        color: white;
        font-size: 10px;
        padding: 2px 6px;
        border-radius: 10px;
        font-weight: 600;
      }
    }
    
    .user-btn {
      display: flex;
      align-items: center;
      gap: 12px;
      background: none;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      padding: 8px 16px 8px 8px;
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover {
        background: #f9fafb;
        border-color: #d1d5db;
      }
      
      &::after {
        display: none;
      }
    }
    
    .user-avatar {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
      color: white;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 14px;
    }
    
    .user-info {
      text-align: left;
    }
    
    .user-name {
      display: block;
      font-weight: 600;
      color: #1f2937;
      font-size: 14px;
    }
    
    .user-role {
      display: block;
      font-size: 12px;
      color: #6b7280;
    }
    
    /* Main Content */
    .main-content {
      margin-left: 260px;
      margin-top: 70px;
      min-height: calc(100vh - 70px - 60px);
      background: #f8fafc;
      transition: all 0.3s ease;
    }
    
    .content-wrapper {
      padding: 24px;
    }
    
    /* Footer */
    .footer {
      margin-left: 260px;
      padding: 20px 24px;
      background: white;
      border-top: 1px solid #e5e7eb;
      color: #6b7280;
      font-size: 14px;
      transition: all 0.3s ease;
    }
    
    /* Dropdown Menu */
    .dropdown-menu {
      border: none;
      box-shadow: 0 10px 40px rgba(0,0,0,0.15);
      border-radius: 12px;
      padding: 8px;
      min-width: 200px;
    }
    
    .dropdown-item {
      padding: 10px 16px;
      border-radius: 8px;
      font-size: 14px;
      
      &:hover {
        background: #f3f4f6;
      }
    }
  `]
})
export class MainLayoutComponent implements OnInit {
  authService = inject(AuthService);
  therapeuteService = inject(TherapeuteService);
  collapsed = signal(false);

  ngOnInit(): void {
    // If user is a therapeute, load their profile
    if (this.authService.isTherapeute()) {
      this.loadCurrentTherapeute();
    }
  }

  private loadCurrentTherapeute(): void {
    // First try to load from localStorage
    this.therapeuteService.loadCurrentTherapeute();

    // Then fetch from API to ensure fresh data
    const userId = this.authService.getUserId();
    if (userId) {
      this.therapeuteService.getTherapeuteByUserId(userId).subscribe({
        next: (therapeute) => {
          this.authService.setTherapeuteId(therapeute.id);
          console.log('Therapeute loaded:', therapeute.fullName, 'ID:', therapeute.id);
        },
        error: (err) => {
          console.error('Error loading therapeute:', err);
        }
      });
    }
  }

  getInitials(): string {
    const u = this.authService.currentUser();
    return u ? (u.firstName?.charAt(0) || '') + (u.lastName?.charAt(0) || '') : '';
  }

  getRole(): string {
    return this.authService.isAdmin() ? 'Administrateur' : 'Thérapeute';
  }

  toggleSidebar() {
    this.collapsed.update(v => !v);
  }

  logout() {
    this.authService.logout();
    this.therapeuteService.clearCurrentTherapeute();
  }
}
