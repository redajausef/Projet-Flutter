import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-seances-calendar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6 animate-fade-in">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-text-primary mb-2">Calendrier des Séances</h1>
          <p class="text-text-secondary">Planifiez et gérez les rendez-vous</p>
        </div>
        <button class="btn-accent flex items-center gap-2">
          <span class="material-icons">add</span>
          Nouvelle Séance
        </button>
      </div>

      <!-- Calendar View Toggle -->
      <div class="card">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-4">
            <button (click)="previousMonth()" class="p-2 rounded-lg hover:bg-surface-light">
              <span class="material-icons">chevron_left</span>
            </button>
            <h2 class="text-xl font-semibold text-text-primary">{{ getCurrentMonthName() }}</h2>
            <button (click)="nextMonth()" class="p-2 rounded-lg hover:bg-surface-light">
              <span class="material-icons">chevron_right</span>
            </button>
          </div>
          <div class="flex items-center gap-2">
            <button [class.bg-primary]="viewMode === 'month'"
                    [class.text-white]="viewMode === 'month'"
                    (click)="viewMode = 'month'"
                    class="px-4 py-2 rounded-lg text-sm font-medium hover:bg-surface-light">
              Mois
            </button>
            <button [class.bg-primary]="viewMode === 'week'"
                    [class.text-white]="viewMode === 'week'"
                    (click)="viewMode = 'week'"
                    class="px-4 py-2 rounded-lg text-sm font-medium hover:bg-surface-light">
              Semaine
            </button>
            <button [class.bg-primary]="viewMode === 'day'"
                    [class.text-white]="viewMode === 'day'"
                    (click)="viewMode = 'day'"
                    class="px-4 py-2 rounded-lg text-sm font-medium hover:bg-surface-light">
              Jour
            </button>
          </div>
        </div>

        <!-- Calendar Grid -->
        <div class="grid grid-cols-7 gap-1">
          <!-- Week days header -->
          <div *ngFor="let day of weekDays" 
               class="p-3 text-center text-sm font-medium text-text-muted">
            {{ day }}
          </div>
          
          <!-- Calendar days -->
          <div *ngFor="let day of calendarDays()"
               class="min-h-[100px] p-2 rounded-lg transition-colors cursor-pointer"
               [class.bg-surface-light]="!day.isCurrentMonth"
               [class.hover:bg-surface-light]="day.isCurrentMonth"
               [class.bg-primary/10]="day.isToday"
               [class.border-2]="day.isToday"
               [class.border-primary]="day.isToday">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm font-medium"
                    [class.text-text-primary]="day.isCurrentMonth"
                    [class.text-text-muted]="!day.isCurrentMonth"
                    [class.text-accent]="day.isToday">
                {{ day.date }}
              </span>
              <span *ngIf="day.seances.length > 0" 
                    class="w-5 h-5 rounded-full bg-accent text-background text-xs flex items-center justify-center">
                {{ day.seances.length }}
              </span>
            </div>
            
            <!-- Seances preview -->
            <div class="space-y-1">
              <div *ngFor="let seance of day.seances.slice(0, 2)"
                   class="text-xs p-1 rounded truncate"
                   [class.bg-info/20]="seance.type === 'VIDEO_CALL'"
                   [class.text-info]="seance.type === 'VIDEO_CALL'"
                   [class.bg-success/20]="seance.type !== 'VIDEO_CALL'"
                   [class.text-success]="seance.type !== 'VIDEO_CALL'">
                {{ formatTime(seance.scheduledAt) }} - {{ seance.patientName }}
              </div>
              <div *ngIf="day.seances.length > 2" class="text-xs text-text-muted">
                +{{ day.seances.length - 2 }} autres
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Upcoming Sessions -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="card">
          <h3 class="text-lg font-semibold text-text-primary mb-4">Séances du jour</h3>
          <div class="space-y-3">
            <div *ngFor="let seance of todaySeances()"
                 class="flex items-center gap-4 p-4 bg-surface-light rounded-xl">
              <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-primary to-primary-light 
                          flex items-center justify-center text-white font-bold">
                {{ seance.patientName?.charAt(0) }}
              </div>
              <div class="flex-1">
                <p class="font-medium text-text-primary">{{ seance.patientName }}</p>
                <p class="text-sm text-text-muted">{{ formatTime(seance.scheduledAt) }}</p>
              </div>
              <span class="badge-info" *ngIf="seance.type === 'VIDEO_CALL'">Vidéo</span>
              <span class="badge-success" *ngIf="seance.type !== 'VIDEO_CALL'">Présentiel</span>
            </div>
            <div *ngIf="todaySeances().length === 0" class="text-center py-8 text-text-muted">
              <span class="material-icons text-4xl mb-2">event_available</span>
              <p>Aucune séance aujourd'hui</p>
            </div>
          </div>
        </div>

        <div class="card">
          <h3 class="text-lg font-semibold text-text-primary mb-4">Cette semaine</h3>
          <div class="space-y-3">
            <div *ngFor="let seance of upcomingSeances().slice(0, 5)"
                 class="flex items-center gap-4 p-4 bg-surface-light rounded-xl">
              <div class="text-center">
                <p class="text-xs text-text-muted">{{ getDayName(seance.scheduledAt) }}</p>
                <p class="text-lg font-bold text-accent">{{ getDay(seance.scheduledAt) }}</p>
              </div>
              <div class="flex-1">
                <p class="font-medium text-text-primary">{{ seance.patientName }}</p>
                <p class="text-sm text-text-muted">{{ formatTime(seance.scheduledAt) }} - {{ seance.therapeuteName }}</p>
              </div>
              <button class="p-2 rounded-lg hover:bg-surface transition-colors">
                <span class="material-icons text-text-muted">more_vert</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class SeancesCalendarComponent implements OnInit {
  private apiService = inject(ApiService);

  viewMode: 'month' | 'week' | 'day' = 'month';
  currentDate = new Date();
  weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  
  allSeances = signal<any[]>([]);
  calendarDays = signal<any[]>([]);
  todaySeances = signal<any[]>([]);
  upcomingSeances = signal<any[]>([]);

  ngOnInit() {
    this.loadSeances();
    this.generateCalendarDays();
  }

  loadSeances() {
    this.apiService.getUpcomingSeances().subscribe({
      next: (data) => {
        this.upcomingSeances.set(data);
        this.filterTodaySeances(data);
      }
    });

    const start = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), 1);
    const end = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 0);
    
    this.apiService.getSeancesByDateRange(start.toISOString(), end.toISOString()).subscribe({
      next: (data) => {
        this.allSeances.set(data);
        this.generateCalendarDays();
      }
    });
  }

  filterTodaySeances(seances: any[]) {
    const today = new Date();
    this.todaySeances.set(seances.filter(s => {
      const seanceDate = new Date(s.scheduledAt);
      return seanceDate.toDateString() === today.toDateString();
    }));
  }

  generateCalendarDays() {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const today = new Date();
    
    const days: any[] = [];
    
    // Previous month days
    const firstDayOfWeek = (firstDay.getDay() + 6) % 7; // Monday = 0
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      const date = new Date(year, month, -i);
      days.push({
        date: date.getDate(),
        isCurrentMonth: false,
        isToday: false,
        seances: []
      });
    }
    
    // Current month days
    for (let i = 1; i <= lastDay.getDate(); i++) {
      const date = new Date(year, month, i);
      const daySeances = this.allSeances().filter(s => {
        const seanceDate = new Date(s.scheduledAt);
        return seanceDate.getDate() === i && 
               seanceDate.getMonth() === month &&
               seanceDate.getFullYear() === year;
      });
      
      days.push({
        date: i,
        isCurrentMonth: true,
        isToday: date.toDateString() === today.toDateString(),
        seances: daySeances
      });
    }
    
    // Next month days
    const remainingDays = 42 - days.length;
    for (let i = 1; i <= remainingDays; i++) {
      days.push({
        date: i,
        isCurrentMonth: false,
        isToday: false,
        seances: []
      });
    }
    
    this.calendarDays.set(days);
  }

  previousMonth() {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1);
    this.loadSeances();
  }

  nextMonth() {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1);
    this.loadSeances();
  }

  getCurrentMonthName(): string {
    return this.currentDate.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }

  formatTime(dateStr: string): string {
    return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getDayName(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('fr-FR', { weekday: 'short' });
  }

  getDay(dateStr: string): number {
    return new Date(dateStr).getDate();
  }
}

