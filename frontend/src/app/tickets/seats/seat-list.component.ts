import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Seat, Zone } from '../ticketing.models';
import { SeatService } from '../seat.service';
import { ZoneService } from '../zone.service';

@Component({
  selector: 'app-seat-list',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="page">
      <header class="page-header">
        <div>
          <p>Prodaja karata</p>
          <h1>Sedista</h1>
        </div>
        <a class="button primary" routerLink="/seats/new">Dodaj sediste</a>
      </header>

      <div class="toolbar">
        <label>
          Filtriraj po zoni
          <select [(ngModel)]="selectedZoneId" (change)="load()">
            <option [ngValue]="null">Sve zone</option>
            @for (zone of zones(); track zone.id) {
              <option [ngValue]="zone.id">{{ zone.name }}</option>
            }
          </select>
        </label>
      </div>

      <table>
        <thead>
          <tr>
            <th>Red</th>
            <th>Broj sedista</th>
            <th>Status</th>
            <th>Zona</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          @for (seat of seats(); track seat.id) {
            <tr>
              <td>{{ seat.rowLabel }}</td>
              <td>{{ seat.seatNumber }}</td>
              <td>{{ statusLabel(seat.status) }}</td>
              <td>{{ seat.zoneName }}</td>
              <td class="actions">
                <button type="button" routerLink="/seats/{{ seat.id }}/edit">Izmeni</button>
                <button type="button" (click)="delete(seat.id)">Obrisi</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </main>
  `
})
export class SeatListComponent implements OnInit {
  readonly seats = signal<Seat[]>([]);
  readonly zones = signal<Zone[]>([]);
  selectedZoneId: number | null = null;

  constructor(
    private readonly seatService: SeatService,
    private readonly zoneService: ZoneService
  ) {}

  ngOnInit(): void {
    this.zoneService.getAll().subscribe((zones) => this.zones.set(zones));
    this.load();
  }

  load(): void {
    this.seatService.getAll(this.selectedZoneId ?? undefined).subscribe((seats) => this.seats.set(seats));
  }

  delete(id: number): void {
    const confirmed = window.confirm('Da li si siguran da zelis da obrises sediste?');
    if (!confirmed) {
      return;
    }

    this.seatService.delete(id).subscribe(() => this.load());
  }

  statusLabel(status: Seat['status']): string {
    const labels: Record<Seat['status'], string> = {
      AVAILABLE: 'Slobodno',
      RESERVED: 'Rezervisano',
      SOLD: 'Prodato',
      BLOCKED: 'Blokirano'
    };
    return labels[status];
  }
}
