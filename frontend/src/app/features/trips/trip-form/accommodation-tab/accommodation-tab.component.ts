import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccommodationService } from '../../../../core/services/accommodation.service';
import { PonudaSmestaja } from '../../../../core/models/models';

@Component({
  selector: 'app-accommodation-tab',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './accommodation-tab.component.html',
  styleUrl: './accommodation-tab.component.css'
})
export class AccommodationTabComponent implements OnInit {
  @Input() putovanjeId!: number;

  private accommodationService = inject(AccommodationService);
  private fb = inject(FormBuilder);

  ponude = signal<PonudaSmestaja[]>([]);
  selectedPonudaId = signal<number | null>(null);
  showForm = signal(false);
  isSaving = signal(false);

  form = this.fb.nonNullable.group({
    ime: ['', Validators.required],
    adresa: ['', Validators.required],
    cena: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit() { this.load(); }

  load() {
    this.accommodationService.getPonude(this.putovanjeId).subscribe({
      next: (list) => {
        this.ponude.set(list);
        const izabrana = list.find(p => p.izabran);
        if (izabrana) this.selectedPonudaId.set(izabrana.id);
      },
      error: () => {}
    });
  }

  addPonuda() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.isSaving.set(true);
    const val = this.form.getRawValue();
    this.accommodationService.addPonuda(this.putovanjeId, val).subscribe({
      next: () => { this.showForm.set(false); this.form.reset(); this.load(); this.isSaving.set(false); },
      error: () => this.isSaving.set(false)
    });
  }

  izaberiSmestaj() {
    if (!this.selectedPonudaId()) return;
    this.accommodationService.izaberi(this.putovanjeId, this.selectedPonudaId()!).subscribe({
      next: () => this.load(),
      error: () => {}
    });
  }

  get izabranaJedna() { return this.ponude().some(p => p.izabran); }
}
