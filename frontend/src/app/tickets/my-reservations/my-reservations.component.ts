import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { Reservation } from '../ticketing.models';
import { ReservationService } from '../reservation.service';

@Component({
  selector: 'app-my-reservations',
  imports: [RouterLink],
  template: `
    <main class="page">
      <header class="page-header">
        <div>
          <p>Rezervacije</p>
          <h1>{{ authService.currentUser()?.role === 'CUSTOMER' ? 'Moje rezervacije' : 'Rezervacije' }}</h1>
        </div>
        <a class="button primary" routerLink="/matches">Pronadji utakmice</a>
      </header>

      @if (!isCustomer()) {
        <section class="manager-reservations panel">
          <div class="toolbar reservation-toolbar">
            <input type="search" placeholder="Pretraga po imenu kupca..." aria-label="Pretraga po imenu kupca" />
            <select aria-label="Status rezervacije">
              <option>Svi statusi</option>
              <option>Aktivna</option>
              <option>Otkazana</option>
              <option>Istekla</option>
            </select>
          </div>

          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Kupac</th>
                <th>Utakmica</th>
                <th>Sediste</th>
                <th>Status</th>
                <th>Akcije</th>
              </tr>
            </thead>
            <tbody>
              @for (reservation of reservations(); track reservation.id) {
                <tr>
                  <td>R{{ reservation.id }}</td>
                  <td>{{ reservation.customerFullName || 'Kupac' }}</td>
                  <td>{{ reservation.homeTeam }} vs {{ reservation.awayTeam }}</td>
                  <td>Zona {{ reservation.zoneName }}, red {{ reservation.rowLabel }}, mesto {{ reservation.seatNumber }}</td>
                  <td><span class="status-pill">{{ statusLabel(reservation.status) }}</span></td>
                  <td class="actions">
                    <button type="button">Potvrdi</button>
                    @if (reservation.status === 'ACTIVE') {
                      <button type="button" (click)="cancel(reservation.id)">Otkazi</button>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </section>
      } @else if (reservations().length === 0) {
        <section class="empty-state">
          <h2>Nema rezervacija</h2>
          <p>Rezervisi slobodno sediste na strani detalja utakmice.</p>
        </section>
      } @else {
        <section class="cards-grid">
          @for (reservation of reservations(); track reservation.id) {
            <article class="ticket-card">
              <div class="ticket-card__head">
                <span>{{ reservation.status }}</span>
                <strong>#{{ reservation.id }}</strong>
              </div>
              <h2>{{ reservation.homeTeam }} vs {{ reservation.awayTeam }}</h2>
              <p>{{ reservation.matchDate }} u {{ reservation.matchTime }}</p>
              <dl>
                <div><dt>Sediste</dt><dd>{{ reservation.zoneName }}, red {{ reservation.rowLabel }}, sediste {{ reservation.seatNumber }}</dd></div>
                <div><dt>Cena</dt><dd>{{ reservation.price }} RSD</dd></div>
                <div><dt>Istice</dt><dd>{{ reservation.expiresAt }}</dd></div>
              </dl>
              @if (reservation.status === 'ACTIVE') {
                <button type="button" (click)="cancel(reservation.id)">Otkazi rezervaciju</button>
              }
            </article>
          }
        </section>
      }
    </main>
  `,
  styles: `
    .manager-reservations {
      padding: 18px;
    }

    .reservation-toolbar {
      justify-content: flex-start;
    }

    .reservation-toolbar input {
      max-width: 360px;
    }

    .reservation-toolbar select {
      max-width: 180px;
    }

    .status-pill {
      display: inline-flex;
      border-radius: 999px;
      background: #dcfce7;
      color: #16a34a;
      padding: 4px 8px;
      font-size: 0.68rem;
      font-weight: 900;
    }
  `
})
export class MyReservationsComponent implements OnInit {
  readonly reservations = signal<Reservation[]>([]);

  constructor(
    private readonly reservationService: ReservationService,
    readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  cancel(id: number): void {
    const confirmed = window.confirm('Da li si siguran da zelis da otkazes rezervaciju?');
    if (!confirmed) {
      return;
    }

    this.reservationService.cancel(id).subscribe(() => this.load());
  }

  isCustomer(): boolean {
    return this.authService.currentUser()?.role === 'CUSTOMER';
  }

  statusLabel(status: Reservation['status']): string {
    const labels: Record<Reservation['status'], string> = {
      ACTIVE: 'Aktivna',
      CANCELLED: 'Otkazana',
      EXPIRED: 'Istekla',
      SOLD: 'Prodata'
    };
    return labels[status];
  }

  private load(): void {
    const request = this.authService.currentUser()?.role === 'CUSTOMER'
      ? this.reservationService.getMyReservations()
      : this.reservationService.getAll();

    request.subscribe((reservations) => this.reservations.set(reservations));
  }
}
