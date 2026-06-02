import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ZoneService } from '../zone.service';
import { Zone } from '../ticketing.models';

@Component({
  selector: 'app-zone-list',
  imports: [RouterLink],
  template: `
    <main class="page">
      <header class="page-header">
        <div>
          <p>Prodaja karata</p>
          <h1>Zone</h1>
        </div>
        <a class="button primary" routerLink="/zones/new">Dodaj zonu</a>
      </header>

      <table>
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Opis</th>
            <th>Koeficijent cene</th>
            <th>Kapacitet</th>
            <th>Zauzeta sedista</th>
            <th>Popunjenost</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          @for (zone of zones(); track zone.id) {
            <tr>
              <td>{{ zone.name }}</td>
              <td>{{ zone.description }}</td>
              <td>{{ zone.priceCoefficient }}</td>
              <td>{{ zone.capacity }}</td>
              <td>{{ zone.occupiedSeats }}</td>
              <td>{{ zone.occupancyRate }}%</td>
              <td class="actions">
                <button type="button" routerLink="/zones/{{ zone.id }}/edit">Izmeni</button>
                <button type="button" (click)="delete(zone.id)">Obrisi</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </main>
  `
})
export class ZoneListComponent implements OnInit {
  readonly zones = signal<Zone[]>([]);

  constructor(private readonly zoneService: ZoneService) {}

  ngOnInit(): void {
    this.load();
  }

  delete(id: number): void {
    const confirmed = window.confirm('Da li si siguran da zelis da obrises zonu?');
    if (!confirmed) {
      return;
    }

    this.zoneService.delete(id).subscribe(() => this.load());
  }

  private load(): void {
    this.zoneService.getAll().subscribe((zones) => this.zones.set(zones));
  }
}
