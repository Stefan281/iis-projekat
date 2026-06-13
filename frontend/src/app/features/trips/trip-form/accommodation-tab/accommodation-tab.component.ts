import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccommodationService } from '../../../../core/services/accommodation.service';
import { AccommodationOffer } from '../../../../core/models/models';

@Component({
  selector: 'app-accommodation-tab',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './accommodation-tab.component.html',
  styleUrl: './accommodation-tab.component.css'
})
export class AccommodationTabComponent implements OnInit {
  @Input() tripId!: number;

  private accommodationService = inject(AccommodationService);
  private fb = inject(FormBuilder);

  offers = signal<AccommodationOffer[]>([]);
  selectedOfferId = signal<number | null>(null);
  showForm = signal(false);
  isSaving = signal(false);

  form = this.fb.nonNullable.group({
    ime: ['', Validators.required],
    adresa: ['', Validators.required],
    cena: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit() { this.load(); }

  load() {
    this.accommodationService.getOffers(this.tripId).subscribe({
      next: (list) => {
        this.offers.set(list);
        const selected = list.find(p => p.izabran);
        if (selected) this.selectedOfferId.set(selected.id);
      },
      error: () => {}
    });
  }

  addOffer() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.isSaving.set(true);
    const val = this.form.getRawValue();
    this.accommodationService.addOffer(this.tripId, val).subscribe({
      next: () => { this.showForm.set(false); this.form.reset(); this.load(); this.isSaving.set(false); },
      error: () => this.isSaving.set(false)
    });
  }

  selectAccommodation() {
    if (!this.selectedOfferId()) return;
    this.accommodationService.select(this.tripId, this.selectedOfferId()!).subscribe({
      next: () => this.load(),
      error: () => {}
    });
  }

  get hasSelection() { return this.offers().some(p => p.izabran); }
}
