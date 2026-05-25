import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { PonudaSmestaja } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AccommodationService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  getOffers(putovanjeId: number) { return this.http.get<PonudaSmestaja[]>(`${this.base}/putovanja/${putovanjeId}/ponude-smestaja`); }
  addOffer(putovanjeId: number, p: {ime: string; adresa: string; cena: number}) { return this.http.post<PonudaSmestaja>(`${this.base}/putovanja/${putovanjeId}/ponude-smestaja`, p); }
  select(putovanjeId: number, ponudaId: number) { return this.http.post<void>(`${this.base}/putovanja/${putovanjeId}/izaberi-smestaj`, { ponudaId }); }
  getSelected(putovanjeId: number) { return this.http.get<PonudaSmestaja | null>(`${this.base}/putovanja/${putovanjeId}/smestaj`); }
}
