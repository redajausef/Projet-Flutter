import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-seances-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <!-- Page Header -->
    <div class="page-header mb-4">
      <div class="row align-items-center">
        <div class="col">
          <h4 class="mb-1">Calendrier des Séances</h4>
          <p class="text-muted mb-0">Planification et suivi des rendez-vous</p>
        </div>
        <div class="col-auto">
          <button class="btn btn-primary" (click)="showAddModal = true">
            <i class="ti ti-plus me-2"></i>Nouvelle séance
          </button>
        </div>
      </div>
    </div>

    <div class="row">
      <!-- Calendar -->
      <div class="col-lg-8">
        <div class="card">
          <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
              <div class="d-flex align-items-center gap-2">
                <button class="btn btn-sm btn-outline-secondary" (click)="previousMonth()">
                  <i class="ti ti-chevron-left"></i>
                </button>
                <button class="btn btn-sm btn-outline-secondary" (click)="nextMonth()">
                  <i class="ti ti-chevron-right"></i>
                </button>
                <h5 class="mb-0 ms-2">{{ monthNames[currentMonth] }} {{ currentYear }}</h5>
              </div>
              <button class="btn btn-sm btn-outline-primary" (click)="goToToday()">Aujourd'hui</button>
            </div>
          </div>
          <div class="card-body p-0">
            <!-- Calendar Grid -->
            <div class="calendar-grid">
              <!-- Week Days Header -->
              <div class="calendar-header">
                @for (day of weekDays; track day) {
                  <div class="calendar-cell text-center py-2 fw-semibold text-muted">{{ day }}</div>
                }
              </div>
              <!-- Calendar Days -->
              <div class="calendar-body">
                @for (week of calendarWeeks(); track $index) {
                  <div class="calendar-row">
                    @for (day of week; track day.date) {
                      <div class="calendar-cell" 
                           [class.other-month]="!day.isCurrentMonth"
                           [class.today]="day.isToday"
                           [class.selected]="day.date === selectedDate"
                           (click)="selectDate(day.date)">
                        <div class="day-number">{{ day.day }}</div>
                        @if (day.events.length > 0) {
                          <div class="day-events">
                            @for (event of day.events.slice(0, 2); track event.id) {
                              <div class="event-dot" [class]="'bg-' + event.color"></div>
                            }
                            @if (day.events.length > 2) {
                              <small class="text-muted">+{{ day.events.length - 2 }}</small>
                            }
                          </div>
                        }
                      </div>
                    }
                  </div>
                }
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Day Details / Today's Schedule -->
      <div class="col-lg-4">
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">
              <i class="ti ti-calendar me-2"></i>
              {{ selectedDate === todayString ? "Aujourd'hui" : formatSelectedDate() }}
            </h5>
          </div>
          <div class="card-body p-0">
            @if (selectedDayEvents().length === 0) {
              <div class="text-center py-5">
                <i class="ti ti-calendar-off f-48 text-muted opacity-50"></i>
                <p class="text-muted mt-3 mb-0">Aucune séance prévue</p>
              </div>
            } @else {
              <div class="list-group list-group-flush">
                @for (event of selectedDayEvents(); track event.id) {
                  <div class="list-group-item">
                    <div class="d-flex align-items-start">
                      <div class="flex-shrink-0 me-3">
                        <div class="avatar avatar-s rounded" [class]="'bg-' + event.color">
                          <i class="ti ti-clock text-white"></i>
                        </div>
                      </div>
                      <div class="flex-grow-1">
                        <div class="d-flex justify-content-between">
                          <h6 class="mb-1">{{ event.patient }}</h6>
                          <small class="text-muted">{{ event.time }}</small>
                        </div>
                        <p class="mb-1 small text-muted">{{ event.type }}</p>
                        <span class="badge" [class]="getStatusClass(event.status)">
                          {{ event.status }}
                        </span>
                      </div>
                    </div>
                  </div>
                }
              </div>
            }
          </div>
        </div>

        <!-- Statistics Card -->
        <div class="card mt-4">
          <div class="card-header">
            <h5 class="mb-0">Statistiques du mois</h5>
          </div>
          <div class="card-body">
            <div class="row g-3">
              <div class="col-6">
                <div class="text-center p-3 bg-primary-subtle rounded">
                  <h3 class="mb-1 text-primary">24</h3>
                  <small class="text-muted">Séances prévues</small>
                </div>
              </div>
              <div class="col-6">
                <div class="text-center p-3 bg-success-subtle rounded">
                  <h3 class="mb-1 text-success">18</h3>
                  <small class="text-muted">Complétées</small>
                </div>
              </div>
              <div class="col-6">
                <div class="text-center p-3 bg-warning-subtle rounded">
                  <h3 class="mb-1 text-warning">4</h3>
                  <small class="text-muted">En attente</small>
                </div>
              </div>
              <div class="col-6">
                <div class="text-center p-3 bg-danger-subtle rounded">
                  <h3 class="mb-1 text-danger">2</h3>
                  <small class="text-muted">Annulées</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Session Modal -->
    @if (showAddModal) {
      <div class="modal fade show d-block" style="background: rgba(0,0,0,0.5)">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Nouvelle Séance</h5>
              <button type="button" class="btn-close" (click)="showAddModal = false"></button>
            </div>
            <div class="modal-body">
              <form>
                <div class="mb-3">
                  <label class="form-label">Patient</label>
                  <select class="form-select">
                    <option>Sélectionner un patient</option>
                    <option>Marie Dupont</option>
                    <option>Jean Martin</option>
                    <option>Sophie Bernard</option>
                  </select>
                </div>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label">Date</label>
                    <input type="date" class="form-control" [value]="selectedDate">
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Heure</label>
                    <input type="time" class="form-control" value="09:00">
                  </div>
                </div>
                <div class="mb-3 mt-3">
                  <label class="form-label">Type de séance</label>
                  <select class="form-select">
                    <option>Consultation</option>
                    <option>Thérapie cognitive</option>
                    <option>Suivi mensuel</option>
                    <option>Vidéoconférence</option>
                  </select>
                </div>
                <div class="mb-3">
                  <label class="form-label">Durée</label>
                  <select class="form-select">
                    <option>30 minutes</option>
                    <option selected>45 minutes</option>
                    <option>60 minutes</option>
                    <option>90 minutes</option>
                  </select>
                </div>
                <div class="mb-3">
                  <label class="form-label">Notes</label>
                  <textarea class="form-control" rows="3" placeholder="Notes optionnelles..."></textarea>
                </div>
              </form>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showAddModal = false">Annuler</button>
              <button type="button" class="btn btn-primary" (click)="saveSession()">
                <i class="ti ti-check me-2"></i>Planifier
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .calendar-grid {
      border: 1px solid #e9ecef;
    }
    
    .calendar-header {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      background: #f8f9fa;
      border-bottom: 1px solid #e9ecef;
    }
    
    .calendar-row {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
    }
    
    .calendar-cell {
      min-height: 80px;
      padding: 8px;
      border-right: 1px solid #e9ecef;
      border-bottom: 1px solid #e9ecef;
      cursor: pointer;
      transition: background 0.2s;
      
      &:nth-child(7n) {
        border-right: none;
      }
      
      &:hover {
        background: #f8f9fa;
      }
      
      &.other-month {
        background: #fafafa;
        .day-number { color: #ccc; }
      }
      
      &.today {
        background: rgba(79, 70, 229, 0.1);
        .day-number {
          background: #4F46E5;
          color: white;
          border-radius: 50%;
          width: 28px;
          height: 28px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      
      &.selected {
        background: rgba(79, 70, 229, 0.05);
        box-shadow: inset 0 0 0 2px #4F46E5;
      }
    }
    
    .day-number {
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .day-events {
      display: flex;
      gap: 4px;
      flex-wrap: wrap;
      align-items: center;
    }
    
    .event-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
    }
    
    .avatar {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .f-48 { font-size: 48px; }
  `]
})
export class SeancesCalendarComponent implements OnInit {
  weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  monthNames = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
  
  currentMonth = new Date().getMonth();
  currentYear = new Date().getFullYear();
  selectedDate = '';
  todayString = '';
  showAddModal = false;
  
  calendarWeeks = signal<any[][]>([]);
  selectedDayEvents = signal<any[]>([]);

  // Demo events
  private events = [
    { id: 1, date: '2025-12-15', time: '09:00', patient: 'Marie Dupont', type: 'Consultation', status: 'Confirmé', color: 'primary' },
    { id: 2, date: '2025-12-15', time: '10:30', patient: 'Jean Martin', type: 'Thérapie cognitive', status: 'Confirmé', color: 'success' },
    { id: 3, date: '2025-12-15', time: '14:00', patient: 'Sophie Bernard', type: 'Suivi mensuel', status: 'En attente', color: 'warning' },
    { id: 4, date: '2025-12-16', time: '09:30', patient: 'Pierre Leroy', type: 'Consultation', status: 'Confirmé', color: 'primary' },
    { id: 5, date: '2025-12-17', time: '11:00', patient: 'Claire Moreau', type: 'Vidéoconférence', status: 'Confirmé', color: 'info' },
    { id: 6, date: '2025-12-18', time: '15:00', patient: 'Marie Dupont', type: 'Suivi', status: 'Confirmé', color: 'success' },
    { id: 7, date: '2025-12-19', time: '10:00', patient: 'Jean Martin', type: 'Thérapie', status: 'En attente', color: 'warning' },
    { id: 8, date: '2025-12-20', time: '09:00', patient: 'Sophie Bernard', type: 'Consultation', status: 'Confirmé', color: 'primary' },
  ];

  ngOnInit() {
    const today = new Date();
    this.todayString = this.formatDate(today);
    this.selectedDate = this.todayString;
    this.generateCalendar();
    this.updateSelectedDayEvents();
  }

  generateCalendar() {
    const firstDay = new Date(this.currentYear, this.currentMonth, 1);
    const lastDay = new Date(this.currentYear, this.currentMonth + 1, 0);
    
    // Adjust for Monday start (0 = Monday, 6 = Sunday)
    let startDay = firstDay.getDay() - 1;
    if (startDay < 0) startDay = 6;
    
    const weeks: any[][] = [];
    let currentWeek: any[] = [];
    
    // Previous month days
    const prevMonthLastDay = new Date(this.currentYear, this.currentMonth, 0).getDate();
    for (let i = startDay - 1; i >= 0; i--) {
      const date = new Date(this.currentYear, this.currentMonth - 1, prevMonthLastDay - i);
      currentWeek.push(this.createDayObject(date, false));
    }
    
    // Current month days
    for (let day = 1; day <= lastDay.getDate(); day++) {
      const date = new Date(this.currentYear, this.currentMonth, day);
      currentWeek.push(this.createDayObject(date, true));
      
      if (currentWeek.length === 7) {
        weeks.push(currentWeek);
        currentWeek = [];
      }
    }
    
    // Next month days
    if (currentWeek.length > 0) {
      let nextMonthDay = 1;
      while (currentWeek.length < 7) {
        const date = new Date(this.currentYear, this.currentMonth + 1, nextMonthDay++);
        currentWeek.push(this.createDayObject(date, false));
      }
      weeks.push(currentWeek);
    }
    
    this.calendarWeeks.set(weeks);
  }

  createDayObject(date: Date, isCurrentMonth: boolean) {
    const dateStr = this.formatDate(date);
    const dayEvents = this.events.filter(e => e.date === dateStr);
    
    return {
      date: dateStr,
      day: date.getDate(),
      isCurrentMonth,
      isToday: dateStr === this.todayString,
      events: dayEvents
    };
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  selectDate(date: string) {
    this.selectedDate = date;
    this.updateSelectedDayEvents();
  }

  updateSelectedDayEvents() {
    const events = this.events.filter(e => e.date === this.selectedDate);
    this.selectedDayEvents.set(events);
  }

  previousMonth() {
    this.currentMonth--;
    if (this.currentMonth < 0) {
      this.currentMonth = 11;
      this.currentYear--;
    }
    this.generateCalendar();
  }

  nextMonth() {
    this.currentMonth++;
    if (this.currentMonth > 11) {
      this.currentMonth = 0;
      this.currentYear++;
    }
    this.generateCalendar();
  }

  goToToday() {
    const today = new Date();
    this.currentMonth = today.getMonth();
    this.currentYear = today.getFullYear();
    this.selectedDate = this.todayString;
    this.generateCalendar();
    this.updateSelectedDayEvents();
  }

  formatSelectedDate(): string {
    const date = new Date(this.selectedDate);
    return date.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Confirmé': return 'bg-success-subtle text-success';
      case 'En attente': return 'bg-warning-subtle text-warning';
      case 'Annulé': return 'bg-danger-subtle text-danger';
      default: return 'bg-secondary-subtle text-secondary';
    }
  }

  saveSession() {
    this.showAddModal = false;
  }
}
