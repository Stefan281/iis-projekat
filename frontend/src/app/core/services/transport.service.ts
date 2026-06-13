import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TransportOffer } from '../models/models';

@Injectable({ providedIn: 'root' })
export class TransportService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/trips';

  getOffers(tripId: number) {
    return this.http.get<TransportOffer[]>(`${this.base}/${tripId}/transport`);
  }
  addOffer(tripId: number, p: { naziv: string; vrsta: string; cena: number }) {
    return this.http.post<TransportOffer>(`${this.base}/${tripId}/transport`, p);
  }
  select(tripId: number, offerId: number) {
    return this.http.put<TransportOffer>(`${this.base}/${tripId}/transport/${offerId}/select`, {});
  }
  getSelected(tripId: number): Observable<TransportOffer | null> {
    return this.http.get<TransportOffer>(`${this.base}/${tripId}/transport/selected`).pipe(
      catchError(() => of(null))
    );
  }
}
