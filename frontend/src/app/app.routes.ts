import { Routes } from '@angular/router';
import { organizatorGuard } from './auth/organizator.guard';
import { direktorGuard } from './auth/direktor.guard';
import { teamMemberGuard } from './auth/team-member.guard';
import { LoginComponent } from './login/login.component';
import { OrgLayoutComponent } from './layout/org-layout/org-layout.component';
import { DirektorLayoutComponent } from './layout/direktor-layout/direktor-layout.component';
import { TeamLayoutComponent } from './layout/team-layout/team-layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { TripsListComponent } from './features/trips/trips-list/trips-list.component';
import { TripFormComponent } from './features/trips/trip-form/trip-form.component';
import { InboxComponent } from './features/inbox/inbox.component';
import { DirektorDashboardComponent } from './direktor/dashboard/direktor-dashboard.component';
import { AnalizaTroskovaComponent } from './direktor/analiza-troskova/analiza-troskova.component';
import { DirektorInboxComponent } from './direktor/inbox/direktor-inbox.component';
import { TeamDashboardComponent } from './team/dashboard/team-dashboard.component';
import { TeamInboxComponent } from './team/inbox/team-inbox.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: OrgLayoutComponent,
    canActivate: [organizatorGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'putovanja', component: TripsListComponent },
      { path: 'putovanja/novo', component: TripFormComponent },
      { path: 'putovanja/:id', component: TripFormComponent },
      { path: 'inbox', component: InboxComponent }
    ]
  },
  {
    path: 'direktor',
    component: DirektorLayoutComponent,
    canActivate: [direktorGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DirektorDashboardComponent },
      { path: 'analiza-troskova', component: AnalizaTroskovaComponent },
      { path: 'inbox', component: DirektorInboxComponent }
    ]
  },
  {
    path: 'team',
    component: TeamLayoutComponent,
    canActivate: [teamMemberGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: TeamDashboardComponent },
      { path: 'inbox', component: TeamInboxComponent }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
