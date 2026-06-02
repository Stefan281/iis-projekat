import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ZoneService } from '../zone.service';

@Component({
  selector: 'app-zone-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="page narrow">
      <header class="page-header">
        <div>
          <p>Zona</p>
          <h1>{{ isEditMode() ? 'Izmena zone' : 'Dodavanje zone' }}</h1>
        </div>
        <a class="button" routerLink="/zones">Nazad</a>
      </header>

      <form [formGroup]="form" (ngSubmit)="save()" class="entity-form">
        <label>Naziv <input type="text" formControlName="name" /></label>
        <label>Opis <textarea formControlName="description"></textarea></label>
        <label>Koeficijent cene <input type="number" min="0" step="0.01" formControlName="priceCoefficient" /></label>
        <label>Kapacitet <input type="number" min="1" formControlName="capacity" /></label>

        @if (error()) {
          <p class="form-error">{{ error() }}</p>
        }

        <button class="button primary" type="submit" [disabled]="form.invalid">
          {{ isEditMode() ? 'Sacuvaj izmene' : 'Dodaj zonu' }}
        </button>
      </form>
    </main>
  `
})
export class ZoneFormComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  readonly isEditMode = signal(false);
  readonly error = signal('');
  private zoneId: number | null = null;

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    priceCoefficient: [1, [Validators.required, Validators.min(0)]],
    capacity: [1, [Validators.required, Validators.min(1)]]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly zoneService: ZoneService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.zoneId = Number(id);
    this.isEditMode.set(true);
    this.zoneService.getById(this.zoneId).subscribe((zone) => this.form.patchValue(zone));
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request = this.form.getRawValue();
    const operation = this.zoneId
      ? this.zoneService.update(this.zoneId, request)
      : this.zoneService.create(request);

    operation.subscribe({
      next: () => this.router.navigateByUrl('/zones'),
      error: () => this.error.set('Zona nije sacuvana. Proveri naziv, koeficijent i kapacitet.')
    });
  }
}
