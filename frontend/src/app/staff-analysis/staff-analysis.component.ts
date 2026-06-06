import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AnalysisPlayer, MatchStatistics, TeamAnalysis } from '../staff/staff.models';
import { StaffStatisticsService } from '../staff/staff-statistics.service';

type AnalysisTeamSide = 'home' | 'away';

@Component({
  selector: 'app-staff-analysis',
  imports: [RouterLink],
  templateUrl: './staff-analysis.component.html',
  styleUrl: './staff-analysis.component.css'
})
export class StaffAnalysisComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly staffStatisticsService = inject(StaffStatisticsService);

  readonly statistics = signal<MatchStatistics | null>(null);
  readonly teamSide = signal<AnalysisTeamSide>('home');
  readonly errorMessage = signal('');

  readonly analysis = computed(() => {
    const stats = this.statistics();
    return this.teamSide() === 'home' ? stats?.homeAnalysis ?? null : stats?.awayAnalysis ?? null;
  });

  readonly teamName = computed(() => {
    const stats = this.statistics();
    return this.teamSide() === 'home' ? stats?.homeTeam.teamName ?? '' : stats?.awayTeam.teamName ?? '';
  });

  constructor() {
    this.route.paramMap.subscribe((params) => {
      this.teamSide.set(params.get('team') === 'away' ? 'away' : 'home');
    });

    this.staffStatisticsService.getCurrentStatistics().subscribe({
      next: (statistics) => this.statistics.set(statistics),
      error: () => this.errorMessage.set('Nije moguce ucitati analizu aktivne utakmice.')
    });
  }

  playerRows(analysis: TeamAnalysis) {
    return [
      { label: 'Najefikasniji igrac', player: analysis.mostEfficientPlayer, metric: 'efficiency' },
      { label: 'Najmanje efikasan igrac', player: analysis.leastEfficientPlayer, metric: 'efficiency' },
      { label: 'Najvise poena', player: analysis.topPointsPlayer, metric: 'points' },
      { label: 'Najvise gresaka', player: analysis.topErrorsPlayer, metric: 'errors' },
      { label: 'Najvise blokova', player: analysis.topBlocksPlayer, metric: 'blocks' },
      { label: 'Najvise servisa', player: analysis.topServesPlayer, metric: 'serves' },
      { label: 'Najvise asistencija', player: analysis.topAssistsPlayer, metric: 'assists' }
    ] as const;
  }

  teamRows(analysis: TeamAnalysis) {
    return [
      { label: 'Uspesnost servisa', value: analysis.serveIndex, suffix: '%' },
      { label: 'Uspesnost napada', value: analysis.attackIndex, suffix: '%' },
      { label: 'Uspesnost blokova', value: analysis.blockIndex, suffix: '%' },
      { label: 'Disciplina igre', value: analysis.disciplineIndex, suffix: '%' },
      { label: 'Efikasnost tima', value: analysis.teamEfficiency, suffix: '' }
    ];
  }

  playerLabel(player: AnalysisPlayer | null, metric: keyof AnalysisPlayer): string {
    if (!player) {
      return 'Nema podataka';
    }

    const value = player[metric];
    return `#${player.jerseyNumber} ${player.playerName} (${value})`;
  }
}
