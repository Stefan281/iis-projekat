import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Poruka } from '../models/models';

@Injectable({ providedIn: 'root' })
export class InboxService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  getInbox() { return this.http.get<Poruka[]>(`${this.base}/inbox`); }
  odgovori(id: number, tekst: string) { return this.http.post<void>(`${this.base}/inbox/${id}/odgovor`, { tekst }); }
  oznaciBroj() { return this.http.get<{count: number}>(`${this.base}/inbox/unread-count`); }
  oznacProcitano(id: number) { return this.http.put<void>(`${this.base}/inbox/${id}/procitano`, {}); }
  // TODO: verify endpoint exists on backend
  posalji(data: { primalacId: number; tekst: string; putovanjeId?: number }) {
    return this.http.post<void>(`${this.base}/inbox`, data);
  }
}
