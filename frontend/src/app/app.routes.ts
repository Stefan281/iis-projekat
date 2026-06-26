import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { roleGuard } from './auth/role.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AppShellComponent } from './layout/app-shell.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { CheckoutComponent } from './tickets/checkout/checkout.component';
import { MatchDetailsComponent } from './tickets/matches/match-details.component';
import { MatchFormComponent } from './tickets/matches/match-form.component';
import { MatchListComponent } from './tickets/matches/match-list.component';
import { MyReservationsComponent } from './tickets/my-reservations/my-reservations.component';
import { MyTicketsComponent } from './tickets/my-tickets/my-tickets.component';
import { SeatFormComponent } from './tickets/seats/seat-form.component';
import { SeatListComponent } from './tickets/seats/seat-list.component';
import { ZoneFormComponent } from './tickets/zones/zone-form.component';
import { ZoneListComponent } from './tickets/zones/zone-list.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'matches', component: MatchListComponent },
      { path: 'matches/new', component: MatchFormComponent, canActivate: [roleGuard], data: { roles: ['ADMIN'] } },
      { path: 'matches/:id', component: MatchDetailsComponent },
      { path: 'matches/:id/edit', component: MatchFormComponent, canActivate: [roleGuard], data: { roles: ['ADMIN'] } },
      { path: 'checkout', component: CheckoutComponent, canActivate: [roleGuard], data: { roles: ['CUSTOMER'] } },
      { path: 'my-tickets', component: MyTicketsComponent, canActivate: [roleGuard], data: { roles: ['CUSTOMER'] } },
      { path: 'my-reservations', component: MyReservationsComponent, canActivate: [roleGuard], data: { roles: ['CUSTOMER', 'MANAGER', 'STATISTICAR', 'STRUCNI_STAB'] } },
      { path: 'zones', component: ZoneListComponent, canActivate: [roleGuard], data: { roles: ['MANAGER', 'ADMIN'] } },
      { path: 'zones/new', component: ZoneFormComponent, canActivate: [roleGuard], data: { roles: ['MANAGER', 'ADMIN'] } },
      { path: 'zones/:id/edit', component: ZoneFormComponent, canActivate: [roleGuard], data: { roles: ['MANAGER', 'ADMIN'] } },
      { path: 'seats', component: SeatListComponent, canActivate: [roleGuard], data: { roles: ['MANAGER', 'ADMIN', 'STATISTICAR', 'STRUCNI_STAB'] } },
      { path: 'seats/new', component: SeatFormComponent, canActivate: [roleGuard], data: { roles: ['MANAGER', 'ADMIN', 'STATISTICAR', 'STRUCNI_STAB'] } },
      { path: 'seats/:id/edit', component: SeatFormComponent, canActivate: [roleGuard], data: { roles: ['MANAGER', 'ADMIN', 'STATISTICAR', 'STRUCNI_STAB'] } }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
