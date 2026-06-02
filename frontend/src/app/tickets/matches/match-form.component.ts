import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatchService } from '../match.service';
import { MatchAttractiveness, MatchRequest, MatchStatus } from '../ticketing.models';

@Component({
  selector: 'app-match-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="page narrow">
      <header class="page-header">
        <div>
          <p>Utakmica</p>
          <h1>{{ isEditMode() ? 'Izmena utakmice' : 'Dodavanje utakmice' }}</h1>
        </div>
        <a class="button" routerLink="/matches">Nazad</a>
      </header>

      <form [formGroup]="form" (ngSubmit)="save()" class="entity-form">
        <label>Datum <input type="date" formControlName="date" /></label>
        <label>Vreme <input type="time" formControlName="time" /></label>
        <label>Domaci tim <input type="text" formControlName="homeTeam" /></label>
        <label>Gostujuci tim <input type="text" formControlName="awayTeam" /></label>
        <label>Lokacija <input type="text" formControlName="location" /></label>
        <label>Status
          <select formControlName="status">
            <option value="SCHEDULED">Zakazana</option>
            <option value="CANCELLED">Otkazana</option>
            <option value="FINISHED">Zavrsena</option>
          </select>
        </label>
        <label>Osnovna cena <input type="number" min="0" formControlName="basePrice" /></label>
        <label>Atraktivnost
          <select formControlName="attractiveness">
            <option value="LOW">Niska</option>
            <option value="MEDIUM">Srednja</option>
            <option value="HIGH">Visoka</option>
            <option value="DERBY">Derbi</option>
          </select>
        </label>

        @if (error()) {
          <p class="form-error">{{ error() }}</p>
        }

        <button class="button primary" type="submit" [disabled]="form.invalid">
          {{ isEditMode() ? 'Sacuvaj izmene' : 'Dodaj utakmicu' }}
        </button>
      </form>
    </main>
  `
})
export class MatchFormComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  readonly isEditMode = signal(false);
  readonly error = signal('');
  private matchId: number | null = null;

  readonly form = this.formBuilder.nonNullable.group({
    date: ['', Validators.required],
    time: ['', Validators.required],
    homeTeam: ['', Validators.required],
    awayTeam: ['', Validators.required],
    location: ['', Validators.required],
    status: ['SCHEDULED' as MatchStatus, Validators.required],
    basePrice: [0, [Validators.required, Validators.min(0)]],
    attractiveness: ['MEDIUM' as MatchAttractiveness, Validators.required]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly matchService: MatchService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.matchId = Number(id);
    this.isEditMode.set(true);
    this.matchService.getById(this.matchId).subscribe((match) => {
      this.form.patchValue({ ...match, time: match.time.slice(0, 5) });
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request: MatchRequest = {
      ...this.form.getRawValue(),
      expectedAttendance: 0
    };
    const operation = this.matchId
      ? this.matchService.update(this.matchId, request)
      : this.matchService.create(request);

    operation.subscribe({
      next: () => this.router.navigateByUrl('/matches'),
      error: () => this.error.set('Utakmica nije sacuvana. Proveri podatke i pokusaj ponovo.')
    });
  }
}
