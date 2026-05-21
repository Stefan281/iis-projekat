import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransportService } from '../../../../core/services/transport.service';
import { PonudaTransporta } from '../../../../core/models/models';

@Component({
  selector: 'app-transport-tab',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './transport-tab.component.html',
  styleUrl: './transport-tab.component.css'
})
export class TransportTabComponent implements OnInit {
  @Input() putovanjeId!: number;

  private transportService = inject(TransportService);
  private fb = inject(FormBuilder);

  ponude = signal<PonudaTransporta[]>([]);
  selectedPonudaId = signal<number | null>(null);
  showForm = signal(false);
  isSaving = signal(false);

  vrstePrevoze = ['AUTOBUS', 'KOMBI', 'AVION', 'VOZ'];

  form = this.fb.nonNullable.group({
    naziv: ['', Validators.required],
    vrsta: ['AUTOBUS', Validators.required],
    cena: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit() { this.load(); }

  load() {
    this.transportService.getPonude(this.putovanjeId).subscribe({
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
    this.transportService.addPonuda(this.putovanjeId, val).subscribe({
      next: () => { this.showForm.set(false); this.form.reset({ vrsta: 'AUTOBUS' }); this.load(); this.isSaving.set(false); },
      error: () => this.isSaving.set(false)
    });
  }

  izaberiPrevoz() {
    if (!this.selectedPonudaId()) return;
    this.transportService.izaberi(this.putovanjeId, this.selectedPonudaId()!).subscribe({
      next: () => this.load(),
      error: () => {}
    });
  }

  vrstaIcon(vrsta: string): string {
    const icons: Record<string, string> = { AUTOBUS: '🚌', KOMBI: '🚐', AVION: '✈️', VOZ: '🚆' };
    return icons[vrsta] ?? '🚗';
  }

  get izabranaJedna() { return this.ponude().some(p => p.izabran); }
}
