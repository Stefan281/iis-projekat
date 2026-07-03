import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MatchSalesStats, PriceBreakdown, PriceHistoryEntry, PriceTotalResponse } from './ticketing.models';

@Injectable({ providedIn: 'root' })
export class PriceService {
  private readonly base = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getBreakdown(matchId: number, seatId: number, ticketTypeId: number): Observable<PriceBreakdown> {
    return this.http.get<PriceBreakdown>(`${this.base}/pricing/breakdown`, {
      params: { matchId, seatId, ticketTypeId },
    });
  }

  getTotal(matchId: number, seatIds: number[], promotionId?: number): Observable<PriceTotalResponse> {
    let params = new HttpParams().set('matchId', matchId);
    for (const id of seatIds) {
      params = params.append('seatIds', id);
    }
    if (promotionId) {
      params = params.set('promotionId', promotionId);
    }
    return this.http.get<PriceTotalResponse>(`${this.base}/pricing/total`, { params });
  }

  getHistory(ruleId?: number): Observable<PriceHistoryEntry[]> {
    const params: Record<string, number> = {};
    if (ruleId != null) params['ruleId'] = ruleId;
    return this.http.get<PriceHistoryEntry[]>(`${this.base}/pricing/history`, { params });
  }

  getSalesStats(): Observable<MatchSalesStats[]> {
    return this.http.get<MatchSalesStats[]>(`${this.base}/dashboard/sales`);
  }
}
