import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { LoginComponent } from './login/login.component';
import { OrgLayoutComponent } from './layout/org-layout/org-layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { TripsListComponent } from './features/trips/trips-list/trips-list.component';
import { TripFormComponent } from './features/trips/trip-form/trip-form.component';
import { InboxComponent } from './features/inbox/inbox.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: OrgLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'putovanja', component: TripsListComponent },
      { path: 'putovanja/novo', component: TripFormComponent },
      { path: 'putovanja/:id', component: TripFormComponent },
      { path: 'inbox', component: InboxComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
