import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatchDetails } from '../match-events/match-events.models';
import { MatchEventsService } from '../match-events/match-events.service';

@Component({
  selector: 'app-staff-dashboard',
  imports: [RouterLink],
  templateUrl: './staff-dashboard.component.html',
  styleUrl: './staff-dashboard.component.css'
})
export class StaffDashboardComponent {
  private readonly matchEventsService = inject(MatchEventsService);

  readonly match = signal<MatchDetails | null>(null);
  readonly errorMessage = signal('');

  constructor() {
    this.matchEventsService.getCurrentMatch().subscribe({
      next: (match) => this.match.set(match),
      error: () => this.errorMessage.set('Nema aktivne utakmice.')
    });
  }

  initials(name: string): string {
    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join('')
      .toUpperCase();
  }
}
