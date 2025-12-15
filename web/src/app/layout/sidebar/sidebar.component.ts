import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterLinkActive } from '@angular/router';

interface NavItem {
  icon: string;
  label: string;
  route: string;
  badge?: number;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLinkActive],
  template: `
    <aside class="fixed left-0 top-0 h-full bg-surface border-r border-surface-light z-50 transition-all duration-300"
           [class.w-64]="!collapsed"
           [class.w-20]="collapsed">
      
      <!-- Logo -->
      <div class="h-16 flex items-center px-4 border-b border-surface-light">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-primary to-primary-light flex items-center justify-center shadow-glow">
            <span class="material-icons text-white text-xl">medical_services</span>
          </div>
          <span *ngIf="!collapsed" class="text-xl font-bold gradient-text">ClinAssist</span>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="p-3 space-y-1">
        <a *ngFor="let item of navItems"
           [routerLink]="item.route"
           routerLinkActive="active"
           class="nav-item group"
           [class.justify-center]="collapsed">
          <span class="material-icons text-xl">{{ item.icon }}</span>
          <span *ngIf="!collapsed" class="font-medium">{{ item.label }}</span>
          <span *ngIf="!collapsed && item.badge" 
                class="ml-auto px-2 py-0.5 text-xs font-semibold rounded-full bg-accent text-background">
            {{ item.badge }}
          </span>
        </a>
      </nav>

      <!-- Bottom Section -->
      <div class="absolute bottom-0 left-0 right-0 p-3 border-t border-surface-light">
        <button (click)="toggleCollapse.emit()"
                class="nav-item w-full justify-center hover:bg-primary/20">
          <span class="material-icons transition-transform duration-300"
                [class.rotate-180]="collapsed">
            chevron_left
          </span>
        </button>
      </div>
    </aside>
  `,
  styles: [`
    .nav-item {
      @apply flex items-center gap-3 px-4 py-3 rounded-xl text-text-secondary 
             hover:bg-surface-light hover:text-text-primary transition-all duration-200;
      
      &.active {
        @apply bg-primary/20 text-accent;
        
        .material-icons {
          @apply text-accent;
        }
      }
    }
  `]
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Output() toggleCollapse = new EventEmitter<void>();

  navItems: NavItem[] = [
    { icon: 'dashboard', label: 'Tableau de bord', route: '/dashboard' },
    { icon: 'people', label: 'Patients', route: '/patients', badge: 5 },
    { icon: 'event', label: 'Séances', route: '/seances' },
    { icon: 'psychology', label: 'Thérapeutes', route: '/therapeutes' },
    { icon: 'insights', label: 'Prédictions', route: '/predictions', badge: 3 },
    { icon: 'settings', label: 'Paramètres', route: '/settings' },
  ];
}

