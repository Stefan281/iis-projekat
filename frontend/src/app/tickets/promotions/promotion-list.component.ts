import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Promotion, PromotionType } from '../ticketing.models';
import { PromotionService } from '../promotion.service';

@Component({
  selector: 'app-promotion-list',
  imports: [FormsModule],
  template: `
    <main class="page">
      <header class="page-header">
        <div>
          <p>Upravljanje cenama</p>
          <h1>Akcije</h1>
        </div>
        <button class="button primary" type="button" (click)="openCreate()">Dodaj akciju</button>
      </header>

      @if (formOpen()) {
        <section class="card form-card">
          <h2>{{ editId() ? 'Izmeni akciju' : 'Nova akcija' }}</h2>
          <form (ngSubmit)="save()">
            <div class="form-row">
              <label>Naziv</label>
              <input [(ngModel)]="form.name" name="name" required />
            </div>
            <div class="form-row">
              <label>Tip akcije</label>
              <select [(ngModel)]="form.promotionType" name="promotionType">
                <option value="PERCENTAGE">Procentualni popust</option>
              </select>
            </div>
            <div class="form-row">
              <label>Popust (%)</label>
              <input type="number" min="0" max="100" step="0.01" [(ngModel)]="form.discountPercentage" name="discountPercentage" required />
            </div>
            <div class="form-row">
              <label>Min. karata</label>
              <input type="number" min="1" [(ngModel)]="form.minTickets" name="minTickets" required />
            </div>
            <div class="form-row">
              <label>Promo kod</label>
              <input [(ngModel)]="form.promoCode" name="promoCode" placeholder="npr. STUDENT10" style="text-transform:uppercase" />
              <small style="color:#6b7280">Kupci unose ovaj kod pri placanju. Ostavite prazno ako ne zelite kod.</small>
            </div>
            <div class="form-row">
              <label>
                <input type="checkbox" [(ngModel)]="form.active" name="active" />
                Aktivna
              </label>
            </div>
            <div class="form-actions">
              <button type="submit" class="button primary">Sačuvaj</button>
              <button type="button" class="button" (click)="closeForm()">Otkaži</button>
            </div>
          </form>
        </section>
      }

      @if (promotions().length === 0) {
        <section class="empty-state">
          <h2>Nema akcija</h2>
          <p>Dodaj prvu akciju klikom na dugme iznad.</p>
        </section>
      } @else {
        <table>
          <thead>
            <tr>
              <th>Naziv</th>
              <th>Promo kod</th>
              <th>Popust</th>
              <th>Status</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            @for (promotion of promotions(); track promotion.id) {
              <tr>
                <td>{{ promotion.name }}</td>
                <td>{{ promotion.promoCode ?? '—' }}</td>
                <td>{{ promotion.discountPercentage + '%' }}</td>
                <td>
                  <span [class]="promotion.status === 'ACTIVE' ? 'badge active' : 'badge inactive'">
                    {{ promotion.status === 'ACTIVE' ? 'Aktivna' : 'Neaktivna' }}
                  </span>
                </td>
                <td class="actions">
                  <button type="button" (click)="openEdit(promotion)">Izmeni</button>
                  <button type="button" (click)="delete(promotion.id)">Obriši</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </main>
  `,
  styles: [`
    .form-card { margin-bottom: 1.5rem; padding: 1.5rem; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; }
    .form-row { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 1rem; }
    .form-row label { font-weight: 500; font-size: 0.875rem; }
    .form-row input[type="text"], .form-row input[type="number"] { padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 6px; font: inherit; }
    .form-actions { display: flex; gap: 0.75rem; margin-top: 0.5rem; }
    .badge { padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .badge.active { background: #d1fae5; color: #065f46; }
    .badge.inactive { background: #f3f4f6; color: #6b7280; }
  `]
})
export class PromotionListComponent implements OnInit {
  readonly promotions = signal<Promotion[]>([]);
  readonly formOpen = signal(false);
  readonly editId = signal<number | null>(null);

  form = this.emptyForm();

  constructor(private readonly promotionService: PromotionService) {}

  ngOnInit(): void {
    this.load();
  }

  openCreate(): void {
    this.editId.set(null);
    this.form = this.emptyForm();
    this.formOpen.set(true);
  }

  openEdit(promotion: Promotion): void {
    this.editId.set(promotion.id);
    this.form = {
      name: promotion.name,
      discountPercentage: promotion.discountPercentage,
      active: promotion.status === 'ACTIVE',
      minTickets: promotion.minTickets,
      promotionType: promotion.promotionType,
      promoCode: promotion.promoCode
    };
    this.formOpen.set(true);
  }

  closeForm(): void {
    this.formOpen.set(false);
  }

  save(): void {
    const id = this.editId();
    const obs = id
      ? this.promotionService.update(id, this.form)
      : this.promotionService.create(this.form);
    obs.subscribe(() => { this.closeForm(); this.load(); });
  }

  delete(id: number): void {
    if (window.confirm('Obrisati ovu akciju?')) {
      this.promotionService.delete(id).subscribe(() => this.load());
    }
  }

  private load(): void {
    this.promotionService.getAll().subscribe((p) => this.promotions.set(p));
  }

  private emptyForm() {
    return { name: '', discountPercentage: 0, active: true, minTickets: 1, promotionType: 'PERCENTAGE' as PromotionType, promoCode: null as string | null };
  }
}
