import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TripsService } from '../../../core/services/trips.service';
import { Trip } from '../../../core/models/models';
import { BasicTabComponent } from './basic-tab/basic-tab.component';
import { AccommodationTabComponent } from './accommodation-tab/accommodation-tab.component';
import { TransportTabComponent } from './transport-tab/transport-tab.component';
import { PassengersTabComponent } from './passengers-tab/passengers-tab.component';

type Tab = 'basic' | 'accommodation' | 'transport' | 'passengers';

@Component({
  selector: 'app-trip-form',
  standalone: true,
  imports: [RouterLink, BasicTabComponent, AccommodationTabComponent, TransportTabComponent, PassengersTabComponent],
  templateUrl: './trip-form.component.html',
  styleUrl: './trip-form.component.css'
})
export class TripFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private tripsService = inject(TripsService);

  tripId = signal<number | null>(null);
  trip = signal<Trip | null>(null);
  activeTab = signal<Tab>('basic');
  isNew = signal(false);

  tabs: { key: Tab; label: string }[] = [
    { key: 'basic', label: 'Osnovno' },
    { key: 'accommodation', label: 'Smeštaj' },
    { key: 'transport', label: 'Prevoz' },
    { key: 'passengers', label: 'Putnici' }
  ];

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id || id === 'novo') {
      this.isNew.set(true);
    } else {
      this.tripId.set(+id);
      this.tripsService.getById(+id).subscribe({
        next: (p) => this.trip.set(p),
        error: () => {}
      });
    }
  }

  setTab(tab: Tab) {
    this.activeTab.set(tab);
  }

  onTripSaved(p: Trip) {
    this.trip.set(p);
    this.tripId.set(p.id);
    this.isNew.set(false);
  }
}
