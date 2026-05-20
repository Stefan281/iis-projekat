import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly user = this.authService.currentUser;
  readonly backendMessage = signal('');

  constructor() {
    this.http.get('http://localhost:8080/api/test', { responseType: 'text' }).subscribe({
      next: (message) => this.backendMessage.set(message),
      error: () => this.backendMessage.set('Backend nije dostupan ili token nije validan.')
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
