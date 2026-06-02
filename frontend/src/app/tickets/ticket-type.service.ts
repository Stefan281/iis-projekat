import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TicketType, TicketTypeRequest } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class TicketTypeService {
  private readonly apiUrl = 'http://localhost:8080/api/ticket-types';

  constructor(private readonly http: HttpClient) {}

  getAll() {
    return this.http.get<TicketType[]>(this.apiUrl);
  }

  getById(id: number) {
    return this.http.get<TicketType>(`${this.apiUrl}/${id}`);
  }

  create(request: TicketTypeRequest) {
    return this.http.post<TicketType>(this.apiUrl, request);
  }

  update(id: number, request: TicketTypeRequest) {
    return this.http.put<TicketType>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
