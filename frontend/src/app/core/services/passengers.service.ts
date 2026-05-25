import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Putnik } from '../models/models';

@Injectable({ providedIn: 'root' })
export class PassengersService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  getAll() { return this.http.get<Putnik[]>(`${this.base}/korisnici?uloga=CLAN_TIMA`); }
  getPassengers(putovanjeId: number) { return this.http.get<Putnik[]>(`${this.base}/putovanja/${putovanjeId}/putnici`); }
  save(putovanjeId: number, igracIds: number[]) { return this.http.post<void>(`${this.base}/putovanja/${putovanjeId}/putnici`, { igracIds }); }
  updateDocumentation(putovanjeId: number, igracId: number, status: string) { return this.http.put<void>(`${this.base}/putovanja/${putovanjeId}/putnici/${igracId}/dokumentacija`, { status }); }
}
