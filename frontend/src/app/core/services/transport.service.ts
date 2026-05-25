import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { PonudaTransporta } from '../models/models';

@Injectable({ providedIn: 'root' })
export class TransportService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  getOffers(putovanjeId: number) { return this.http.get<PonudaTransporta[]>(`${this.base}/putovanja/${putovanjeId}/ponude-transporta`); }
  addOffer(putovanjeId: number, p: {naziv: string; vrsta: string; cena: number}) { return this.http.post<PonudaTransporta>(`${this.base}/putovanja/${putovanjeId}/ponude-transporta`, p); }
  select(putovanjeId: number, ponudaId: number) { return this.http.post<void>(`${this.base}/putovanja/${putovanjeId}/izaberi-transport`, { ponudaId }); }
  getSelected(putovanjeId: number) { return this.http.get<PonudaTransporta | null>(`${this.base}/putovanja/${putovanjeId}/transport`); }
}
