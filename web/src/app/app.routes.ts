import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'patients',
        loadComponent: () => import('./features/patients/patients-list/patients-list.component').then(m => m.PatientsListComponent)
      },
      {
        path: 'patients/:id',
        loadComponent: () => import('./features/patients/patient-detail/patient-detail.component').then(m => m.PatientDetailComponent)
      },
      {
        path: 'seances',
        loadComponent: () => import('./features/seances/seances-calendar/seances-calendar.component').then(m => m.SeancesCalendarComponent)
      },
      {
        path: 'therapeutes',
        loadComponent: () => import('./features/therapeutes/therapeutes-list/therapeutes-list.component').then(m => m.TherapeutesListComponent)
      },
      {
        path: 'predictions',
        loadComponent: () => import('./features/predictions/predictions-dashboard/predictions-dashboard.component').then(m => m.PredictionsDashboardComponent)
      },
      {
        path: 'settings',
        loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];

