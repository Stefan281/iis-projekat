import { Routes } from '@angular/router';
import { organizatorGuard } from './auth/organizator.guard';
import { directorGuard } from './auth/director.guard';
import { teamMemberGuard } from './auth/team-member.guard';
import { LoginComponent } from './login/login.component';
import { OrgLayoutComponent } from './layout/org-layout/org-layout.component';
import { DirectorLayoutComponent } from './layout/director-layout/director-layout.component';
import { TeamLayoutComponent } from './layout/team-layout/team-layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { TripsListComponent } from './features/trips/trips-list/trips-list.component';
import { TripFormComponent } from './features/trips/trip-form/trip-form.component';
import { InboxComponent } from './features/inbox/inbox.component';
import { DirectorDashboardComponent } from './director/dashboard/director-dashboard.component';
import { CostAnalysisComponent } from './director/cost-analysis/cost-analysis.component';
import { DirectorInboxComponent } from './director/inbox/director-inbox.component';
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
    component: DirectorLayoutComponent,
    canActivate: [directorGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DirectorDashboardComponent },
      { path: 'analiza-troskova', component: CostAnalysisComponent },
      { path: 'inbox', component: DirectorInboxComponent }
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
