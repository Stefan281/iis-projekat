import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { OpponentPlayer, OpponentTeam } from '../opponent-teams/opponent-team.models';
import { MatchDetails, MatchEvent, MatchEventType, PlayerSelection } from './match-events.models';
import { MatchEventsService } from './match-events.service';

@Component({
  selector: 'app-match-events',
  imports: [DatePipe],
  templateUrl: './match-events.component.html',
  styleUrl: './match-events.component.css'
})
export class MatchEventsComponent {
  private readonly authService = inject(AuthService);
  private readonly matchEventsService = inject(MatchEventsService);

  readonly match = signal<MatchDetails | null>(null);
  readonly selectedEventType = signal<MatchEventType>('POINT');
  readonly selectedPlayer = signal<PlayerSelection | null>(null);
  readonly errorMessage = signal('');
  readonly isSaving = signal(false);

  readonly eventTypes = [
    { type: 'POINT' as MatchEventType, label: 'Poen', className: 'point' },
    { type: 'ERROR' as MatchEventType, label: 'Greska', className: 'error' },
    { type: 'SERVE' as MatchEventType, label: 'Servis', className: 'serve' },
    { type: 'ASSIST' as MatchEventType, label: 'Asistencija', className: 'assist' },
    { type: 'BLOCK' as MatchEventType, label: 'Blok', className: 'block' }
  ];

  readonly title = computed(() => {
    const match = this.match();
    return match ? `${match.homeTeam.name} vs ${match.awayTeam.name}` : 'Aktuelna utakmica';
  });

  constructor() {
    this.loadMatch();
  }

  selectPlayer(team: OpponentTeam, player: OpponentPlayer): void {
    this.selectedPlayer.set({ team, player });
    this.errorMessage.set('');
  }

  selectEventType(eventType: MatchEventType): void {
    this.selectedEventType.set(eventType);
    this.errorMessage.set('');
  }

  addEvent(): void {
    const match = this.match();
    const selectedPlayer = this.selectedPlayer();
    const currentUser = this.authService.currentUser();

    if (!match || !currentUser) {
      this.errorMessage.set('Nema aktivne utakmice ili korisnik nije prijavljen.');
      return;
    }

    if (!selectedPlayer?.player.id) {
      this.errorMessage.set('Izaberite igraca za dogadjaj.');
      return;
    }

    this.isSaving.set(true);
    this.matchEventsService.addEvent(match.id, {
      statisticianId: currentUser.id,
      primaryPlayerId: selectedPlayer.player.id,
      secondaryPlayerId: null,
      eventType: this.selectedEventType(),
      description: null
    }).subscribe({
      next: (createdEvent) => {
        this.match.update((currentMatch) => {
          if (!currentMatch) {
            return currentMatch;
          }

          return {
            ...currentMatch,
            events: [createdEvent, ...currentMatch.events].slice(0, 10)
          };
        });
        this.isSaving.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'Nije moguce dodati dogadjaj.');
        this.isSaving.set(false);
      }
    });
  }

  eventLabel(eventType: MatchEventType): string {
    return this.eventTypes.find((item) => item.type === eventType)?.label ?? eventType;
  }

  eventClass(event: MatchEvent): string {
    /*return event.eventType === 'ERROR' ? 'event-row bad' : 'event-row good';*/
    if (event.eventType === 'ERROR')
      return 'event-row error';
    else if(event.eventType === 'POINT')
      return 'event-row point';
    else if(event.eventType === 'SERVE')
      return 'event-row serve';
    else if(event.eventType === 'ASSIST')
      return 'event-row assist';
    else
      return 'event-row block';
  }

  trackPlayer(_: number, player: OpponentPlayer): number {
    return player.id ?? player.jerseyNumber;
  }

  private loadMatch(): void {
    this.matchEventsService.getCurrentMatch().subscribe({
      next: (match) => {
        this.match.set(match);
        const firstPlayer = match.homeTeam.players[0] ?? match.awayTeam.players[0];

        if (firstPlayer) {
          this.selectedPlayer.set({
            team: match.homeTeam.players[0] ? match.homeTeam : match.awayTeam,
            player: firstPlayer
          });
        }
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'Nije moguce ucitati aktivnu utakmicu.');
      }
    });
  }

  statusText(status: string): string {
  if (status === 'SCHEDULED') {
    return 'Zakazana';
  }

  if (status === 'IN_PROGRESS') {
    return 'U toku';
  }

  if (status === 'FINISHED') {
    return 'Završena';
  }
  
  return status;
  }
}
