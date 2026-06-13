import { Component, EventEmitter, inject, Input, OnDestroy, OnChanges, OnInit, Output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { Trip, TripStatus } from '../../../../core/models/models';
import { TripsService } from '../../../../core/services/trips.service';

@Component({
  selector: 'app-basic-tab',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './basic-tab.component.html',
  styleUrl: './basic-tab.component.css'
})
export class BasicTabComponent implements OnInit, OnChanges, OnDestroy {
  @Input() trip: Trip | null = null;
  @Input() isNew = false;
  @Output() saved = new EventEmitter<Trip>();

  private fb = inject(FormBuilder);
  private tripsService = inject(TripsService);

  isSaving = signal(false);
  saveSuccess = signal(false);
  overlapError = signal<string | null>(null);

  private dateSub?: Subscription;

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    location: ['', Validators.required],
    purpose: ['', Validators.required],
    departureDate: ['', Validators.required],
    returnDate: [''],
    status: ['IN_PROCESSING' as TripStatus, Validators.required]
  });

  statuses = [
    { value: 'IN_PROCESSING', label: 'U fazi obrade' },
    { value: 'AWAITING_APPROVAL', label: 'Čeka odobrenje' },
    { value: 'CONFIRMED', label: 'Odobreno' }
  ];

  ngOnInit() {
    // Clear the overlap error as soon as the user edits either date.
    const reset = () => this.overlapError.set(null);
    this.dateSub = this.form.controls.departureDate.valueChanges.subscribe(reset);
    this.dateSub.add(this.form.controls.returnDate.valueChanges.subscribe(reset));
  }

  ngOnDestroy() {
    this.dateSub?.unsubscribe();
  }

  ngOnChanges() {
    if (this.trip) {
      this.form.patchValue({
        name: this.trip.name,
        location: this.trip.location,
        purpose: this.trip.purpose,
        departureDate: this.trip.departureDate,
        returnDate: this.trip.returnDate ?? '',
        status: this.trip.status
      });
    }
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.isSaving.set(true);
    this.overlapError.set(null);
    const val = this.form.getRawValue();
    const payload: Partial<Trip> = {
      name: val.name,
      location: val.location,
      purpose: val.purpose,
      departureDate: val.departureDate,
      returnDate: val.returnDate || undefined,
      status: val.status
    };

    const req = this.isNew
      ? this.tripsService.create(payload)
      : this.tripsService.update(this.trip!.id, payload);

    req.subscribe({
      next: (p) => {
        this.isSaving.set(false);
        this.saveSuccess.set(true);
        setTimeout(() => this.saveSuccess.set(false), 2000);
        this.saved.emit(p);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        if (err.status === 409) {
          const message = err.error?.message
            ?? (typeof err.error === 'string'
              ? err.error
              : 'Trip dates overlap with an existing trip.');
          this.overlapError.set(message);
        }
      }
    });
  }
}
