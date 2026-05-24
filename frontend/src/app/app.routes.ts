import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { HomeRedirectComponent } from './home-redirect/home-redirect.component';
import { LoginComponent } from './login/login.component';
import { MatchEventsComponent } from './match-events/match-events.component';
import { OpponentTeamsComponent } from './opponent-teams/opponent-teams.component';
import { StaffDashboardComponent } from './staff-dashboard/staff-dashboard.component';
import { StaffPerformanceComponent } from './staff-performance/staff-performance.component';
import { StaffTeamsComponent } from './staff-teams/staff-teams.component';
import { StatisticianLayoutComponent } from './statistician-layout/statistician-layout.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: StatisticianLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'unos-dogadjaja', component: MatchEventsComponent },
      { path: 'protivnicki-timovi', component: OpponentTeamsComponent },
      { path: 'strucni-stab', component: StaffDashboardComponent },
      { path: 'strucni-stab/utakmica', component: StaffPerformanceComponent },
      { path: 'strucni-stab/timovi', component: StaffTeamsComponent },
      { path: '', component: HomeRedirectComponent, pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
