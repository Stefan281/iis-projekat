import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CostAnalysisService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/cost-analysis';

  getCostAnalysis(year: number, month: number): Observable<any> {
    return this.http.get(`${this.base}?year=${year}&month=${month}`);
  }

  downloadReport(year: number, month: number): Observable<Blob> {
    return this.http.get(`${this.base}/report?year=${year}&month=${month}`, { responseType: 'blob' });
  }
}
