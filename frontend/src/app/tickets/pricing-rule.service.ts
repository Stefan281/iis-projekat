import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PricingRule, PricingRuleRequest } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class PricingRuleService {
  private readonly url = 'http://localhost:8080/api/pricing-rules';

  constructor(private http: HttpClient) {}

  getAll(): Observable<PricingRule[]> {
    return this.http.get<PricingRule[]>(this.url);
  }

  create(request: PricingRuleRequest): Observable<PricingRule> {
    return this.http.post<PricingRule>(this.url, request);
  }

  update(id: number, request: PricingRuleRequest): Observable<PricingRule> {
    return this.http.put<PricingRule>(`${this.url}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
