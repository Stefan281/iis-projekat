import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatchStatistics, PlayerStatistic, TeamStatistic } from '../staff/staff.models';
import { StaffStatisticsService } from '../staff/staff-statistics.service';

type PlayerModalTeam = 'home' | 'away';

@Component({
  selector: 'app-staff-performance',
  imports: [RouterLink],
  templateUrl: './staff-performance.component.html',
  styleUrl: './staff-performance.component.css'
})
export class StaffPerformanceComponent {
  private readonly staffStatisticsService = inject(StaffStatisticsService);

  readonly statistics = signal<MatchStatistics | null>(null);
  readonly openPlayerTeam = signal<PlayerModalTeam | null>(null);
  readonly selectedPlayer = signal<PlayerStatistic | null>(null);
  readonly errorMessage = signal('');

  constructor() {
    this.staffStatisticsService.getCurrentStatistics().subscribe({
      next: (statistics) => this.statistics.set(statistics),
      error: () => this.errorMessage.set('Nije moguce ucitati statistiku aktivne utakmice.')
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

  rows(team: TeamStatistic) {
    return [
      { label: 'Setovi', value: team.setsWon },
      { label: 'Poeni', value: team.points },
      { label: 'Greske', value: team.errors },
      { label: 'Servisi', value: team.serves },
      { label: 'Blokovi', value: team.blocks },
      { label: 'Izmene', value: team.substitutions }
    ];
  }

  openPlayers(team: PlayerModalTeam): void {
    this.openPlayerTeam.set(team);
    this.selectedPlayer.set(null);
  }

  closePlayers(): void {
    this.openPlayerTeam.set(null);
    this.selectedPlayer.set(null);
  }

  selectPlayer(player: PlayerStatistic): void {
    this.selectedPlayer.set(player);
  }

  closePlayerDetails(): void {
    this.selectedPlayer.set(null);
  }

  currentPlayers(): PlayerStatistic[] {
    const stats = this.statistics();
    const team = this.openPlayerTeam();

    if (!stats || !team) {
      return [];
    }

    return team === 'home' ? stats.homePlayers : stats.awayPlayers;
  }

  currentTeamName(): string {
    const stats = this.statistics();
    const team = this.openPlayerTeam();

    if (!stats || !team) {
      return '';
    }

    return team === 'home' ? stats.homeTeam.teamName : stats.awayTeam.teamName;
  }

  playerStatusLabel(player: PlayerStatistic): string {
    if (player.playerStatus === 'IN_GAME') {
      return 'u igri';
    }

    if (player.playerStatus === 'BENCH') {
      return 'na klupi';
    }

    return 'neaktivan';
  }

  playerRows(player: PlayerStatistic) {
    return [
      { label: 'Poeni', value: player.points },
      { label: 'Greske', value: player.errors },
      { label: 'Blokovi', value: player.blocks },
      { label: 'Servisi', value: player.serves },
      { label: 'Asistencije', value: player.assists }
    ];
  }
}
