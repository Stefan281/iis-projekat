import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { Match } from '../ticketing.models';
import { MatchService } from '../match.service';

@Component({
  selector: 'app-match-list',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="page">
      <div class="toolbar">
        <input type="search" placeholder="Pretraga po timu..." aria-label="Pretraga po timu" [(ngModel)]="searchTerm" />
        @if (showStatusFilter()) {
          <select aria-label="Status" [(ngModel)]="selectedStatus">
            <option value="">Svi statusi</option>
            <option value="SCHEDULED">Zakazana</option>
            <option value="CANCELLED">Otkazana</option>
            <option value="FINISHED">Zavrsena</option>
          </select>
        }
        @if (canManageMatches()) {
          <a class="button primary" routerLink="/matches/new">Dodaj novu utakmicu</a>
        }
      </div>

      <table>
        <thead>
          <tr>
            <th>Utakmica</th>
            <th>Datum</th>
            <th>Lokacija</th>
            <th>Status</th>
            <th>Cena od</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          @for (match of filteredMatches(); track match.id) {
            <tr>
              <td>{{ match.homeTeam }} vs {{ match.awayTeam }}</td>
              <td>{{ match.date }} u {{ match.time }}</td>
              <td>{{ match.location }}</td>
              <td>{{ statusLabel(match.status) }}</td>
              <td>{{ match.basePrice }} RSD</td>
              <td class="actions">
                <button type="button" routerLink="/matches/{{ match.id }}">Detalji</button>
                @if (canManageMatches()) {
                  <button type="button" routerLink="/matches/{{ match.id }}/edit">Izmeni</button>
                  <button type="button" (click)="cancel(match)">Otkazi</button>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </main>
  `
})
export class MatchListComponent implements OnInit {
  private readonly authService = inject(AuthService);
  readonly matches = signal<Match[]>([]);
  searchTerm = '';
  selectedStatus = '';
  readonly canManageMatches = computed(() => this.authService.currentUser()?.role === 'ADMIN');
  readonly showStatusFilter = computed(() => this.authService.currentUser()?.role !== 'CUSTOMER');

  filteredMatches(): Match[] {
    const query = this.searchTerm.trim().toLowerCase();
    const status = this.selectedStatus;

    return this.matches().filter((match) => {
      const teams = `${match.homeTeam} ${match.awayTeam}`.toLowerCase();
      const matchesSearch = !query || teams.includes(query);
      const matchesStatus = !status || match.status === status;
      return matchesSearch && matchesStatus;
    });
  }

  constructor(private readonly matchService: MatchService) {}

  ngOnInit(): void {
    this.load();
  }

  cancel(match: Match): void {
    const confirmed = window.confirm(`Da li si siguran da zelis da otkazes utakmicu ${match.homeTeam} vs ${match.awayTeam}?`);
    if (!confirmed) {
      return;
    }

    this.matchService.update(match.id, { ...match, status: 'CANCELLED' }).subscribe(() => this.load());
  }

  statusLabel(status: Match['status']): string {
    const labels: Record<Match['status'], string> = {
      SCHEDULED: 'Zakazana',
      CANCELLED: 'Otkazana',
      FINISHED: 'Zavrsena'
    };
    return labels[status];
  }

  private load(): void {
    this.matchService.getAll().subscribe((matches) => this.matches.set(matches));
  }
}
