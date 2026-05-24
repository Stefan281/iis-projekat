import { Component, inject, signal } from '@angular/core';
import { MatchDetails } from '../match-events/match-events.models';
import { MatchEventsService } from '../match-events/match-events.service';
import { OpponentTeam } from '../opponent-teams/opponent-team.models';

@Component({
  selector: 'app-staff-teams',
  templateUrl: './staff-teams.component.html',
  styleUrl: './staff-teams.component.css'
})
export class StaffTeamsComponent {
  private readonly matchEventsService = inject(MatchEventsService);

  readonly match = signal<MatchDetails | null>(null);
  readonly selectedTeam = signal<OpponentTeam | null>(null);
  readonly errorMessage = signal('');
  readonly isLoading = signal(false);

  constructor() {
    this.loadCurrentMatch();
  }

  selectTeam(team: OpponentTeam): void {
    this.selectedTeam.set(team);
  }

  isCurrentTeam(team: OpponentTeam): boolean {
    const match = this.match();
    return !!team.id && (match?.homeTeam.id === team.id || match?.awayTeam.id === team.id);
  }

  private loadCurrentMatch(): void {
    this.isLoading.set(true);
    this.matchEventsService.getCurrentMatch().subscribe({
      next: (match) => {
        this.match.set(match);
        this.selectedTeam.set(match.homeTeam);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Nije moguce ucitati timove aktuelne utakmice.');
        this.isLoading.set(false);
      }
    });
  }
}
