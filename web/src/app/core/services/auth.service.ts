import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'THERAPEUTE' | 'PATIENT';
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl;
  
  currentUser = signal<User | null>(null);
  isAuthenticated = computed(() => !!this.currentUser());
  
  constructor(private http: HttpClient, private router: Router) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage() {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        this.currentUser.set(user);
      } catch {
        this.clearStorage();
      }
    }
  }

  login(credentials: { username: string; password: string }): Observable<AuthResponse> {
    // Transform to backend expected format
    const payload: LoginRequest = {
      usernameOrEmail: credentials.username,
      password: credentials.password
    };
    
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, payload).pipe(
      tap(response => {
        localStorage.setItem('token', response.token);
        const user: User = {
          id: response.id,
          username: response.username,
          email: response.email,
          firstName: response.firstName,
          lastName: response.lastName,
          role: response.role as User['role']
        };
        localStorage.setItem('user', JSON.stringify(user));
        this.currentUser.set(user);
      })
    );
  }

  logout() {
    this.clearStorage();
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  private clearStorage() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ADMIN';
  }

  isTherapeute(): boolean {
    return this.currentUser()?.role === 'THERAPEUTE';
  }

  isPatient(): boolean {
    return this.currentUser()?.role === 'PATIENT';
  }
}
