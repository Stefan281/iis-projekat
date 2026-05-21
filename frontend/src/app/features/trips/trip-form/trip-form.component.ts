import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TripsService } from '../../../core/services/trips.service';
import { Putovanje } from '../../../core/models/models';
import { BasicTabComponent } from './basic-tab/basic-tab.component';
import { AccommodationTabComponent } from './accommodation-tab/accommodation-tab.component';
import { TransportTabComponent } from './transport-tab/transport-tab.component';
import { PassengersTabComponent } from './passengers-tab/passengers-tab.component';

type Tab = 'osnovno' | 'smestaj' | 'prevoz' | 'putnici';

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

  putovanjeId = signal<number | null>(null);
  putovanje = signal<Putovanje | null>(null);
  activeTab = signal<Tab>('osnovno');
  isNew = signal(false);

  tabs: { key: Tab; label: string }[] = [
    { key: 'osnovno', label: 'Osnovno' },
    { key: 'smestaj', label: 'Smeštaj' },
    { key: 'prevoz', label: 'Prevoz' },
    { key: 'putnici', label: 'Putnici' }
  ];

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id || id === 'novo') {
      this.isNew.set(true);
    } else {
      this.putovanjeId.set(+id);
      this.tripsService.getById(+id).subscribe({
        next: (p) => this.putovanje.set(p),
        error: () => {}
      });
    }
  }

  setTab(tab: Tab) {
    this.activeTab.set(tab);
  }

  onPutovanjeSaved(p: Putovanje) {
    this.putovanje.set(p);
    this.putovanjeId.set(p.id);
    this.isNew.set(false);
  }
}
