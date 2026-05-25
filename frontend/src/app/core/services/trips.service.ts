import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Putovanje } from '../models/models';

export interface RoomInfo {
  roomNumber: string;
  roommates: { id: number; name: string }[];
}

@Injectable({ providedIn: 'root' })
export class TripsService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/trips';

  getAll() { return this.http.get<Putovanje[]>(this.base); }
  getById(id: number) { return this.http.get<Putovanje>(`${this.base}/${id}`); }
  create(p: Partial<Putovanje>) { return this.http.post<Putovanje>(this.base, p); }
  update(id: number, p: Partial<Putovanje>) { return this.http.put<Putovanje>(`${this.base}/${id}`, p); }
  delete(id: number) { return this.http.delete<void>(`${this.base}/${id}`); }
  updateStatus(id: number, status: string, razlogOdbijanja?: string) {
    return this.http.put<Putovanje>(`${this.base}/${id}/status`, { status, razlogOdbijanja });
  }
  getRoomAssignment(tripId: number): Observable<RoomInfo | null> {
    return this.http.get<RoomInfo>(`${this.base}/${tripId}/my-room`).pipe(
      catchError(() => of(null))
    );
  }
}
