import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { Match, Seat, Zone } from '../ticketing.models';
import { MatchService } from '../match.service';
import { ZoneService } from '../zone.service';
import { SeatService } from '../seat.service';
import { TicketService } from '../ticket.service';
import { ReservationService } from '../reservation.service';

@Component({
  selector: 'app-match-details',
  imports: [RouterLink],
  template: `
    <main class="page">
      @if (match(); as selectedMatch) {
        @if (isCustomer()) {
          <a class="back-link" routerLink="/matches">Nazad na utakmice</a>

          <section class="details-hero">
            <div>
              <span class="badge">{{ statusLabel(selectedMatch.status) }}</span>
              <h1>{{ selectedMatch.homeTeam }} <span>vs</span> {{ selectedMatch.awayTeam }}</h1>
              <p>{{ selectedMatch.date }} u {{ selectedMatch.time }} - {{ selectedMatch.location }}</p>
            </div>
            <div class="price-box">
              <span>Osnovna cena</span>
              <strong>{{ selectedMatch.basePrice }} RSD</strong>
            </div>
          </section>

          <section class="details-grid">
            <div class="panel">
              <div class="section-title">
                <h2>Zone i sedista</h2>
                <p>Izaberi slobodno sediste za rezervaciju ili kupovinu.</p>
              </div>

              <div class="arena-map">
                @for (zone of zones(); track zone.id) {
                  <button type="button" class="zone-tile" [class.selected]="selectedZoneId() === zone.id" (click)="selectZone(zone.id)">
                    <span>{{ zone.name }}</span>
                    <strong>{{ priceForZone(selectedMatch.basePrice, zone) }} RSD</strong>
                    <small>{{ zoneOccupancyPercent(zone.id) }}% popunjeno</small>
                  </button>
                }
              </div>

              <div class="seat-layout">
                @for (row of seatRows(); track row.rowLabel) {
                  <div class="seat-row">
                    <span class="row-label">{{ row.rowLabel }}</span>
                    <div class="seat-row__items">
                      @for (seat of row.seats; track seat.id) {
                        <button
                          type="button"
                          class="seat-dot"
                          [class.selected]="selectedSeatIds().includes(seat.id)"
                          [class.unavailable]="seat.status !== 'AVAILABLE'"
                          [disabled]="seat.status !== 'AVAILABLE'"
                          (click)="selectSeat(seat.id)"
                          title="Red {{ seat.rowLabel }}, sediste {{ seat.seatNumber }}"
                        >
                          {{ seat.rowLabel }}{{ seat.seatNumber }}
                        </button>
                      }
                    </div>
                  </div>
                }
              </div>
            </div>

            <aside class="panel checkout-panel">
              <h2>Tvoj izbor</h2>
              @if (message()) {
                <p class="success">{{ message() }}</p>
              }
              @if (error()) {
                <p class="error">{{ error() }}</p>
              }
              @if (selectedSeats().length > 0) {
                <dl>
                  <div><dt>Broj karata</dt><dd>{{ selectedSeats().length }}</dd></div>
                  <div><dt>Sedista</dt><dd>{{ selectedSeatsLabel() }}</dd></div>
                  <div><dt>Ukupno</dt><dd><strong>{{ totalPrice(selectedMatch) }} RSD</strong></dd></div>
                </dl>
                <button class="button primary" type="button" (click)="goToCheckout(selectedMatch.id)">Kupi karte</button>
                <button class="button" type="button" (click)="reserve(selectedMatch.id)">Rezervisi karte</button>
              } @else if (!message()) {
                <p class="muted">Prvo izaberi jedno ili vise slobodnih sedista.</p>
              }
            </aside>
          </section>
        } @else {
          <section class="manager-detail">
            <div class="manager-toolbar">
              <input type="text" [value]="selectedMatch.homeTeam + ' vs ' + selectedMatch.awayTeam" readonly />
            </div>

            <div class="manager-stats">
              <article><span>Ukupno</span><strong>{{ totalCapacity() }}</strong></article>
              <article class="blue"><span>Slobodno</span><strong>{{ seatsByStatus('AVAILABLE') }}</strong></article>
              <article class="orange"><span>Rezervisano</span><strong>{{ seatsByStatus('RESERVED') }}</strong></article>
              <article class="green"><span>Prodato</span><strong>{{ seatsByStatus('SOLD') }}</strong></article>
            </div>

            <section class="manager-table panel">
              <h2>Zone izabrana utakmica</h2>
              <table>
                <thead>
                  <tr>
                    <th>Naziv zone</th>
                    <th>Mesta</th>
                    <th>Cena</th>
                    <th>Akcije</th>
                  </tr>
                </thead>
                <tbody>
                  @for (zone of zones(); track zone.id) {
                    <tr>
                      <td>{{ zone.name }}</td>
                      <td>{{ zone.capacity }}</td>
                      <td>{{ priceForZone(selectedMatch.basePrice, zone) }} RSD</td>
                      <td><button type="button" routerLink="/zones/{{ zone.id }}/edit">Izmeni</button></td>
                    </tr>
                  }
                </tbody>
              </table>
            </section>
          </section>
        }
      }
    </main>
  `,
  styles: `
    .manager-detail {
      display: grid;
      gap: 18px;
    }

    .manager-toolbar {
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      background: #ffffff;
      padding: 16px;
    }

    .manager-toolbar input {
      max-width: 420px;
    }

    .manager-stats {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 12px;
    }

    .manager-stats article {
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      background: #ffffff;
      padding: 16px;
    }

    .manager-stats article.blue {
      background: #eff6ff;
    }

    .manager-stats article.orange {
      background: #fff7ed;
    }

    .manager-stats article.green {
      background: #f0fdf4;
    }

    .manager-stats span {
      display: block;
      color: #64748b;
      font-size: 0.68rem;
      font-weight: 900;
      text-transform: uppercase;
    }

    .manager-stats strong {
      display: block;
      margin-top: 8px;
      color: #2563eb;
      font-size: 1.1rem;
      font-weight: 900;
    }

    .manager-table h2 {
      margin: 0 0 14px;
      font-size: 0.95rem;
    }

    .seat-layout {
      display: grid;
      gap: 10px;
    }

    .seat-row {
      display: grid;
      grid-template-columns: 34px minmax(0, 1fr);
      gap: 10px;
      align-items: center;
    }

    .row-label {
      color: #475569;
      font-weight: 900;
      text-align: center;
    }

    .seat-row__items {
      display: grid;
      grid-template-columns: repeat(10, minmax(44px, 1fr));
      gap: 8px;
    }

    @media (max-width: 760px) {
      .seat-row__items {
        grid-template-columns: repeat(5, minmax(44px, 1fr));
      }
    }

  `
})
export class MatchDetailsComponent implements OnInit {
  readonly match = signal<Match | null>(null);
  readonly zones = signal<Zone[]>([]);
  readonly seats = signal<Seat[]>([]);
  readonly selectedZoneId = signal<number | null>(null);
  readonly selectedSeatIds = signal<number[]>([]);
  readonly message = signal('');
  readonly error = signal('');

  constructor(
    private readonly route: ActivatedRoute,
    private readonly authService: AuthService,
    private readonly matchService: MatchService,
    private readonly zoneService: ZoneService,
    private readonly seatService: SeatService,
    private readonly ticketService: TicketService,
    private readonly reservationService: ReservationService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.matchService.getById(id).subscribe((match) => {
      this.match.set(match);
      this.loadSeats(match.id);
    });
    this.zoneService.getAll().subscribe((zones) => {
      this.zones.set(zones);
      if (zones.length > 0) {
        this.selectedZoneId.set(zones[0].id);
      }
    });
  }

  filteredSeats(): Seat[] {
    const zoneId = this.selectedZoneId();
    return this.seats()
      .filter((seat) => !zoneId || seat.zoneId === zoneId)
      .sort((first, second) => first.rowLabel.localeCompare(second.rowLabel) || first.seatNumber - second.seatNumber);
  }

  seatRows(): Array<{ rowLabel: string; seats: Seat[] }> {
    const rows = new Map<string, Seat[]>();
    for (const seat of this.filteredSeats()) {
      rows.set(seat.rowLabel, [...(rows.get(seat.rowLabel) ?? []), seat]);
    }

    return Array.from(rows.entries()).map(([rowLabel, seats]) => ({
      rowLabel,
      seats: seats.sort((first, second) => first.seatNumber - second.seatNumber)
    }));
  }

  selectedSeat(): Seat | null {
    return this.selectedSeats()[0] ?? null;
  }

  selectedSeats(): Seat[] {
    const selectedIds = this.selectedSeatIds();
    return this.seats().filter((seat) => selectedIds.includes(seat.id));
  }

  isCustomer(): boolean {
    return this.authService.currentUser()?.role === 'CUSTOMER';
  }

  totalCapacity(): number {
    return this.zones().reduce((total, zone) => total + zone.capacity, 0);
  }

  seatsByStatus(status: Seat['status']): number {
    return this.seats().filter((seat) => seat.status === status).length;
  }

  selectZone(zoneId: number): void {
    this.selectedZoneId.set(zoneId);
  }

  selectSeat(seatId: number): void {
    this.message.set('');
    this.error.set('');
    this.selectedSeatIds.update((seatIds) =>
      seatIds.includes(seatId)
        ? seatIds.filter((id) => id !== seatId)
        : [...seatIds, seatId]
    );
  }

  priceForZone(basePrice: number, zone: Zone): number {
    return Math.round(this.applyPriceFactors(basePrice, zone.priceCoefficient));
  }

  priceForSeat(basePrice: number, seat: Seat): number {
    const zone = this.zones().find((item) => item.id === seat.zoneId);
    return zone ? this.priceForZone(basePrice, zone) : basePrice;
  }

  totalPrice(match: Match): number {
    return this.selectedSeats()
      .reduce((total, seat) => total + this.priceForSeat(match.basePrice, seat), 0);
  }

  selectedSeatsLabel(): string {
    return this.selectedSeats()
      .map((seat) => `${seat.zoneName} ${seat.rowLabel}${seat.seatNumber}`)
      .join(', ');
  }

  goToCheckout(matchId: number): void {
    const seatIds = this.selectedSeatIds();
    this.router.navigate(['/checkout'], {
      queryParams: { matchId, seatIds: seatIds.join(',') }
    });
  }

  purchase(matchId: number): void {
    const seatIds = this.selectedSeatIds();
    this.ticketService.purchase({ matchId, seatIds }).subscribe({
      next: () => {
        this.message.set('Karte su uspesno kupljene.');
        this.error.set('');
        this.selectedSeatIds.set([]);
        this.loadSeats(matchId);
      },
      error: () => {
        this.error.set('Kupovina nije uspela. Proveri izabrana sedista.');
        this.message.set('');
      }
    });
  }

  reserve(matchId: number): void {
    const seatIds = this.selectedSeatIds();
    if (seatIds.length === 0) {
      this.error.set('Izaberi bar jedno slobodno sediste.');
      return;
    }

    forkJoin(seatIds.map((seatId) => this.reservationService.reserve({ matchId, seatId }))).subscribe({
      next: () => {
        this.message.set(seatIds.length === 1 ? 'Rezervacija je kreirana.' : 'Rezervacije su kreirane.');
        this.error.set('');
        this.selectedSeatIds.set([]);
        this.loadSeats(matchId);
      },
      error: () => {
        this.error.set('Rezervacija nije uspela. Prijavi se i izaberi slobodno sediste.');
        this.message.set('');
      }
    });
  }

  statusLabel(status: Match['status']): string {
    const labels: Record<Match['status'], string> = {
      SCHEDULED: 'Zakazana',
      CANCELLED: 'Otkazana',
      FINISHED: 'Zavrsena'
    };
    return labels[status];
  }

  private loadSeats(matchId: number): void {
    this.seatService.getAll(undefined, matchId).subscribe((seats) => this.seats.set(seats));
  }

  private applyPriceFactors(basePrice: number, zoneCoefficient: number): number {
    const match = this.match();
    if (!match) return basePrice * zoneCoefficient;

    let price = basePrice * zoneCoefficient * this.attractivenessCoefficient(match.attractiveness);
    const occupancy = this.occupiedSeatsCount() / Math.max(this.seats().length, 1);
    if (occupancy >= 0.8) {
      price *= 1.2;
    } else if (occupancy >= 0.5) {
      price *= 1.1;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const matchDate = new Date(match.date);
    const daysUntilMatch = Math.round((matchDate.getTime() - today.getTime()) / 86400000);
    if (daysUntilMatch >= 30) {
      price *= 0.9;
    } else if (daysUntilMatch <= 0 && this.seatsByStatus('AVAILABLE') > 0) {
      price *= 0.85;
    }

    return price;
  }

  private attractivenessCoefficient(attractiveness: Match['attractiveness']): number {
    const coefficients: Record<Match['attractiveness'], number> = {
      LOW: 0.95,
      MEDIUM: 1,
      HIGH: 1.15,
      DERBY: 1.3
    };
    return coefficients[attractiveness];
  }

  zoneOccupancyPercent(zoneId: number): number {
    const zoneSeats = this.seats().filter((seat) => seat.zoneId === zoneId);
    if (zoneSeats.length === 0) return 0;
    const occupied = zoneSeats.filter((seat) => seat.status !== 'AVAILABLE').length;
    return Math.round((occupied / zoneSeats.length) * 100);
  }

  private occupiedSeatsCount(): number {
    return this.seats().filter((seat) => seat.status === 'RESERVED' || seat.status === 'SOLD').length;
  }
}
