import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Promotion, PromotionRequest } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private readonly apiUrl = 'http://localhost:8080/api/promotions';

  constructor(private readonly http: HttpClient) {}

  getAll() {
    return this.http.get<Promotion[]>(this.apiUrl);
  }

  getById(id: number) {
    return this.http.get<Promotion>(`${this.apiUrl}/${id}`);
  }

  create(request: PromotionRequest) {
    return this.http.post<Promotion>(this.apiUrl, request);
  }

  update(id: number, request: PromotionRequest) {
    return this.http.put<Promotion>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
