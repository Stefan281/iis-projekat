import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TripsService } from '../../../core/services/trips.service';
import { Trip } from '../../../core/models/models';

@Component({
  selector: 'app-trips-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './trips-list.component.html',
  styleUrl: './trips-list.component.css'
})
export class TripsListComponent implements OnInit {
  private tripsService = inject(TripsService);
  trips = signal<Trip[]>([]);

  ngOnInit() {
    this.tripsService.getAll().subscribe({
      next: (list) => this.trips.set(
        [...list].sort((a, b) =>
          new Date(a.departureDate).getTime() - new Date(b.departureDate).getTime()
        )
      ),
      error: () => {}
    });
  }

  formatDateRange(p: Trip): string {
    const departure = new Date(p.departureDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    if (!p.returnDate) return departure;
    const ret = new Date(p.returnDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    return `${departure}–${ret}`;
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

  showWarning(p: Trip): boolean {
    const today = new Date(); today.setHours(0,0,0,0);
    const d = new Date(p.departureDate); d.setHours(0,0,0,0);
    const diff = Math.ceil((d.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    return diff < 30 && diff >= 0 && p.status !== 'CONFIRMED';
  }
}
