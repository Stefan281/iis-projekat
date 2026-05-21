import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { OpponentTeam } from './opponent-team.models';

@Injectable({
  providedIn: 'root'
})
export class OpponentTeamsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/opponents';

  getAll() {
    return this.http.get<OpponentTeam[]>(this.apiUrl);
  }

  create(team: OpponentTeam) {
    return this.http.post<OpponentTeam>(this.apiUrl, team);
  }

  update(id: number, team: OpponentTeam) {
    return this.http.put<OpponentTeam>(`${this.apiUrl}/${id}`, team);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
