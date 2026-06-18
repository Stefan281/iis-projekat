import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransportService } from '../../../../core/services/transport.service';
import { TransportOffer } from '../../../../core/models/models';

@Component({
  selector: 'app-transport-tab',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './transport-tab.component.html',
  styleUrl: './transport-tab.component.css'
})
export class TransportTabComponent implements OnInit {
  @Input() tripId!: number;

  private transportService = inject(TransportService);
  private fb = inject(FormBuilder);

  offers = signal<TransportOffer[]>([]);
  selectedOfferId = signal<number | null>(null);
  showForm = signal(false);
  isSaving = signal(false);

  transportTypes = ['AUTOBUS', 'KOMBI', 'AVION', 'VOZ'];

  form = this.fb.nonNullable.group({
    carrierName: ['', Validators.required],
    transportType: ['AUTOBUS', Validators.required],
    price: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit() { this.load(); }

  load() {
    this.transportService.getOffers(this.tripId).subscribe({
      next: (list) => {
        this.offers.set(list);
        const selected = list.find(p => p.selected);
        if (selected) this.selectedOfferId.set(selected.id);
      },
      error: () => {}
    });
  }

  addOffer() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.isSaving.set(true);
    const val = this.form.getRawValue();
    this.transportService.addOffer(this.tripId, val).subscribe({
      next: () => { this.showForm.set(false); this.form.reset({ transportType: 'AUTOBUS' }); this.load(); this.isSaving.set(false); },
      error: () => this.isSaving.set(false)
    });
  }

  selectTransport() {
    if (!this.selectedOfferId()) return;
    this.transportService.select(this.tripId, this.selectedOfferId()!).subscribe({
      next: () => this.load(),
      error: () => {}
    });
  }

  typeIcon(type: string): string {
    const icons: Record<string, string> = { AUTOBUS: '🚌', KOMBI: '🚐', AVION: '✈️', VOZ: '🚆' };
    return icons[type] ?? '🚗';
  }

  get hasSelection() { return this.offers().some(p => p.selected); }
}
