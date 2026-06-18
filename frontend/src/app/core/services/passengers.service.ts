import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Passenger } from '../models/models';

@Injectable({ providedIn: 'root' })
export class PassengersService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  getAll(tripId: number) {
    return this.http.get<Passenger[]>(`${this.base}/trips/${tripId}/passengers`);
  }

  toggle(tripId: number, userId: number, checked: boolean) {
    return this.http.post<Passenger>(
      `${this.base}/trips/${tripId}/passengers/toggle`,
      { userId, checked }
    );
  }

  updateRoom(tripId: number, userId: number, roomNumber: string) {
    return this.http.put<Passenger>(
      `${this.base}/trips/${tripId}/passengers/${userId}/room`,
      { roomNumber }
    );
  }

  updateDocumentation(tripId: number, userId: number, status: string) {
    return this.http.put<Passenger>(
      `${this.base}/trips/${tripId}/passengers/${userId}/documentation`,
      { status }
    );
  }
}
