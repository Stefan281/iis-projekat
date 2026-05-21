import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Obavestenje } from '../models/models';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/obavestenja';

  getAll() { return this.http.get<Obavestenje[]>(this.base); }
  create(tekst: string) { return this.http.post<Obavestenje>(this.base, { tekst }); }
}
