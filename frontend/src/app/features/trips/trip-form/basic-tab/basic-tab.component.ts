import { Component, EventEmitter, inject, Input, OnChanges, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Putovanje, PutovanjeStatus } from '../../../../core/models/models';
import { TripsService } from '../../../../core/services/trips.service';

@Component({
  selector: 'app-basic-tab',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './basic-tab.component.html',
  styleUrl: './basic-tab.component.css'
})
export class BasicTabComponent implements OnChanges {
  @Input() putovanje: Putovanje | null = null;
  @Input() isNew = false;
  @Output() saved = new EventEmitter<Putovanje>();

  private fb = inject(FormBuilder);
  private tripsService = inject(TripsService);

  isSaving = signal(false);
  saveSuccess = signal(false);

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    location: ['', Validators.required],
    purpose: ['', Validators.required],
    departureDate: ['', Validators.required],
    returnDate: [''],
    status: ['IN_PROCESSING' as PutovanjeStatus, Validators.required]
  });

  statusi = [
    { value: 'IN_PROCESSING', label: 'U fazi obrade' },
    { value: 'AWAITING_APPROVAL', label: 'Čeka odobrenje' },
    { value: 'CONFIRMED', label: 'Odobreno' }
  ];

  ngOnChanges() {
    if (this.putovanje) {
      this.form.patchValue({
        name: this.putovanje.name,
        location: this.putovanje.location,
        purpose: this.putovanje.purpose,
        departureDate: this.putovanje.departureDate,
        returnDate: this.putovanje.returnDate ?? '',
        status: this.putovanje.status
      });
    }
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.isSaving.set(true);
    const val = this.form.getRawValue();
    const payload: Partial<Putovanje> = {
      name: val.name,
      location: val.location,
      purpose: val.purpose,
      departureDate: val.departureDate,
      returnDate: val.returnDate || undefined,
      status: val.status
    };

    const req = this.isNew
      ? this.tripsService.create(payload)
      : this.tripsService.update(this.putovanje!.id, payload);

    req.subscribe({
      next: (p) => {
        this.isSaving.set(false);
        this.saveSuccess.set(true);
        setTimeout(() => this.saveSuccess.set(false), 2000);
        this.saved.emit(p);
      },
      error: () => this.isSaving.set(false)
    });
  }
}
