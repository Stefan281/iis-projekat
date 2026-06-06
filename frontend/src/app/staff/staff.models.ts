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

export interface AnalysisPlayer {
  playerId: number;
  playerName: string;
  jerseyNumber: number;
  points: number;
  errors: number;
  blocks: number;
  serves: number;
  assists: number;
  efficiency: number;
}

export interface TeamAnalysis {
  teamId: number;
  teamName: string;
  teamEfficiency: number;
  attackIndex: number;
  serveIndex: number;
  blockIndex: number;
  disciplineIndex: number;
  mostEfficientPlayer: AnalysisPlayer | null;
  leastEfficientPlayer: AnalysisPlayer | null;
  topPointsPlayer: AnalysisPlayer | null;
  topErrorsPlayer: AnalysisPlayer | null;
  topBlocksPlayer: AnalysisPlayer | null;
  topServesPlayer: AnalysisPlayer | null;
  topAssistsPlayer: AnalysisPlayer | null;
}

export interface PlayerAnalysis {
  playerId: number;
  playerName: string;
  jerseyNumber: number;
  efficiency: number;
  serveContribution: number;
  overallRating: number;
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
  homeAnalysis: TeamAnalysis | null;
  awayAnalysis: TeamAnalysis | null;
  homePlayerAnalyses: PlayerAnalysis[];
  awayPlayerAnalyses: PlayerAnalysis[];
}
