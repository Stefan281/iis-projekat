import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Reservation, ReservationRequest } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly apiUrl = 'http://localhost:8080/api/reservations';

  constructor(private readonly http: HttpClient) {}

  getMyReservations() {
    return this.http.get<Reservation[]>(`${this.apiUrl}/my`);
  }

  getAll() {
    return this.http.get<Reservation[]>(this.apiUrl);
  }

  reserve(request: ReservationRequest) {
    return this.http.post<Reservation>(this.apiUrl, request);
  }

  cancel(id: number) {
    return this.http.patch<Reservation>(`${this.apiUrl}/${id}/cancel`, {});
  }

  confirm(id: number) {
    return this.http.patch<Reservation>(`${this.apiUrl}/${id}/confirm`, {});
  }
}
