import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Announcement } from '../models/models';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/obavestenja';

  getAll() { return this.http.get<Announcement[]>(this.base); }
  create(text: string) { return this.http.post<Announcement>(this.base, { tekst: text }); }
}
