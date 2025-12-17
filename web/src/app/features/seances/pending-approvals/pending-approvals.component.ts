import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SeanceService } from '../../../core/services/seance.service';
import { Seance } from '../../../core/models';

@Component({
  selector: 'app-pending-approvals',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <div class="card-header">
        <div class="d-flex align-items-center justify-content-between">
          <div>
            <h5 class="mb-1">Demandes de rendez-vous en attente</h5>
            <p class="text-muted small mb-0">{{ pendingSeances.length }} demande(s) à traiter</p>
          </div>
          <span class="badge bg-warning">
            <i class="ti ti-clock me-1"></i>
            En attente
          </span>
        </div>
      </div>
      <div class="card-body p-0">
        @if (loading) {
          <div class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Chargement...</span>
            </div>
          </div>
        } @else if (pendingSeances.length === 0) {
          <div class="text-center py-5">
            <i class="ti ti-check-circle text-success" style="font-size: 48px;"></i>
            <p class="text-muted mt-3 mb-0">Aucune demande en attente</p>
          </div>
        } @else {
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="bg-light">
                <tr>
                  <th>Patient</th>
                  <th>Date & Heure</th>
                  <th>Type</th>
                  <th>Durée</th>
                  <th>Notes</th>
                  <th class="text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (seance of pendingSeances; track seance.id) {
                  <tr>
                    <td>
                      <div class="d-flex align-items-center">
                        <div class="avatar avatar-sm me-2">
                          @if (seance.patientImageUrl) {
                            <img [src]="seance.patientImageUrl" alt="" class="rounded-circle">
                          } @else {
                            <div class="avatar-title bg-primary rounded-circle">
                              {{ getInitials(seance.patientName) }}
                            </div>
                          }
                        </div>
                        <div>
                          <div class="fw-medium">{{ seance.patientName }}</div>
                          <small class="text-muted">{{ seance.patientCode }}</small>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div>
                        <i class="ti ti-calendar text-primary me-1"></i>
                        <span>{{ formatDate(seance.scheduledAt) }}</span>
                      </div>
                      <div class="text-muted small">
                        <i class="ti ti-clock text-primary me-1"></i>
                        <span>{{ formatTime(seance.scheduledAt) }}</span>
                      </div>
                    </td>
                    <td>
                      <span class="badge" [class]="getTypeBadgeClass(seance.type)">
                        {{ getTypeLabel(seance.type) }}
                      </span>
                    </td>
                    <td>{{ seance.durationMinutes }} min</td>
                    <td>
                      @if (seance.notes) {
                        <span class="text-truncate d-inline-block" style="max-width: 200px;" [title]="seance.notes">
                          {{ seance.notes }}
                        </span>
                      } @else {
                        <span class="text-muted">-</span>
                      }
                    </td>
                    <td class="text-end">
                      <div class="btn-group">
                        <button 
                          class="btn btn-sm btn-success"
                          (click)="approveSeance(seance.id)"
                          [disabled]="processingIds.has(seance.id)">
                          @if (processingIds.has(seance.id)) {
                            <span class="spinner-border spinner-border-sm me-1"></span>
                          } @else {
                            <i class="ti ti-check me-1"></i>
                          }
                          Approuver
                        </button>
                        <button 
                          class="btn btn-sm btn-danger"
                          (click)="rejectSeance(seance.id)"
                          [disabled]="processingIds.has(seance.id)">
                          @if (processingIds.has(seance.id)) {
                            <span class="spinner-border spinner-border-sm me-1"></span>
                          } @else {
                            <i class="ti ti-x me-1"></i>
                          }
                          Refuser
                        </button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .avatar {
      width: 40px;
      height: 40px;
    }
    .avatar-sm {
      width: 36px;
      height: 36px;
    }
    .avatar-title {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      font-weight: 600;
      color: white;
    }
  `]
})
export class PendingApprovalsComponent implements OnInit {
  private seanceService = inject(SeanceService);
  
  pendingSeances: Seance[] = [];
  loading = false;
  processingIds = new Set<number>();

  ngOnInit() {
    this.loadPendingSeances();
  }

  loadPendingSeances() {
    this.loading = true;
    // Get all seances and filter by PENDING_APPROVAL status
    this.seanceService.getSeances(0, 100).subscribe({
      next: (response) => {
        this.pendingSeances = response.content.filter(s => s.status === 'PENDING_APPROVAL');
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading pending seances:', error);
        this.loading = false;
      }
    });
  }

  approveSeance(id: number) {
    this.processingIds.add(id);
    this.seanceService.updateSeanceStatus(id, 'CONFIRMED').subscribe({
      next: () => {
        this.processingIds.delete(id);
        // Remove from list
        this.pendingSeances = this.pendingSeances.filter(s => s.id !== id);
        // Show success message (you can add a toast service here)
        console.log('Séance approuvée avec succès');
      },
      error: (error) => {
        this.processingIds.delete(id);
        console.error('Error approving seance:', error);
      }
    });
  }

  rejectSeance(id: number) {
    this.processingIds.add(id);
    this.seanceService.updateSeanceStatus(id, 'CANCELLED').subscribe({
      next: () => {
        this.processingIds.delete(id);
        // Remove from list
        this.pendingSeances = this.pendingSeances.filter(s => s.id !== id);
        // Show success message
        console.log('Séance refusée');
      },
      error: (error) => {
        this.processingIds.delete(id);
        console.error('Error rejecting seance:', error);
      }
    });
  }

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      'IN_PERSON': 'En personne',
      'VIDEO_CALL': 'Vidéo',
      'PHONE_CALL': 'Téléphone',
      'HOME_VISIT': 'À domicile',
      'GROUP_SESSION': 'Groupe'
    };
    return labels[type] || type;
  }

  getTypeBadgeClass(type: string): string {
    const classes: Record<string, string> = {
      'IN_PERSON': 'bg-primary',
      'VIDEO_CALL': 'bg-info',
      'PHONE_CALL': 'bg-warning',
      'HOME_VISIT': 'bg-success',
      'GROUP_SESSION': 'bg-secondary'
    };
    return classes[type] || 'bg-secondary';
  }
}
