import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { PassengersService } from '../../../../core/services/passengers.service';
import { Putnik } from '../../../../core/models/models';

@Component({
  selector: 'app-passengers-tab',
  standalone: true,
  imports: [],
  templateUrl: './passengers-tab.component.html',
  styleUrl: './passengers-tab.component.css'
})
export class PassengersTabComponent implements OnInit {
  @Input() putovanjeId!: number;

  private passengersService = inject(PassengersService);

  putnici = signal<Putnik[]>([]);
  isSaving = signal(false);

  ngOnInit() { this.load(); }

  load() {
    this.passengersService.getPassengers(this.putovanjeId).subscribe({
      next: (list) => this.putnici.set(list),
      error: () => this.loadFromAll()
    });
  }

  loadFromAll() {
    this.passengersService.getAll().subscribe({
      next: (list) => this.putnici.set(list.map(p => ({ ...p, dodat: false }))),
      error: () => {}
    });
  }

  togglePassenger(id: number) {
    this.putnici.update(list =>
      list.map(p => p.id === id ? { ...p, dodat: !p.dodat } : p)
    );
  }

  toggleAll(event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    this.putnici.update(list => list.map(p => ({ ...p, dodat: checked })));
  }

  get allChecked(): boolean {
    return this.putnici().length > 0 && this.putnici().every(p => p.dodat);
  }

  save() {
    this.isSaving.set(true);
    const ids = this.putnici().filter(p => p.dodat).map(p => p.id);
    this.passengersService.save(this.putovanjeId, ids).subscribe({
      next: () => { this.isSaving.set(false); this.load(); },
      error: () => this.isSaving.set(false)
    });
  }

  statusClass(s: string): string {
    return s === 'KOMPLETNO' ? 'complete' : 'review';
  }
}
