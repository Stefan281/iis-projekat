import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { MatchStatistics } from './staff.models';

@Injectable({
  providedIn: 'root'
})
export class StaffStatisticsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/matches';

  getCurrentStatistics() {
    return this.http.get<MatchStatistics>(`${this.apiUrl}/current/statistics`);
  }

  generateCurrentReport() {
    return this.http.get(`${this.apiUrl}/current/report`, { responseType: 'blob' });
  }
}
