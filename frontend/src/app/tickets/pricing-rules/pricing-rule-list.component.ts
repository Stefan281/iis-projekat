import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PricingRule, PricingRuleCondition, PricingRuleRequest } from '../ticketing.models';
import { PricingRuleService } from '../pricing-rule.service';

const CONDITION_LABELS: Record<PricingRuleCondition, string> = {
  DERBY: 'Derbi utakmica',
  HIGH_ATTRACTIVENESS: 'Visoka atraktivnost',
  LOW_ATTRACTIVENESS: 'Niska atraktivnost',
  HIGH_OCCUPANCY: 'Visoka popunjenost (≥80%)',
  MEDIUM_OCCUPANCY: 'Srednja popunjenost (50–80%)',
  LOW_OCCUPANCY: 'Niska popunjenost (<30%)',
  WEEKEND: 'Vikend',
  TODAY: 'Danas',
  EARLY_BIRD: 'Early bird (≥30 dana unapred)',
};

@Component({
  selector: 'app-pricing-rule-list',
  imports: [FormsModule],
  template: `
    <main class="page">
      <header class="page-header">
        <div>
          <p>Upravljanje cenama</p>
          <h1>Pravila cena</h1>
        </div>
        <button class="button primary" (click)="openCreate()">Dodaj pravilo</button>
      </header>

      @if (formOpen()) {
        <section class="card form-card">
          <h2>{{ editId() ? 'Izmeni pravilo' : 'Novo pravilo' }}</h2>
          <form (ngSubmit)="save()">
            <div class="form-row">
              <label>Naziv</label>
              <input [(ngModel)]="form.name" name="name" required />
            </div>
            <div class="form-row">
              <label>Opis</label>
              <input [(ngModel)]="form.description" name="description" />
            </div>
            <div class="form-row">
              <label>Uslov</label>
              <select [(ngModel)]="form.condition" name="condition" required>
                @for (c of conditions; track c.value) {
                  <option [value]="c.value">{{ c.label }}</option>
                }
              </select>
            </div>
            <div class="form-row">
              <label>Koeficijent</label>
              <input type="number" step="0.01" min="0.01" [(ngModel)]="form.coefficient" name="coefficient" required />
            </div>
            <div class="form-row">
              <label>Prioritet</label>
              <input type="number" min="0" [(ngModel)]="form.priority" name="priority" required />
            </div>
            <div class="form-row">
              <label>
                <input type="checkbox" [(ngModel)]="form.active" name="active" />
                Aktivno
              </label>
            </div>
            <div class="form-actions">
              <button type="submit" class="button primary">Sačuvaj</button>
              <button type="button" class="button" (click)="closeForm()">Otkaži</button>
            </div>
          </form>
        </section>
      }

      <table>
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Uslov</th>
            <th>Koeficijent</th>
            <th>Prioritet</th>
            <th>Status</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          @for (rule of rules(); track rule.id) {
            <tr>
              <td>{{ rule.name }}</td>
              <td>{{ conditionLabel(rule.condition) }}</td>
              <td>{{ rule.coefficient }}</td>
              <td>{{ rule.priority }}</td>
              <td>
                <span [class]="rule.active ? 'badge active' : 'badge inactive'">
                  {{ rule.active ? 'Aktivno' : 'Neaktivno' }}
                </span>
              </td>
              <td class="actions">
                <button type="button" (click)="openEdit(rule)">Izmeni</button>
                <button type="button" (click)="delete(rule.id)">Obriši</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </main>
  `,
  styles: [`
    .form-card { margin-bottom: 1.5rem; padding: 1.5rem; }
    .form-row { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 1rem; }
    .form-row label { font-weight: 500; font-size: 0.875rem; }
    .form-row input, .form-row select { padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 6px; }
    .form-actions { display: flex; gap: 0.75rem; margin-top: 0.5rem; }
    .badge { padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .badge.active { background: #d1fae5; color: #065f46; }
    .badge.inactive { background: #f3f4f6; color: #6b7280; }
  `]
})
export class PricingRuleListComponent implements OnInit {
  readonly rules = signal<PricingRule[]>([]);
  readonly formOpen = signal(false);
  readonly editId = signal<number | null>(null);

  form: PricingRuleRequest = this.emptyForm();

  readonly conditions = (Object.keys(CONDITION_LABELS) as PricingRuleCondition[]).map((k) => ({
    value: k,
    label: CONDITION_LABELS[k],
  }));

  constructor(private readonly service: PricingRuleService) {}

  ngOnInit(): void {
    this.load();
  }

  openCreate(): void {
    this.editId.set(null);
    this.form = this.emptyForm();
    this.formOpen.set(true);
  }

  openEdit(rule: PricingRule): void {
    this.editId.set(rule.id);
    this.form = { name: rule.name, description: rule.description, condition: rule.condition,
                  coefficient: rule.coefficient, active: rule.active, priority: rule.priority };
    this.formOpen.set(true);
  }

  closeForm(): void {
    this.formOpen.set(false);
  }

  save(): void {
    const id = this.editId();
    const obs = id ? this.service.update(id, this.form) : this.service.create(this.form);
    obs.subscribe(() => { this.closeForm(); this.load(); });
  }

  delete(id: number): void {
    this.service.delete(id).subscribe(() => this.load());
  }

  conditionLabel(c: PricingRuleCondition): string {
    return CONDITION_LABELS[c] ?? c;
  }

  private load(): void {
    this.service.getAll().subscribe((r) => this.rules.set(r));
  }

  private emptyForm(): PricingRuleRequest {
    return { name: '', description: '', condition: 'DERBY', coefficient: 1.0, active: true, priority: 0 };
  }
}
