import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const direktorGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const user = authService.currentUser();
  if (!user || user.role !== 'GENERALNI_DIREKTOR') {
    router.navigate(['/login']);
    return false;
  }
  return true;
};
