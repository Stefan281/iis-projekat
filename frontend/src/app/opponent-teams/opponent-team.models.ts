export interface OpponentPlayer {
  id?: number;
  fullName: string;
  jerseyNumber: number;
  position: string;
  height: number;
  age: number;
  playerStatus?: 'INACTIVE' | 'BENCH' | 'IN_GAME';
}

export interface OpponentTeam {
  id?: number;
  name: string;
  wins: number;
  losses: number;
  city: string;
  coach: string;
  playStyle?: string | null;
  note?: string | null;
  teamType?: 'HOME' | 'OPPONENT';
  players: OpponentPlayer[];
}
