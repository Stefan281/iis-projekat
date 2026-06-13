import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AccommodationOffer } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AccommodationService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/trips';

  getOffers(tripId: number) {
    return this.http.get<AccommodationOffer[]>(`${this.base}/${tripId}/accommodation`);
  }
  addOffer(tripId: number, p: { ime: string; adresa: string; cena: number }) {
    return this.http.post<AccommodationOffer>(`${this.base}/${tripId}/accommodation`, p);
  }
  select(tripId: number, offerId: number) {
    return this.http.put<AccommodationOffer>(`${this.base}/${tripId}/accommodation/${offerId}/select`, {});
  }
  getSelected(tripId: number): Observable<AccommodationOffer | null> {
    return this.http.get<AccommodationOffer>(`${this.base}/${tripId}/accommodation/selected`).pipe(
      catchError(() => of(null))
    );
  }
}
