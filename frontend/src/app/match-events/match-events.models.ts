import { OpponentPlayer, OpponentTeam } from '../opponent-teams/opponent-team.models';

export type MatchEventType = 'POINT' | 'ERROR' | 'SERVE' | 'ASSIST' | 'BLOCK';

export interface MatchEvent {
  id: number;
  eventType: MatchEventType;
  eventTime: string;
  description?: string | null;
  statisticianId: number;
  primaryPlayerId: number;
  secondaryPlayerId?: number | null;
  playerName: string;
  jerseyNumber: number;
  teamName: string;
}

export interface MatchDetails {
  id: number;
  matchDate: string;
  result: string;
  status: string;
  homeTeam: OpponentTeam;
  awayTeam: OpponentTeam;
  events: MatchEvent[];
}

export interface MatchEventRequest {
  statisticianId: number;
  primaryPlayerId: number;
  secondaryPlayerId?: number | null;
  eventType: MatchEventType;
  description?: string | null;
}

export interface PlayerSelection {
  team: OpponentTeam;
  player: OpponentPlayer;
}
