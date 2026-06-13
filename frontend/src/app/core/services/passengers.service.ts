import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Passenger } from '../models/models';

@Injectable({ providedIn: 'root' })
export class PassengersService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  /** All team members merged with their participant data for this trip. */
  getAll(tripId: number) {
    return this.http.get<Passenger[]>(`${this.base}/trips/${tripId}/passengers`);
  }

  /** Adds (checked=true) or removes (checked=false) a member from the trip. */
  toggle(tripId: number, userId: number, checked: boolean) {
    return this.http.post<Passenger>(
      `${this.base}/trips/${tripId}/passengers/toggle`,
      { userId, checked }
    );
  }

  updateRoom(tripId: number, participantId: number, roomNumber: string) {
    return this.http.put<Passenger>(
      `${this.base}/trips/${tripId}/passengers/${participantId}/room`,
      { roomNumber }
    );
  }

  updateDocumentation(tripId: number, participantId: number, status: string) {
    return this.http.put<Passenger>(
      `${this.base}/trips/${tripId}/passengers/${participantId}/documentation`,
      { status }
    );
  }

  /** Persists a documentation status for a member who is not a participant of the trip. */
  setDocumentationForNonParticipant(tripId: number, userId: number, status: string) {
    return this.http.post<Passenger>(
      `${this.base}/trips/${tripId}/passengers/documentation`,
      { userId, status }
    );
  }
}
