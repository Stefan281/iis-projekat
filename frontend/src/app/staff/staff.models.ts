export interface TeamStatistic {
  teamId: number;
  teamName: string;
  setsWon: number;
  points: number;
  errors: number;
  serves: number;
  blocks: number;
  substitutions: number;
}

export interface PlayerStatistic {
  playerId: number;
  playerName: string;
  jerseyNumber: number;
  playerStatus: 'INACTIVE' | 'BENCH' | 'IN_GAME';
  points: number;
  errors: number;
  blocks: number;
  serves: number;
  assists: number;
}

export interface MatchStatistics {
  matchId: number;
  matchDate: string;
  result: string;
  status: string;
  homeTeam: TeamStatistic;
  awayTeam: TeamStatistic;
  homePlayers: PlayerStatistic[];
  awayPlayers: PlayerStatistic[];
}
