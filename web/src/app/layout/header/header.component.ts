import { Component, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="h-16 bg-surface border-b border-surface-light flex items-center justify-between px-6">
      <!-- Left Section -->
      <div class="flex items-center gap-4">
        <button (click)="toggleSidebar.emit()" 
                class="p-2 rounded-lg hover:bg-surface-light transition-colors">
          <span class="material-icons text-text-secondary">menu</span>
        </button>
        
        <!-- Search -->
        <div class="relative">
          <span class="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-text-muted">search</span>
          <input type="text" 
                 placeholder="Rechercher patients, séances..."
                 class="w-80 pl-10 pr-4 py-2 bg-surface-light rounded-xl text-text-primary 
                        placeholder-text-muted border border-transparent focus:border-primary 
                        focus:outline-none transition-colors">
        </div>
      </div>

      <!-- Right Section -->
      <div class="flex items-center gap-4">
        <!-- Notifications -->
        <button class="relative p-2 rounded-lg hover:bg-surface-light transition-colors">
          <span class="material-icons text-text-secondary">notifications</span>
          <span class="absolute top-1 right-1 w-2 h-2 bg-accent rounded-full"></span>
        </button>

        <!-- User Menu -->
        <div class="flex items-center gap-3 pl-4 border-l border-surface-light">
          <div class="text-right">
            <p class="text-sm font-semibold text-text-primary">
              {{ authService.currentUser()?.firstName }} {{ authService.currentUser()?.lastName }}
            </p>
            <p class="text-xs text-text-muted capitalize">{{ authService.currentUser()?.role?.toLowerCase() }}</p>
          </div>
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-accent to-accent-light 
                      flex items-center justify-center text-background font-bold cursor-pointer">
            {{ authService.currentUser()?.firstName?.charAt(0) }}{{ authService.currentUser()?.lastName?.charAt(0) }}
          </div>
          <button (click)="logout()" 
                  class="p-2 rounded-lg hover:bg-error/20 transition-colors group"
                  title="Déconnexion">
            <span class="material-icons text-text-muted group-hover:text-error">logout</span>
          </button>
        </div>
      </div>
    </header>
  `
})
export class HeaderComponent {
  @Output() toggleSidebar = new EventEmitter<void>();
  
  authService = inject(AuthService);

  logout() {
    this.authService.logout();
  }
}

