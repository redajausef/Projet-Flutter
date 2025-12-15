import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet, SidebarComponent, HeaderComponent],
  template: `
    <div class="flex min-h-screen bg-background">
      <!-- Sidebar -->
      <app-sidebar 
        [collapsed]="sidebarCollapsed()" 
        (toggleCollapse)="toggleSidebar()"
      />
      
      <!-- Main Content -->
      <div class="flex-1 flex flex-col transition-all duration-300"
           [class.ml-64]="!sidebarCollapsed()"
           [class.ml-20]="sidebarCollapsed()">
        <!-- Header -->
        <app-header (toggleSidebar)="toggleSidebar()" />
        
        <!-- Page Content -->
        <main class="flex-1 p-6 overflow-auto">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
    }
  `]
})
export class MainLayoutComponent {
  sidebarCollapsed = signal(false);

  toggleSidebar() {
    this.sidebarCollapsed.update(v => !v);
  }
}

