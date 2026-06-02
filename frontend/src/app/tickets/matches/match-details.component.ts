import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
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
                    <small>{{ zone.occupancyRate }}% popunjeno</small>
                  </button>
                }
              </div>

              <div class="seat-grid">
                @for (seat of filteredSeats(); track seat.id) {
                  <button
                    type="button"
                    class="seat-dot"
                    [class.selected]="selectedSeatId() === seat.id"
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

            <aside class="panel checkout-panel">
              <h2>Tvoj izbor</h2>
              @if (selectedSeat(); as seat) {
                <dl>
                  <div><dt>Zona</dt><dd>{{ seat.zoneName }}</dd></div>
                  <div><dt>Sediste</dt><dd>Red {{ seat.rowLabel }}, sediste {{ seat.seatNumber }}</dd></div>
                  <div><dt>Ukupno</dt><dd>{{ priceForSeat(selectedMatch.basePrice, seat) }} RSD</dd></div>
                </dl>
                @if (message()) {
                  <p class="success">{{ message() }}</p>
                }
                @if (error()) {
                  <p class="error">{{ error() }}</p>
                }
                <button class="button primary" type="button" (click)="purchase(selectedMatch.id, seat.id)">Kupi kartu</button>
                <button class="button" type="button" (click)="reserve(selectedMatch.id, seat.id)">Rezervisi 24h</button>
              } @else {
                <p class="muted">Prvo izaberi slobodno sediste.</p>
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
  `
})
export class MatchDetailsComponent implements OnInit {
  readonly match = signal<Match | null>(null);
  readonly zones = signal<Zone[]>([]);
  readonly seats = signal<Seat[]>([]);
  readonly selectedZoneId = signal<number | null>(null);
  readonly selectedSeatId = signal<number | null>(null);
  readonly message = signal('');
  readonly error = signal('');

  constructor(
    private readonly route: ActivatedRoute,
    private readonly authService: AuthService,
    private readonly matchService: MatchService,
    private readonly zoneService: ZoneService,
    private readonly seatService: SeatService,
    private readonly ticketService: TicketService,
    private readonly reservationService: ReservationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.matchService.getById(id).subscribe((match) => this.match.set(match));
    this.zoneService.getAll().subscribe((zones) => {
      this.zones.set(zones);
      if (zones.length > 0) {
        this.selectedZoneId.set(zones[0].id);
      }
    });
    this.loadSeats();
  }

  filteredSeats(): Seat[] {
    const zoneId = this.selectedZoneId();
    return this.seats().filter((seat) => !zoneId || seat.zoneId === zoneId);
  }

  selectedSeat(): Seat | null {
    return this.seats().find((seat) => seat.id === this.selectedSeatId()) ?? null;
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
    this.selectedSeatId.set(null);
  }

  selectSeat(seatId: number): void {
    this.message.set('');
    this.error.set('');
    this.selectedSeatId.set(seatId);
  }

  priceForZone(basePrice: number, zone: Zone): number {
    return Math.round(basePrice * zone.priceCoefficient);
  }

  priceForSeat(basePrice: number, seat: Seat): number {
    const zone = this.zones().find((item) => item.id === seat.zoneId);
    return zone ? this.priceForZone(basePrice, zone) : basePrice;
  }

  purchase(matchId: number, seatId: number): void {
    this.ticketService.purchase({ matchId, seatId }).subscribe({
      next: () => {
        this.message.set('Karta je uspesno kupljena.');
        this.error.set('');
        this.loadSeats();
      },
      error: () => {
        this.error.set('Kupovina nije uspela. Prijavi se i izaberi slobodno sediste.');
        this.message.set('');
      }
    });
  }

  reserve(matchId: number, seatId: number): void {
    this.reservationService.reserve({ matchId, seatId }).subscribe({
      next: () => {
        this.message.set('Rezervacija je kreirana na 24 sata.');
        this.error.set('');
        this.loadSeats();
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

  private loadSeats(): void {
    this.seatService.getAll().subscribe((seats) => this.seats.set(seats));
  }
}
