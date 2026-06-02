import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { Match, Reservation } from '../tickets/ticketing.models';
import { MatchService } from '../tickets/match.service';
import { ReservationService } from '../tickets/reservation.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly matchService = inject(MatchService);
  private readonly reservationService = inject(ReservationService);

  readonly user = this.authService.currentUser;
  readonly matches = signal<Match[]>([]);
  readonly reservations = signal<Reservation[]>([]);
  readonly role = computed(() => this.user()?.role);

  readonly customerMatches = computed(() => this.matches().slice(0, 2));
  readonly activeReservations = computed(() => this.reservations().filter((reservation) => reservation.status === 'ACTIVE').slice(0, 2));

  ngOnInit(): void {
    this.matchService.getAll().subscribe({
      next: (matches) => this.matches.set(matches),
      error: () => this.matches.set([])
    });

    if (this.role() === 'CUSTOMER') {
      this.reservationService.getMyReservations().subscribe({
        next: (reservations) => this.reservations.set(reservations),
        error: () => this.reservations.set([])
      });
    }
  }
}
