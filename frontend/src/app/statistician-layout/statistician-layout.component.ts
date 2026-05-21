import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-statistician-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './statistician-layout.component.html',
  styleUrl: './statistician-layout.component.css'
})
export class StatisticianLayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
