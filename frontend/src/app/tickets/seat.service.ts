import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Seat, SeatRequest } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class SeatService {
  private readonly apiUrl = 'http://localhost:8080/api/seats';

  constructor(private readonly http: HttpClient) {}

  getAll(zoneId?: number, matchId?: number) {
    let params = new HttpParams();
    if (zoneId) {
      params = params.set('zoneId', zoneId);
    }
    if (matchId) {
      params = params.set('matchId', matchId);
    }

    return this.http.get<Seat[]>(this.apiUrl, { params });
  }

  getById(id: number) {
    return this.http.get<Seat>(`${this.apiUrl}/${id}`);
  }

  create(request: SeatRequest) {
    return this.http.post<Seat>(this.apiUrl, request);
  }

  update(id: number, request: SeatRequest) {
    return this.http.put<Seat>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
