import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { MatchDetails, MatchEvent, MatchEventRequest } from './match-events.models';

@Injectable({
  providedIn: 'root'
})
export class MatchEventsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/matches';

  getCurrentMatch() {
    return this.http.get<MatchDetails>(`${this.apiUrl}/current`);
  }

  addEvent(matchId: number, request: MatchEventRequest) {
    return this.http.post<MatchEvent>(`${this.apiUrl}/${matchId}/events`, request);
  }
}
