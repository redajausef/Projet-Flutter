import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <!-- Page Header -->
    <div class="page-header mb-4">
      <h4 class="mb-1">Tableau de bord</h4>
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-0">
          <li class="breadcrumb-item">Accueil</li>
          <li class="breadcrumb-item active">Dashboard</li>
        </ol>
      </nav>
    </div>

    <!-- Stats Cards -->
    <div class="row g-4 mb-4">
      @for (stat of analyticsCards; track stat.title) {
        <div class="col-sm-6 col-xl-3">
          <div class="card stat-card h-100">
            <div class="card-body">
              <h6 class="text-muted fw-normal mb-3">{{ stat.title }}</h6>
              <div class="d-flex align-items-center mb-3">
                <h2 class="mb-0 me-3">{{ stat.value }}</h2>
                <span class="badge rounded-pill" [class]="stat.badgeClass">
                  <i [class]="stat.icon" class="me-1"></i>{{ stat.change }}
                </span>
              </div>
              <p class="text-muted mb-0">
                <span [class]="stat.textClass" class="fw-medium">{{ stat.extra }}</span> {{ stat.extraText }}
              </p>
            </div>
          </div>
        </div>
      }
    </div>

    <!-- Charts Row -->
    <div class="row g-4 mb-4">
      <!-- Weekly Activity Chart -->
      <div class="col-lg-8">
        <div class="card h-100">
          <div class="card-header bg-transparent py-3">
            <div class="d-flex justify-content-between align-items-center">
              <h5 class="mb-0">Activité Hebdomadaire</h5>
              <div class="btn-group">
                <button class="btn btn-sm btn-primary">Semaine</button>
                <button class="btn btn-sm btn-outline-secondary">Mois</button>
              </div>
            </div>
          </div>
          <div class="card-body pt-4">
            <div class="chart-wrapper">
              <div class="chart-bars">
                @for (day of weeklyData; track day.name) {
                  <div class="chart-column">
                    <div class="bar-wrapper">
                      <div class="bar" [style.height.%]="day.value" [class.active]="day.isToday"></div>
                    </div>
                    <div class="bar-label" [class.active]="day.isToday">{{ day.name }}</div>
                    <div class="bar-value">{{ day.count }}</div>
                  </div>
                }
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sessions Overview -->
      <div class="col-lg-4">
        <div class="card h-100">
          <div class="card-header bg-transparent py-3">
            <h5 class="mb-0">Types de Séances</h5>
          </div>
          <div class="card-body pt-4">
            @for (type of sessionTypes; track type.name; let last = $last) {
              <div class="type-item" [class.mb-4]="!last">
                <div class="d-flex align-items-center">
                  <div class="type-icon" [class]="type.bgClass">
                    <i [class]="type.icon"></i>
                  </div>
                  <div class="flex-grow-1 ms-3">
                    <div class="d-flex justify-content-between mb-2">
                      <span class="fw-medium">{{ type.name }}</span>
                      <span class="fw-bold">{{ type.percent }}%</span>
                    </div>
                    <div class="progress">
                      <div class="progress-bar" [class]="type.barClass" [style.width.%]="type.percent"></div>
                    </div>
                  </div>
                </div>
              </div>
            }
          </div>
        </div>
      </div>
    </div>

    <!-- Tables Row -->
    <div class="row g-4 mb-4">
      <!-- Recent Sessions -->
      <div class="col-lg-8">
        <div class="card">
          <div class="card-header bg-transparent d-flex justify-content-between align-items-center py-3">
            <h5 class="mb-0">Séances Récentes</h5>
            <a routerLink="/seances" class="btn btn-sm btn-primary">Voir tout</a>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead>
                <tr>
                  <th class="ps-4">Patient</th>
                  <th>Type</th>
                  <th>Statut</th>
                  <th>Date</th>
                  <th class="text-end pe-4">Durée</th>
                </tr>
              </thead>
              <tbody>
                @for (session of recentSessions; track session.id) {
                  <tr>
                    <td class="ps-4">
                      <div class="d-flex align-items-center">
                        <div class="avatar" [class]="session.avatarClass">
                          {{ session.initials }}
                        </div>
                        <span class="ms-3 fw-medium">{{ session.patient }}</span>
                      </div>
                    </td>
                    <td class="text-muted">{{ session.type }}</td>
                    <td>
                      <span class="badge" [class]="session.statusClass">
                        <i [class]="session.statusIcon" class="me-1"></i>
                        {{ session.status }}
                      </span>
                    </td>
                    <td class="text-muted">{{ session.date }}</td>
                    <td class="text-end pe-4 fw-medium">{{ session.duration }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- AI Predictions -->
      <div class="col-lg-4">
        <div class="card h-100">
          <div class="card-header bg-transparent d-flex justify-content-between align-items-center py-3">
            <h5 class="mb-0">Alertes IA</h5>
            <span class="badge bg-danger rounded-pill px-3">{{ alerts.length }}</span>
          </div>
          <div class="card-body p-0">
            @for (alert of alerts; track alert.id; let last = $last) {
              <div class="alert-item" [class.border-bottom]="!last">
                <div class="d-flex">
                  <div class="alert-icon" [class]="alert.level === 'high' ? 'danger' : 'warning'">
                    <i class="ti ti-alert-triangle"></i>
                  </div>
                  <div class="flex-grow-1">
                    <div class="d-flex justify-content-between align-items-start mb-1">
                      <h6 class="mb-0">{{ alert.patient }}</h6>
                      <span class="score" [class]="alert.level === 'high' ? 'text-danger' : 'text-warning'">
                        {{ alert.score }}%
                      </span>
                    </div>
                    <p class="text-muted small mb-0">{{ alert.message }}</p>
                  </div>
                </div>
              </div>
            }
          </div>
          <div class="card-footer bg-transparent text-center py-3">
            <a routerLink="/predictions" class="text-primary text-decoration-none fw-medium">
              Voir toutes les alertes <i class="ti ti-arrow-right ms-1"></i>
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="card">
      <div class="card-body py-4">
        <div class="row g-4">
          <div class="col-6 col-md-3">
            <a routerLink="/patients" class="quick-action">
              <div class="quick-icon bg-primary">
                <i class="ti ti-user-plus"></i>
              </div>
              <h6>Nouveau Patient</h6>
              <small>Ajouter un patient</small>
            </a>
          </div>
          <div class="col-6 col-md-3">
            <a routerLink="/seances" class="quick-action">
              <div class="quick-icon bg-success">
                <i class="ti ti-calendar-plus"></i>
              </div>
              <h6>Planifier Séance</h6>
              <small>Nouveau rendez-vous</small>
            </a>
          </div>
          <div class="col-6 col-md-3">
            <a routerLink="/predictions" class="quick-action">
              <div class="quick-icon bg-warning">
                <i class="ti ti-chart-dots"></i>
              </div>
              <h6>Analyse IA</h6>
              <small>Lancer une prédiction</small>
            </a>
          </div>
          <div class="col-6 col-md-3">
            <a href="javascript:void(0)" class="quick-action">
              <div class="quick-icon bg-info">
                <i class="ti ti-report"></i>
              </div>
              <h6>Rapports</h6>
              <small>Générer un rapport</small>
            </a>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    /* Cards */
    .card {
      border: none;
      border-radius: 16px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.06);
    }
    
    .stat-card {
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(0,0,0,0.1);
      }
      
      h2 {
        font-size: 2rem;
        font-weight: 700;
      }
    }
    
    .card-header {
      border-bottom: 1px solid #f0f0f0;
      padding: 16px 20px;
    }
    
    .card-body {
      padding: 20px;
    }
    
    .card-footer {
      border-top: 1px solid #f0f0f0;
    }
    
    /* Chart */
    .chart-wrapper {
      padding: 10px 0;
    }
    
    .chart-bars {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      height: 220px;
      gap: 16px;
    }
    
    .chart-column {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
    }
    
    .bar-wrapper {
      width: 100%;
      height: 160px;
      background: #f5f5f5;
      border-radius: 10px;
      display: flex;
      align-items: flex-end;
      overflow: hidden;
    }
    
    .bar {
      width: 100%;
      background: linear-gradient(180deg, rgba(79, 70, 229, 0.5) 0%, rgba(79, 70, 229, 0.2) 100%);
      border-radius: 10px 10px 0 0;
      transition: height 0.5s ease;
      
      &.active {
        background: linear-gradient(180deg, #4F46E5 0%, rgba(79, 70, 229, 0.6) 100%);
      }
    }
    
    .bar-label {
      font-weight: 500;
      color: #6b7280;
      
      &.active {
        color: #4F46E5;
        font-weight: 700;
      }
    }
    
    .bar-value {
      font-size: 13px;
      color: #9ca3af;
    }
    
    /* Type Items */
    .type-icon {
      width: 42px;
      height: 42px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 18px;
    }
    
    .progress {
      height: 8px;
      border-radius: 4px;
      background: #f0f0f0;
    }
    
    /* Avatar */
    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 14px;
      color: white;
    }
    
    /* Table */
    .table {
      thead tr {
        background: #fafafa;
      }
      
      th {
        font-size: 11px;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        color: #6b7280;
        padding: 14px 16px;
        border: none;
      }
      
      td {
        padding: 16px;
        border-color: #f5f5f5;
        font-size: 14px;
      }
      
      tr:last-child td {
        border: none;
      }
    }
    
    /* Alerts */
    .alert-item {
      padding: 16px 20px;
      transition: background 0.2s;
      
      &:hover {
        background: #fafafa;
      }
    }
    
    .alert-icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      margin-right: 14px;
      flex-shrink: 0;
      
      &.danger {
        background: rgba(239, 68, 68, 0.1);
        color: #EF4444;
      }
      
      &.warning {
        background: rgba(234, 179, 8, 0.1);
        color: #EAB308;
      }
    }
    
    .score {
      font-size: 18px;
      font-weight: 700;
    }
    
    /* Quick Actions */
    .quick-action {
      display: block;
      text-align: center;
      padding: 24px 16px;
      border-radius: 12px;
      text-decoration: none;
      color: inherit;
      transition: all 0.3s ease;
      
      &:hover {
        background: #f8fafc;
        transform: translateY(-3px);
        
        .quick-icon {
          transform: scale(1.1);
        }
      }
      
      h6 {
        margin-bottom: 4px;
        color: #1f2937;
      }
      
      small {
        color: #6b7280;
      }
    }
    
    .quick-icon {
      width: 60px;
      height: 60px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      color: white;
      margin: 0 auto 16px;
      transition: transform 0.3s ease;
    }
    
    /* Breadcrumb */
    .breadcrumb {
      font-size: 14px;
      
      .breadcrumb-item {
        color: #6b7280;
        
        &.active {
          color: #4F46E5;
        }
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  authService = inject(AuthService);
  private apiService = inject(ApiService);

  stats = signal<any>(null);

  analyticsCards = [
    { title: 'Total Patients', value: '156', change: '+12%', icon: 'ti ti-trending-up', badgeClass: 'bg-success-subtle text-success', extra: '32', extraText: 'nouveaux ce mois', textClass: 'text-success' },
    { title: 'Séances Aujourd\'hui', value: '8', change: '+5', icon: 'ti ti-arrow-up', badgeClass: 'bg-primary-subtle text-primary', extra: '3', extraText: 'complétées', textClass: 'text-primary' },
    { title: 'Taux de Présence', value: '94.2%', change: '+2.1%', icon: 'ti ti-trending-up', badgeClass: 'bg-success-subtle text-success', extra: '↑', extraText: 'vs mois dernier', textClass: 'text-success' },
    { title: 'Alertes IA', value: '5', change: '-2', icon: 'ti ti-trending-down', badgeClass: 'bg-warning-subtle text-warning', extra: '2', extraText: 'prioritaires', textClass: 'text-warning' }
  ];

  weeklyData = [
    { name: 'Lun', value: 65, count: 8, isToday: false },
    { name: 'Mar', value: 80, count: 10, isToday: false },
    { name: 'Mer', value: 45, count: 6, isToday: false },
    { name: 'Jeu', value: 90, count: 12, isToday: false },
    { name: 'Ven', value: 70, count: 9, isToday: true },
    { name: 'Sam', value: 30, count: 4, isToday: false },
    { name: 'Dim', value: 10, count: 1, isToday: false }
  ];

  sessionTypes = [
    { name: 'Consultation', percent: 45, icon: 'ti ti-user', bgClass: 'bg-primary', barClass: 'bg-primary' },
    { name: 'Thérapie', percent: 30, icon: 'ti ti-brain', bgClass: 'bg-success', barClass: 'bg-success' },
    { name: 'Suivi', percent: 15, icon: 'ti ti-refresh', bgClass: 'bg-warning', barClass: 'bg-warning' },
    { name: 'Vidéo', percent: 10, icon: 'ti ti-video', bgClass: 'bg-info', barClass: 'bg-info' }
  ];

  recentSessions = [
    { id: 1, patient: 'Marie Dupont', initials: 'MD', type: 'Consultation', status: 'Terminée', statusClass: 'bg-success-subtle text-success', statusIcon: 'ti ti-check', date: 'Aujourd\'hui', duration: '45 min', avatarClass: 'bg-primary' },
    { id: 2, patient: 'Jean Martin', initials: 'JM', type: 'Thérapie cognitive', status: 'Terminée', statusClass: 'bg-success-subtle text-success', statusIcon: 'ti ti-check', date: 'Aujourd\'hui', duration: '60 min', avatarClass: 'bg-success' },
    { id: 3, patient: 'Sophie Bernard', initials: 'SB', type: 'Suivi mensuel', status: 'En cours', statusClass: 'bg-warning-subtle text-warning', statusIcon: 'ti ti-clock', date: 'Aujourd\'hui', duration: '30 min', avatarClass: 'bg-warning' },
    { id: 4, patient: 'Pierre Leroy', initials: 'PL', type: 'Consultation', status: 'Planifiée', statusClass: 'bg-secondary-subtle text-secondary', statusIcon: 'ti ti-calendar', date: '15:30', duration: '45 min', avatarClass: 'bg-info' },
    { id: 5, patient: 'Claire Moreau', initials: 'CM', type: 'Vidéo', status: 'Planifiée', statusClass: 'bg-secondary-subtle text-secondary', statusIcon: 'ti ti-calendar', date: '17:00', duration: '30 min', avatarClass: 'bg-danger' }
  ];

  alerts = [
    { id: 1, patient: 'Sophie Bernard', score: 82, level: 'high', message: 'Risque d\'abandon - 3 absences consécutives' },
    { id: 2, patient: 'Thomas Petit', score: 68, level: 'high', message: 'Score anxiété en hausse significative' },
    { id: 3, patient: 'Emma Garcia', score: 54, level: 'medium', message: 'Progression ralentie depuis 4 semaines' }
  ];

  ngOnInit() {
    this.apiService.getDashboardStats().subscribe({
      next: (data) => this.stats.set(data),
      error: () => {}
    });
  }
}
