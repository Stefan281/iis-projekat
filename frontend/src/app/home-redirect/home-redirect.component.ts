import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-home-redirect',
  template: ''
})
export class HomeRedirectComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  constructor() {
    const user = this.authService.currentUser();
    this.router.navigateByUrl(user?.role === 'STRUCNI_STAB' ? '/strucni-stab' : '/dashboard');
  }
}
