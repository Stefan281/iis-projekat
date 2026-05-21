import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TripsService } from '../../../core/services/trips.service';
import { Putovanje } from '../../../core/models/models';

@Component({
  selector: 'app-trips-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './trips-list.component.html',
  styleUrl: './trips-list.component.css'
})
export class TripsListComponent implements OnInit {
  private tripsService = inject(TripsService);
  putovanja = signal<Putovanje[]>([]);

  ngOnInit() {
    this.tripsService.getAll().subscribe({
      next: (list) => this.putovanja.set(list),
      error: () => {}
    });
  }

  formatDateRange(p: Putovanje): string {
    const pol = new Date(p.departureDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    if (!p.returnDate) return pol;
    const pov = new Date(p.returnDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    return `${pol}–${pov}`;
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      IN_PROCESSING: 'U fazi obrade',
      AWAITING_APPROVAL: 'Čeka odobrenje',
      CONFIRMED: 'Odobreno',
      REJECTED: 'Odbijeno',
      COMPLETED: 'Završeno'
    };
    return map[status] ?? status;
  }

  showWarning(p: Putovanje): boolean {
    const today = new Date(); today.setHours(0,0,0,0);
    const d = new Date(p.departureDate); d.setHours(0,0,0,0);
    const diff = Math.ceil((d.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    return diff < 30 && diff >= 0 && p.status !== 'CONFIRMED';
  }
}
