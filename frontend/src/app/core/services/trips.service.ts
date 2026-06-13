import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Trip } from '../models/models';

export interface RoomInfo {
  roomNumber: string;
  roommates: { id: number; name: string }[];
}

@Injectable({ providedIn: 'root' })
export class TripsService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/trips';

  getAll() { return this.http.get<Trip[]>(this.base); }
  getById(id: number) { return this.http.get<Trip>(`${this.base}/${id}`); }
  create(p: Partial<Trip>) { return this.http.post<Trip>(this.base, p); }
  update(id: number, p: Partial<Trip>) { return this.http.put<Trip>(`${this.base}/${id}`, p); }
  delete(id: number) { return this.http.delete<void>(`${this.base}/${id}`); }
  updateStatus(id: number, status: string, razlogOdbijanja?: string) {
    return this.http.put<Trip>(`${this.base}/${id}/status`, { status, razlogOdbijanja });
  }
  getRoomAssignment(tripId: number): Observable<RoomInfo | null> {
    return this.http.get<RoomInfo>(`${this.base}/${tripId}/my-room`).pipe(
      catchError(() => of(null))
    );
  }
}
