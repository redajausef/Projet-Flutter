import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { DashboardService } from '../../core/services/dashboard.service';
import { SeanceService } from '../../core/services/seance.service';
import { PatientService } from '../../core/services/patient.service';
import { AuthService } from '../../core/services/auth.service';
import { DashboardStats, Seance, Patient, ChartDataPoint } from '../../core/models';
import { PendingApprovalsComponent } from '../seances/pending-approvals/pending-approvals.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, PendingApprovalsComponent],
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
      @for (stat of analyticsCards(); track stat.title) {
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
                @for (day of weeklyData(); track day.name) {
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
            @for (type of sessionTypes(); track type.name; let last = $last) {
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

    <!-- Pending Approvals Section -->
    <div class="row g-4 mb-4">
      <div class="col-12">
        <app-pending-approvals></app-pending-approvals>
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
                @for (session of recentSessions(); track session.id) {
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
            <span class="badge bg-danger rounded-pill px-3">{{ alerts().length }}</span>
          </div>
          <div class="card-body p-0">
            @for (alert of alerts(); track alert.id; let last = $last) {
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
export class DashboardComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  private dashboardService = inject(DashboardService);
  private seanceService = inject(SeanceService);
  private patientService = inject(PatientService);
  private destroy$ = new Subject<void>();

  // State
  loading = signal(true);
  stats = signal<DashboardStats | null>(null);
  recentSeances = signal<Seance[]>([]);
  highRiskPatients = signal<Patient[]>([]);
  viewMode = signal<'week' | 'month'>('week');

  // Computed analytics cards
  analyticsCards = computed(() => {
    const s = this.stats();
    if (!s) return this.getDefaultCards();

    return [
      {
        title: 'Total Patients',
        value: s.totalPatients.toString(),
        change: `+${s.patientGrowthPercentage?.toFixed(0) || 12}%`,
        icon: 'ti ti-trending-up',
        badgeClass: 'bg-success-subtle text-success',
        extra: s.newPatientsThisMonth?.toString() || '32',
        extraText: 'nouveaux ce mois',
        textClass: 'text-success'
      },
      {
        title: 'Séances Aujourd\'hui',
        value: s.todaySeances?.toString() || '8',
        change: `+${s.upcomingSeances || 5}`,
        icon: 'ti ti-arrow-up',
        badgeClass: 'bg-primary-subtle text-primary',
        extra: s.completedSeancesThisMonth?.toString() || '3',
        extraText: 'complétées ce mois',
        textClass: 'text-primary'
      },
      {
        title: 'Taux de Présence',
        value: `${s.seanceCompletionRate?.toFixed(1) || 94.2}%`,
        change: '+2.1%',
        icon: 'ti ti-trending-up',
        badgeClass: 'bg-success-subtle text-success',
        extra: '↑',
        extraText: 'vs mois dernier',
        textClass: 'text-success'
      },
      {
        title: 'Alertes IA',
        value: s.highRiskPatients?.toString() || '5',
        change: '-2',
        icon: 'ti ti-trending-down',
        badgeClass: 'bg-warning-subtle text-warning',
        extra: '2',
        extraText: 'prioritaires',
        textClass: 'text-warning'
      }
    ];
  });

  weeklyData = computed(() => {
    const s = this.stats();
    const trend = s?.seancesTrend || [];
    const dayNames = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
    const today = new Date().getDay();
    const todayIndex = today === 0 ? 6 : today - 1; // Convert Sunday=0 to index 6

    if (trend.length > 0) {
      const maxValue = Math.max(...trend.map(t => t.value));
      return trend.map((t, i) => ({
        name: t.label,
        value: maxValue > 0 ? (t.value / maxValue) * 100 : 0,
        count: t.value,
        isToday: i === trend.length - 1  // Last element is always today
      }));
    }

    // No data available
    return [
      { name: 'Lun', value: 0, count: 0, isToday: todayIndex === 0 },
      { name: 'Mar', value: 0, count: 0, isToday: todayIndex === 1 },
      { name: 'Mer', value: 0, count: 0, isToday: todayIndex === 2 },
      { name: 'Jeu', value: 0, count: 0, isToday: todayIndex === 3 },
      { name: 'Ven', value: 0, count: 0, isToday: todayIndex === 4 },
      { name: 'Sam', value: 0, count: 0, isToday: todayIndex === 5 },
      { name: 'Dim', value: 0, count: 0, isToday: todayIndex === 6 }
    ];
  });

  sessionTypes = computed(() => {
    const s = this.stats();
    if (s?.seancesByType) {
      const total = Object.values(s.seancesByType).reduce((a, b) => a + b, 0);
      const types = [
        { key: 'IN_PERSON', name: 'En personne', icon: 'ti ti-user', bgClass: 'bg-primary', barClass: 'bg-primary' },
        { key: 'VIDEO_CALL', name: 'Vidéo', icon: 'ti ti-video', bgClass: 'bg-info', barClass: 'bg-info' },
        { key: 'PHONE', name: 'Téléphone', icon: 'ti ti-phone', bgClass: 'bg-success', barClass: 'bg-success' },
        { key: 'HOME_VISIT', name: 'Visite domicile', icon: 'ti ti-home', bgClass: 'bg-warning', barClass: 'bg-warning' }
      ];

      return types.map(t => ({
        ...t,
        percent: total > 0 ? Math.round((s.seancesByType[t.key] || 0) / total * 100) : 0
      }));
    }

    return [
      { name: 'En personne', percent: 0, icon: 'ti ti-user', bgClass: 'bg-primary', barClass: 'bg-primary' },
      { name: 'Vidéo', percent: 0, icon: 'ti ti-video', bgClass: 'bg-info', barClass: 'bg-info' },
      { name: 'Téléphone', percent: 0, icon: 'ti ti-phone', bgClass: 'bg-success', barClass: 'bg-success' },
      { name: 'Visite domicile', percent: 0, icon: 'ti ti-home', bgClass: 'bg-warning', barClass: 'bg-warning' }
    ];
  });

  recentSessions = computed(() => {
    const seances = this.recentSeances();
    const colors = ['bg-primary', 'bg-success', 'bg-warning', 'bg-info', 'bg-danger'];

    return seances.map((s, i) => ({
      id: s.id,
      patient: s.patientName,
      initials: this.getInitials(s.patientName),
      type: this.getSeanceTypeLabel(s.type),
      status: this.getStatusLabel(s.status),
      statusClass: this.getStatusClass(s.status),
      statusIcon: this.getStatusIcon(s.status),
      date: this.formatDate(s.scheduledAt),
      duration: `${s.durationMinutes} min`,
      avatarClass: colors[i % colors.length]
    }));
  });

  alerts = computed(() => {
    const patients = this.highRiskPatients();
    return patients.slice(0, 5).map((p, i) => ({
      id: p.id,
      patient: p.fullName || `${p.firstName} ${p.lastName}`,
      score: p.riskScore || 0,
      level: (p.riskScore || 0) >= 70 ? 'high' : 'medium',
      message: this.getRiskMessage(p)
    }));
  });

  ngOnInit() {
    this.loadDashboardData();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDashboardData() {
    this.loading.set(true);

    forkJoin({
      stats: this.dashboardService.getDashboardStats(),
      seances: this.seanceService.getTodaySeances(),
      highRisk: this.patientService.getHighRiskPatients(50)
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.stats.set(data.stats);
        this.recentSeances.set(data.seances);
        this.highRiskPatients.set(data.highRisk);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  refreshDashboard() {
    this.loadDashboardData();
  }

  setViewMode(mode: 'week' | 'month') {
    this.viewMode.set(mode);
  }

  private getDefaultCards() {
    return [
      { title: 'Total Patients', value: '0', change: '0%', icon: 'ti ti-trending-up', badgeClass: 'bg-secondary-subtle text-secondary', extra: '0', extraText: 'nouveaux ce mois', textClass: 'text-secondary' },
      { title: 'Séances Aujourd\'hui', value: '0', change: '0', icon: 'ti ti-minus', badgeClass: 'bg-secondary-subtle text-secondary', extra: '0', extraText: 'complétées', textClass: 'text-secondary' },
      { title: 'Taux de Présence', value: '0%', change: '0%', icon: 'ti ti-minus', badgeClass: 'bg-secondary-subtle text-secondary', extra: '-', extraText: 'vs mois dernier', textClass: 'text-secondary' },
      { title: 'Alertes IA', value: '0', change: '0', icon: 'ti ti-minus', badgeClass: 'bg-secondary-subtle text-secondary', extra: '0', extraText: 'prioritaires', textClass: 'text-secondary' }
    ];
  }

  private getInitials(name: string): string {
    if (!name) return 'XX';
    const parts = name.split(' ');
    return parts.map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase();
  }

  private getSeanceTypeLabel(type: Seance['type']): string {
    const labels: Record<string, string> = {
      'CONSULTATION': 'Consultation',
      'THERAPY': 'Thérapie',
      'FOLLOW_UP': 'Suivi',
      'VIDEO': 'Vidéo',
      'EMERGENCY': 'Urgence'
    };
    return labels[type] || type;
  }

  private getStatusLabel(status: Seance['status']): string {
    const labels: Record<string, string> = {
      'SCHEDULED': 'Planifiée',
      'CONFIRMED': 'Confirmée',
      'IN_PROGRESS': 'En cours',
      'COMPLETED': 'Terminée',
      'CANCELLED': 'Annulée',
      'NO_SHOW': 'Absent'
    };
    return labels[status] || status;
  }

  private getStatusClass(status: Seance['status']): string {
    const classes: Record<string, string> = {
      'SCHEDULED': 'bg-secondary-subtle text-secondary',
      'CONFIRMED': 'bg-info-subtle text-info',
      'IN_PROGRESS': 'bg-warning-subtle text-warning',
      'COMPLETED': 'bg-success-subtle text-success',
      'CANCELLED': 'bg-danger-subtle text-danger',
      'NO_SHOW': 'bg-danger-subtle text-danger'
    };
    return classes[status] || 'bg-secondary-subtle text-secondary';
  }

  private getStatusIcon(status: Seance['status']): string {
    const icons: Record<string, string> = {
      'SCHEDULED': 'ti ti-calendar',
      'CONFIRMED': 'ti ti-calendar-check',
      'IN_PROGRESS': 'ti ti-clock',
      'COMPLETED': 'ti ti-check',
      'CANCELLED': 'ti ti-x',
      'NO_SHOW': 'ti ti-user-off'
    };
    return icons[status] || 'ti ti-calendar';
  }

  private formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    const today = new Date();

    if (date.toDateString() === today.toDateString()) {
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    }

    return date.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' });
  }

  private getRiskMessage(patient: Patient): string {
    const risk = patient.riskScore || 0;
    if (risk >= 80) return 'Risque critique - Intervention recommandée';
    if (risk >= 70) return 'Risque d\'abandon élevé';
    if (risk >= 50) return 'Progression ralentie';
    return 'À surveiller';
  }
}
