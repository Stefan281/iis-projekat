import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Match, Promotion, PriceTotalResponse, Seat } from '../ticketing.models';
import { MatchService } from '../match.service';
import { PriceService } from '../price.service';
import { PromotionService } from '../promotion.service';
import { SeatService } from '../seat.service';
import { TicketService } from '../ticket.service';

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
              @if (priceTotal(); as pt) {
                @if (pt.discount > 0) {
                  <div><dt>Osnovna cena</dt><dd>{{ pt.baseTotal }} RSD</dd></div>
                  <div><dt>Popust{{ pt.promotionName ? ' (' + pt.promotionName + ')' : '' }}</dt><dd>-{{ pt.discount }} RSD</dd></div>
                }
                <div><dt><strong>Ukupno za naplatu</strong></dt><dd><strong>{{ pt.finalTotal }} RSD</strong></dd></div>
              } @else {
                <div><dt>Ukupno za naplatu</dt><dd>Učitavanje...</dd></div>
              }
            </dl>

            <div class="promo-section">
              <label class="promo-label">Promo kod</label>
              <div class="promo-input-row">
                <input
                  name="promoCodeInput"
                  [(ngModel)]="promoCode"
                  placeholder="Unesite promo kod"
                  [disabled]="!!appliedPromotion()"
                  autocomplete="off"
                />
                @if (!appliedPromotion()) {
                  <button class="button" type="button" (click)="applyPromoCode()" [disabled]="!promoCode.trim()">Primeni</button>
                } @else {
                  <button class="button" type="button" (click)="removePromoCode()">Ukloni</button>
                }
              </div>
              @if (promoError()) {
                <p class="promo-error">{{ promoError() }}</p>
              }
              @if (appliedPromotion(); as promo) {
                <p class="promo-success">Primenjen: {{ promo.name }} (-{{ promo.discountPercentage }}%)</p>
              }
            </div>
          </div>

          <div class="entity-form">
            <label>
              Ime na kartici
              <input name="buyerFullName" [(ngModel)]="cardholderName" autocomplete="off" required />
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
      display: block;
      border-radius: 6px;
      background: #dcfce7;
      color: #16a34a;
      padding: 10px 12px;
      font-size: 0.82rem;
      font-weight: 700;
    }

    .promo-section {
      display: grid;
      gap: 6px;
      padding: 14px;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      background: #f9fafb;
    }

    .promo-label {
      font-size: 0.8rem;
      font-weight: 700;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .promo-input-row {
      display: flex;
      gap: 8px;
    }

    .promo-input-row input {
      flex: 1;
      padding: 8px 10px;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font: inherit;
      font-size: 0.9rem;
      text-transform: uppercase;
    }

    .promo-input-row input:disabled {
      background: #f3f4f6;
      color: #6b7280;
    }

    .promo-error {
      margin: 0;
      font-size: 0.82rem;
      color: #dc2626;
    }

    .promo-success {
      margin: 0;
      font-size: 0.82rem;
      font-weight: 600;
      color: #16a34a;
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
  readonly priceTotal = signal<PriceTotalResponse | null>(null);
  readonly matchId = signal<number | null>(null);
  readonly selectedSeatIds = signal<number[]>([]);
  readonly message = signal('');
  readonly error = signal('');
  readonly isSubmitting = signal(false);
  readonly appliedPromotion = signal<Promotion | null>(null);
  readonly promoError = signal('');

  cardholderName = '';
  cardNumber = '';
  expiry = '';
  cvv = '';
  promoCode = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly matchService: MatchService,
    private readonly seatService: SeatService,
    private readonly ticketService: TicketService,
    private readonly priceService: PriceService,
    private readonly promotionService: PromotionService
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
    this.seatService.getAll(undefined, matchId).subscribe((seats) => this.seats.set(seats));
    this.priceService.getTotal(matchId, seatIds).subscribe((total) => this.priceTotal.set(total));
  }

  applyPromoCode(): void {
    const code = this.promoCode.trim();
    if (!code) return;

    this.promotionService.getByCode(code).subscribe({
      next: (promo) => {
        if (promo.status !== 'ACTIVE') {
          this.promoError.set('Promo kod nije aktivan.');
          return;
        }
        if (promo.minTickets > this.selectedSeatIds().length) {
          this.promoError.set(`Ovaj kod važi za minimum ${promo.minTickets} karte.`);
          return;
        }
        this.appliedPromotion.set(promo);
        this.promoError.set('');
        const matchId = this.matchId()!;
        this.priceService.getTotal(matchId, this.selectedSeatIds(), promo.id)
          .subscribe((total) => this.priceTotal.set(total));
      },
      error: () => {
        this.promoError.set('Promo kod nije pronađen.');
      }
    });
  }

  removePromoCode(): void {
    this.appliedPromotion.set(null);
    this.promoCode = '';
    this.promoError.set('');
    const matchId = this.matchId()!;
    this.priceService.getTotal(matchId, this.selectedSeatIds())
      .subscribe((total) => this.priceTotal.set(total));
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

  submit(matchId: number): void {
    this.message.set('');
    this.error.set('');

    if (!this.isCardFormValid()) {
      this.error.set('Unesi ispravne podatke sa kartice.');
      return;
    }

    this.isSubmitting.set(true);
    const promotionId = this.appliedPromotion()?.id ?? undefined;
    this.ticketService.purchase({ matchId, seatIds: this.selectedSeatIds(), promotionId }).subscribe({
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

  private isCardFormValid(): boolean {
    return /^[\p{L} ]+$/u.test(this.cardholderName.trim())
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
