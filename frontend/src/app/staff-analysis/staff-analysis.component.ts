import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ActivityRecommendation, AnalysisPlayer, MatchStatistics, PlayerAnalysis, PlayerStatistic, TeamAnalysis } from '../staff/staff.models';
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
  readonly isPlayerListOpen = signal(false);
  readonly selectedPlayer = signal<PlayerStatistic | null>(null);
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

  teamPlayerRows(analysis: TeamAnalysis) {
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
      { label: 'Indeks servisa', value: analysis.serveIndex, suffix: '/10' },
      { label: 'Indeks napada', value: analysis.attackIndex, suffix: '/10' },
      { label: 'Indeks blokova', value: analysis.blockIndex, suffix: '/10' },
      { label: 'Disciplina igre', value: analysis.disciplineIndex, suffix: '/10' },
      { label: 'Efikasnost tima', value: analysis.teamEfficiency, suffix: '/10' }
    ];
  }

  playerLabel(player: AnalysisPlayer | null, metric: keyof AnalysisPlayer): string {
    if (!player) {
      return 'Nema podataka';
    }

    const value = player[metric];
    return `#${player.jerseyNumber} ${player.playerName} (${value})`;
  }

  openPlayers(): void {
    this.isPlayerListOpen.set(true);
    this.selectedPlayer.set(null);
  }

  closePlayers(): void {
    this.isPlayerListOpen.set(false);
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

    if (!stats) {
      return [];
    }

    return this.teamSide() === 'home' ? stats.homePlayers : stats.awayPlayers;
  }

  selectedPlayerAnalysis(): PlayerAnalysis | null {
    const stats = this.statistics();
    const player = this.selectedPlayer();

    if (!stats || !player) {
      return null;
    }

    const analyses = this.teamSide() === 'home' ? stats.homePlayerAnalyses : stats.awayPlayerAnalyses;
    return analyses.find((analysis) => analysis.playerId === player.playerId) ?? null;
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

  playerAnalysisRows(analysis: PlayerAnalysis | null) {
    if (!analysis) {
      return [
        { label: 'Efikasnost', value: 'Nema podataka' },
        { label: 'Doprinos servisa', value: 'Nema podataka' },
        { label: 'Ukupna ocena', value: 'Nema podataka' }
      ];
    }

    return [
      { label: 'Efikasnost', value: `${analysis.efficiency}/10` },
      { label: 'Doprinos servisa', value: `${analysis.serveContribution}/10` },
      { label: 'Ukupna ocena', value: `${analysis.overallRating}/10` }
    ];
  }

  recommendations(): ActivityRecommendation[] {
    return this.statistics()?.recommendations ?? [];
  }

  priorityLabel(priority: ActivityRecommendation['priority']): string {
    if (priority === 'HIGH') {
      return 'Visok prioritet';
    }

    if (priority === 'MEDIUM') {
      return 'Srednji prioritet';
    }

    return 'Nizak prioritet';
  }

  recommendationTeamLabel(recommendation: ActivityRecommendation): string {
    return recommendation.teamType === 'HOME' ? 'Nas tim' : recommendation.teamName;
  }
}
