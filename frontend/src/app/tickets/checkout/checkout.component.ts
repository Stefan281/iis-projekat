import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Match, Seat, Zone } from '../ticketing.models';
import { MatchService } from '../match.service';
import { SeatService } from '../seat.service';
import { TicketService } from '../ticket.service';
import { ZoneService } from '../zone.service';

@Component({
  selector: 'app-checkout',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="page narrow">
      <a class="back-link" routerLink="/matches/{{ matchId() }}">Nazad na izbor sedista</a>

      <header class="page-header">
        <div>
          <p>Kupovina karata</p>
          <h1>Placanje karticom</h1>
        </div>
      </header>

      @if (match(); as selectedMatch) {
        <section class="panel checkout-page">
          <div>
            <h2>{{ selectedMatch.homeTeam }} vs {{ selectedMatch.awayTeam }}</h2>
            <p>{{ selectedMatch.date }} u {{ selectedMatch.time }} - {{ selectedMatch.location }}</p>

            <dl>
              <div><dt>Broj karata</dt><dd>{{ selectedSeats().length }}</dd></div>
              <div><dt>Sedista</dt><dd>{{ selectedSeatsLabel() }}</dd></div>
              <div><dt>Ukupno</dt><dd>{{ totalPrice(selectedMatch) }} RSD</dd></div>
            </dl>
          </div>

          <div class="entity-form">
            <label>
              Ime na kartici
              <input name="buyerFullName" [(ngModel)]="cardholderName" autocomplete="off" pattern="[A-Za-z ]+" required />
            </label>

            <label>
              Broj kartice
              <input name="ticketNumberInput" [(ngModel)]="cardNumber" autocomplete="one-time-code" inputmode="numeric" maxlength="16" placeholder="0000000000000000" required />
            </label>

            <div class="form-grid">
              <label>
                Datum isteka
                <input name="validUntilInput" [(ngModel)]="expiry" autocomplete="one-time-code" inputmode="numeric" maxlength="4" placeholder="MMYY" required />
              </label>

              <label>
                CVV
                <input name="securityInput" [(ngModel)]="cvv" autocomplete="one-time-code" inputmode="numeric" maxlength="3" required />
              </label>
            </div>

            @if (message()) {
              <p class="success dark">{{ message() }}</p>
            }
            @if (error()) {
              <p class="error">{{ error() }}</p>
            }

            <button class="button primary" type="button" [disabled]="isSubmitting() || selectedSeats().length === 0" (click)="submit(selectedMatch.id)">
              Potvrdi kupovinu
            </button>
          </div>
        </section>
      } @else {
        <section class="empty-state">
          <h2>Kupovina nije spremna</h2>
          <p>Vrati se na utakmicu i izaberi sedista.</p>
        </section>
      }
    </main>
  `,
  styles: `
    .checkout-page {
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(280px, 380px);
      gap: 22px;
    }

    .checkout-page h2 {
      margin: 0 0 8px;
    }

    .checkout-page p {
      color: #64748b;
    }

    .checkout-page dl {
      display: grid;
      gap: 10px;
      margin-top: 20px;
    }

    .checkout-page dl div {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      border-bottom: 1px solid #e5e7eb;
      padding-bottom: 10px;
    }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }

    .success.dark {
      color: #166534;
    }

    @media (max-width: 760px) {
      .checkout-page,
      .form-grid {
        grid-template-columns: 1fr;
      }
    }
  `
})
export class CheckoutComponent implements OnInit {
  readonly match = signal<Match | null>(null);
  readonly seats = signal<Seat[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly matchId = signal<number | null>(null);
  readonly selectedSeatIds = signal<number[]>([]);
  readonly message = signal('');
  readonly error = signal('');
  readonly isSubmitting = signal(false);

  cardholderName = '';
  cardNumber = '';
  expiry = '';
  cvv = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly matchService: MatchService,
    private readonly seatService: SeatService,
    private readonly ticketService: TicketService,
    private readonly zoneService: ZoneService
  ) {}

  ngOnInit(): void {
    const matchId = Number(this.route.snapshot.queryParamMap.get('matchId'));
    const seatIds = (this.route.snapshot.queryParamMap.get('seatIds') ?? '')
      .split(',')
      .map((id) => Number(id))
      .filter((id) => Number.isFinite(id) && id > 0);

    if (!matchId || seatIds.length === 0) {
      this.error.set('Nisu izabrana sedista za kupovinu.');
      return;
    }

    this.matchId.set(matchId);
    this.selectedSeatIds.set(seatIds);
    this.matchService.getById(matchId).subscribe((match) => this.match.set(match));
    this.zoneService.getAll().subscribe((zones) => this.zones.set(zones));
    this.seatService.getAll(undefined, matchId).subscribe((seats) => this.seats.set(seats));
  }

  selectedSeats(): Seat[] {
    const ids = this.selectedSeatIds();
    return this.seats()
      .filter((seat) => ids.includes(seat.id))
      .sort((first, second) => first.rowLabel.localeCompare(second.rowLabel) || first.seatNumber - second.seatNumber);
  }

  selectedSeatsLabel(): string {
    return this.selectedSeats()
      .map((seat) => `${seat.zoneName} ${seat.rowLabel}${seat.seatNumber}`)
      .join(', ');
  }

  totalPrice(match: Match): number {
    return this.selectedSeats().reduce((total, seat) => total + this.priceForSeat(match.basePrice, seat), 0);
  }

  submit(matchId: number): void {
    this.message.set('');
    this.error.set('');

    if (!this.isCardFormValid()) {
      this.error.set('Unesi ispravne podatke sa kartice.');
      return;
    }

    this.isSubmitting.set(true);
    this.ticketService.purchase({ matchId, seatIds: this.selectedSeatIds() }).subscribe({
      next: () => {
        this.message.set('Kupovina je uspesno izvrsena.');
        this.error.set('');
        this.isSubmitting.set(false);
      },
      error: (error) => {
        this.error.set(this.purchaseErrorMessage(error));
        this.isSubmitting.set(false);
      }
    });
  }

  private priceForSeat(basePrice: number, seat: Seat): number {
    const zone = this.zones().find((item) => item.id === seat.zoneId);
    return Math.round(basePrice * (zone?.priceCoefficient ?? 1));
  }

  private isCardFormValid(): boolean {
    return /^[A-Za-z ]+$/.test(this.cardholderName.trim())
      && /^\d{16}$/.test(this.cardNumber.trim())
      && /^\d{4}$/.test(this.expiry.trim())
      && /^\d{3}$/.test(this.cvv.trim());
  }

  private purchaseErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.error?.message === 'Seat is not available') {
      return 'Izabrano sediste vise nije slobodno ili je rezervisano.';
    }

    if (error instanceof HttpErrorResponse && error.status === 401) {
      return 'Sesija je istekla. Prijavi se ponovo.';
    }

    return 'Kupovina nije uspela. Proveri izabrana sedista.';
  }
}
