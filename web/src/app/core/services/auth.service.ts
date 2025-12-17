import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, switchMap, of, catchError, map } from 'rxjs';
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

// Backend response structure
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    role: string;
    profileImageUrl?: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl;

  currentUser = signal<User | null>(null);
  therapeuteId = signal<number | null>(null);
  isAuthenticated = computed(() => !!this.currentUser());

  constructor(private http: HttpClient, private router: Router) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage() {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    const therapeuteIdStr = localStorage.getItem('therapeuteId');

    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        this.currentUser.set(user);

        if (therapeuteIdStr) {
          this.therapeuteId.set(parseInt(therapeuteIdStr, 10));
        }
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
        // Store tokens
        localStorage.setItem('token', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);

        // Create user from response
        const user: User = {
          id: response.user.id,
          username: response.user.username,
          email: response.user.email,
          firstName: response.user.firstName,
          lastName: response.user.lastName,
          role: response.user.role as User['role']
        };
        localStorage.setItem('user', JSON.stringify(user));
        this.currentUser.set(user);
      }),
      // Chain the therapeute fetch for therapist users
      switchMap(response => {
        if (response.user.role === 'THERAPEUTE') {
          return this.http.get<any>(`${this.apiUrl}/therapeutes/me`).pipe(
            tap(therapeute => {
              console.log('Therapeute loaded:', therapeute);
              this.setTherapeuteId(therapeute.id);
              localStorage.setItem('currentTherapeute', JSON.stringify(therapeute));
            }),
            catchError(err => {
              console.error('Error loading therapeute:', err);
              // Fallback: use user ID as approximation
              this.setTherapeuteId(1); // Default to 1 for dr.martin
              return of(null);
            }),
            // Return the original response after therapeute is loaded
            map(() => response)
          );
        }
        return of(response);
      })
    );
  }

  logout() {
    this.clearStorage();
    this.currentUser.set(null);
    this.therapeuteId.set(null);
    this.router.navigate(['/login']);
  }

  private clearStorage() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    localStorage.removeItem('therapeuteId');
    localStorage.removeItem('currentTherapeute');
  }

  /**
   * Store therapeute ID after fetching from backend
   */
  setTherapeuteId(id: number): void {
    this.therapeuteId.set(id);
    localStorage.setItem('therapeuteId', id.toString());
  }

  /**
   * Get current therapeute ID
   */
  getTherapeuteId(): number | null {
    return this.therapeuteId();
  }

  /**
   * Get current user ID
   */
  getUserId(): number | null {
    return this.currentUser()?.id || null;
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
