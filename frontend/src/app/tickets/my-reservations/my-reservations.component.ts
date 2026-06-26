import { Component, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
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
          <h1>{{ customerTitle() }}</h1>
        </div>
        @if (isCustomer()) {
          <button class="button primary" type="button" (click)="togglePreviousReservations()">
            {{ showPreviousReservations() ? 'Aktivne rezervacije' : 'Prethodne rezervacije' }}
          </button>
        } @else {
          <a class="button primary" routerLink="/matches">Pronadji utakmice</a>
        }
      </header>

      @if (error()) {
        <p class="form-error">{{ error() }}</p>
      }

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
              @for (reservation of sortedReservations(); track reservation.id) {
                <tr>
                  <td>R{{ reservation.id }}</td>
                  <td>{{ reservation.customerFullName || 'Kupac' }}</td>
                  <td>{{ reservation.homeTeam }} vs {{ reservation.awayTeam }}</td>
                  <td>Zona {{ reservation.zoneName }}, red {{ reservation.rowLabel }}, mesto {{ reservation.seatNumber }}</td>
                  <td><span class="status-pill">{{ statusLabel(reservation.status) }}</span></td>
                  <td class="actions">
                    @if (reservation.status === 'ACTIVE') {
                      <button type="button" (click)="confirm(reservation.id)">Potvrdi</button>
                      <button type="button" (click)="cancel(reservation.id)">Otkazi rezervaciju</button>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </section>
      } @else if (customerReservations().length === 0) {
        <section class="empty-state">
          <h2>Nema rezervacija</h2>
          <p>{{ showPreviousReservations() ? 'Nemas prethodnih rezervacija.' : 'Rezervisi slobodno sediste na strani detalja utakmice.' }}</p>
        </section>
      } @else {
        <section class="cards-grid">
          @for (reservation of customerReservations(); track reservation.id) {
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
                <div><dt>Status</dt><dd>{{ statusLabel(reservation.status) }}</dd></div>
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
  readonly error = signal('');
  readonly showPreviousReservations = signal(false);

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

    this.error.set('');
    this.reservationService.cancel(id).subscribe({
      next: () => this.load(),
      error: (error) => this.error.set(this.errorMessage(error))
    });
  }

  confirm(id: number): void {
    const confirmed = window.confirm('Da li zelis da potvrdis rezervaciju i kreiras kartu?');
    if (!confirmed) {
      return;
    }

    this.error.set('');
    this.reservationService.confirm(id).subscribe({
      next: () => this.load(),
      error: (error) => this.error.set(this.errorMessage(error))
    });
  }

  isCustomer(): boolean {
    return this.authService.currentUser()?.role === 'CUSTOMER';
  }

  customerTitle(): string {
    if (!this.isCustomer()) return 'Rezervacije';
    return this.showPreviousReservations() ? 'Prethodne rezervacije' : 'Moje aktivne rezervacije';
  }

  togglePreviousReservations(): void {
    this.showPreviousReservations.update((value) => !value);
  }

  customerReservations(): Reservation[] {
    return this.showPreviousReservations()
      ? this.reservations().filter((reservation) => reservation.status !== 'ACTIVE')
      : this.reservations().filter((reservation) => reservation.status === 'ACTIVE');
  }

  sortedReservations(): Reservation[] {
    const order: Record<Reservation['status'], number> = {
      ACTIVE: 0,
      EXPIRED: 1,
      CANCELLED: 2,
      SOLD: 3
    };
    return [...this.reservations()].sort((a, b) => order[a.status] - order[b.status]);
  }

  statusLabel(status: Reservation['status']): string {
    const labels: Record<Reservation['status'], string> = {
      ACTIVE: 'Aktivna',
      CANCELLED: 'Otkazana',
      EXPIRED: 'Istekla',
      SOLD: 'Potvrdjena'
    };
    return labels[status];
  }

  private load(): void {
    const request = this.authService.currentUser()?.role === 'CUSTOMER'
      ? this.reservationService.getMyReservations()
      : this.reservationService.getAll();

    request.subscribe((reservations) => this.reservations.set(reservations));
  }

  private errorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && typeof error.error?.message === 'string') {
      return error.error.message;
    }

    return 'Akcija nije uspesno izvrsena. Proveri status rezervacije.';
  }
}
