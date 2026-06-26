import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PurchaseRequest, Ticket } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly apiUrl = 'http://localhost:8080/api/tickets';

  constructor(private readonly http: HttpClient) {}

  getMyTickets() {
    return this.http.get<Ticket[]>(`${this.apiUrl}/my`);
  }

  purchase(request: PurchaseRequest) {
    return this.http.post<Ticket[]>(`${this.apiUrl}/purchase`, request);
  }
}
