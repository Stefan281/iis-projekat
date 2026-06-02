import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Match, MatchRequest } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class MatchService {
  private readonly apiUrl = 'http://localhost:8080/api/matches';

  constructor(private readonly http: HttpClient) {}

  getAll() {
    return this.http.get<Match[]>(this.apiUrl);
  }

  getById(id: number) {
    return this.http.get<Match>(`${this.apiUrl}/${id}`);
  }

  create(request: MatchRequest) {
    return this.http.post<Match>(this.apiUrl, request);
  }

  update(id: number, request: MatchRequest) {
    return this.http.put<Match>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
