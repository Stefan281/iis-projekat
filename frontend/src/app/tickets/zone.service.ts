import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Zone, ZoneRequest } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class ZoneService {
  private readonly apiUrl = 'http://localhost:8080/api/zones';

  constructor(private readonly http: HttpClient) {}

  getAll() {
    return this.http.get<Zone[]>(this.apiUrl);
  }

  getById(id: number) {
    return this.http.get<Zone>(`${this.apiUrl}/${id}`);
  }

  create(request: ZoneRequest) {
    return this.http.post<Zone>(this.apiUrl, request);
  }

  update(id: number, request: ZoneRequest) {
    return this.http.put<Zone>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
