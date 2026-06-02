import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Ticket } from '../ticketing.models';
import { TicketService } from '../ticket.service';

@Component({
  selector: 'app-my-tickets',
  imports: [RouterLink],
  template: `
    <main class="page">
      <header class="page-header">
        <div>
          <p>Kupac</p>
          <h1>Moje karte</h1>
        </div>
        <a class="button primary" routerLink="/matches">Pronadji utakmice</a>
      </header>

      @if (tickets().length === 0) {
        <section class="empty-state">
          <h2>Jos uvek nema kupljenih karata</h2>
          <p>Izaberi utakmicu i kupi prvu kartu.</p>
        </section>
      } @else {
        <section class="cards-grid">
          @for (ticket of tickets(); track ticket.id) {
            <article class="ticket-card">
              <div class="ticket-card__head">
                <span>{{ ticket.status }}</span>
                <strong>#{{ ticket.id }}</strong>
              </div>
              <h2>{{ ticket.homeTeam }} vs {{ ticket.awayTeam }}</h2>
              <p>{{ ticket.matchDate }} u {{ ticket.matchTime }}</p>
              <dl>
                <div><dt>Lokacija</dt><dd>{{ ticket.location }}</dd></div>
                <div><dt>Sediste</dt><dd>{{ ticket.zoneName }}, red {{ ticket.rowLabel }}, sediste {{ ticket.seatNumber }}</dd></div>
                <div><dt>Placeno</dt><dd>{{ ticket.price }} RSD</dd></div>
              </dl>
            </article>
          }
        </section>
      }
    </main>
  `
})
export class MyTicketsComponent implements OnInit {
  readonly tickets = signal<Ticket[]>([]);

  constructor(private readonly ticketService: TicketService) {}

  ngOnInit(): void {
    this.ticketService.getMyTickets().subscribe((tickets) => this.tickets.set(tickets));
  }
}
