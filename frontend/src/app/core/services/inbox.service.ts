import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Message } from '../models/models';

@Injectable({ providedIn: 'root' })
export class InboxService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  getMessages() { return this.http.get<Message[]>(`${this.base}/inbox`); }
  reply(id: number, text: string) { return this.http.post<void>(`${this.base}/inbox/${id}/odgovor`, { tekst: text }); }
  getUnreadCount() { return this.http.get<{count: number}>(`${this.base}/inbox/unread-count`); }
  markAsRead(id: number) { return this.http.put<void>(`${this.base}/inbox/${id}/procitano`, {}); }
  send(data: { primalacId: number; tekst: string; putovanjeId?: number }) {
    return this.http.post<void>(`${this.base}/inbox`, data);
  }
}
