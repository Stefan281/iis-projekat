import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

const TEAM_ROLES = ['IGRAC', 'STATISTICAR', 'STRUCNI_STAB'];

export const teamMemberGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const user = authService.currentUser();

  if (!user || !TEAM_ROLES.includes(user.role)) {
    router.navigate(['/login']);
    return false;
  }
  return true;
};
