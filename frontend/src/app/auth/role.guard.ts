import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { UserRole } from './auth.models';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = route.data['roles'] as UserRole[] | undefined;
  const role = authService.currentUser()?.role;

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  if (!allowedRoles || (role && allowedRoles.includes(role))) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
