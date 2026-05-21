import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Putnik } from '../models/models';

@Injectable({ providedIn: 'root' })
export class PassengersService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  getSvi() { return this.http.get<Putnik[]>(`${this.base}/korisnici?uloga=CLAN_TIMA`); }
  getPutnici(putovanjeId: number) { return this.http.get<Putnik[]>(`${this.base}/putovanja/${putovanjeId}/putnici`); }
  sacuvaj(putovanjeId: number, igracIds: number[]) { return this.http.post<void>(`${this.base}/putovanja/${putovanjeId}/putnici`, { igracIds }); }
  updateDokumentacija(putovanjeId: number, igracId: number, status: string) { return this.http.put<void>(`${this.base}/putovanja/${putovanjeId}/putnici/${igracId}/dokumentacija`, { status }); }
}
