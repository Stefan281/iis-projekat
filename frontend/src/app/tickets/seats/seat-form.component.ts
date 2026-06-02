import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SeatService } from '../seat.service';
import { ZoneService } from '../zone.service';
import { SeatStatus, Zone } from '../ticketing.models';

@Component({
  selector: 'app-seat-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="page narrow">
      <header class="page-header">
        <div>
          <p>Sediste</p>
          <h1>{{ isEditMode() ? 'Izmena sedista' : 'Dodavanje sedista' }}</h1>
        </div>
        <a class="button" routerLink="/seats">Nazad</a>
      </header>

      <form [formGroup]="form" (ngSubmit)="save()" class="entity-form">
        <label>Red <input type="text" formControlName="rowLabel" /></label>
        <label>Broj sedista <input type="number" min="1" formControlName="seatNumber" /></label>
        <label>Status
          <select formControlName="status">
            <option value="AVAILABLE">Slobodno</option>
            <option value="RESERVED">Rezervisano</option>
            <option value="SOLD">Prodato</option>
            <option value="BLOCKED">Blokirano</option>
          </select>
        </label>
        <label>Zona
          <select formControlName="zoneId">
            @for (zone of zones(); track zone.id) {
              <option [ngValue]="zone.id">{{ zone.name }}</option>
            }
          </select>
        </label>

        @if (error()) {
          <p class="form-error">{{ error() }}</p>
        }

        <button class="button primary" type="submit" [disabled]="form.invalid || zones().length === 0">
          {{ isEditMode() ? 'Sacuvaj izmene' : 'Dodaj sediste' }}
        </button>
      </form>
    </main>
  `
})
export class SeatFormComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  readonly isEditMode = signal(false);
  readonly zones = signal<Zone[]>([]);
  readonly error = signal('');
  private seatId: number | null = null;

  readonly form = this.formBuilder.nonNullable.group({
    rowLabel: ['', Validators.required],
    seatNumber: [1, [Validators.required, Validators.min(1)]],
    status: ['AVAILABLE' as SeatStatus, Validators.required],
    zoneId: [0, [Validators.required, Validators.min(1)]]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly seatService: SeatService,
    private readonly zoneService: ZoneService
  ) {}

  ngOnInit(): void {
    this.zoneService.getAll().subscribe((zones) => {
      this.zones.set(zones);
      if (!this.form.controls.zoneId.value && zones.length > 0) {
        this.form.controls.zoneId.setValue(zones[0].id);
      }
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.seatId = Number(id);
    this.isEditMode.set(true);
    this.seatService.getById(this.seatId).subscribe((seat) => this.form.patchValue(seat));
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request = this.form.getRawValue();
    const operation = this.seatId
      ? this.seatService.update(this.seatId, request)
      : this.seatService.create(request);

    operation.subscribe({
      next: () => this.router.navigateByUrl('/seats'),
      error: (error) => this.error.set(this.errorMessage(error))
    });
  }

  private errorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && typeof error.error?.message === 'string') {
      return error.error.message;
    }

    return 'Sediste nije sacuvano. Proveri red, broj sedista i zonu.';
  }
}
