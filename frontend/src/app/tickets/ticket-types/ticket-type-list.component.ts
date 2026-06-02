import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TicketType } from '../ticketing.models';
import { TicketTypeService } from '../ticket-type.service';

@Component({
  selector: 'app-ticket-type-list',
  imports: [RouterLink],
  template: `
    <main class="page">
      <header class="page-header">
        <div>
          <p>Ticket sales</p>
          <h1>Ticket types</h1>
        </div>
        <a class="button primary" routerLink="/ticket-types/new">Add ticket type</a>
      </header>

      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Coefficient</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          @for (ticketType of ticketTypes(); track ticketType.id) {
            <tr>
              <td>{{ ticketType.name }}</td>
              <td>{{ ticketType.description }}</td>
              <td>{{ ticketType.coefficient }}</td>
              <td class="actions">
                <a routerLink="/ticket-types/{{ ticketType.id }}/edit">Edit</a>
                <button type="button" (click)="delete(ticketType.id)">Delete</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </main>
  `
})
export class TicketTypeListComponent implements OnInit {
  readonly ticketTypes = signal<TicketType[]>([]);

  constructor(private readonly ticketTypeService: TicketTypeService) {}

  ngOnInit(): void {
    this.load();
  }

  delete(id: number): void {
    this.ticketTypeService.delete(id).subscribe(() => this.load());
  }

  private load(): void {
    this.ticketTypeService.getAll().subscribe((ticketTypes) => this.ticketTypes.set(ticketTypes));
  }
}
