import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { UserRole } from '../auth/auth.models';

interface PlayerResponse {
  id: number;
  firstName: string;
  lastName: string;
  birthYear: number | null;
  height: number | null;
  position: string | null;
  club: string | null;
  note: string | null;
}

interface PlayerSummary {
  players: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly user = this.authService.currentUser;
  readonly backendMessage = signal('');
  readonly playerMessage = signal('');
  readonly playerError = signal('');
  readonly isSavingPlayer = signal(false);
  readonly activeView = signal<'dashboard' | 'players' | 'add-player' | 'player-details' | 'edit-player'>('dashboard');
  readonly playerCount = signal(0);
  readonly players = signal<PlayerResponse[]>([]);
  readonly selectedPlayer = signal<PlayerResponse | null>(null);
  readonly searchTerm = signal('');
  readonly positionFilter = signal('');
  readonly clubFilter = signal('');
  readonly sortBy = signal<'newest' | 'oldest' | 'name' | 'position' | 'club'>('newest');

  readonly navItems = [
    { label: 'Dashboard', icon: 'D' },
    { label: 'Igraci', icon: 'I' },
    { label: 'Posmatranja', icon: 'P' },
    { label: 'Performanse', icon: 'F' },
    { label: 'Metrike', icon: 'M' }
  ];

  readonly recentPlayers = computed(() => this.players().slice().sort((a, b) => b.id - a.id).slice(0, 5));
  readonly positions = computed(() => this.uniqueValues(this.players().map((player) => player.position)));
  readonly clubs = computed(() => this.uniqueValues(this.players().map((player) => player.club)));
  readonly filteredPlayers = computed(() => {
    var search = this.searchTerm().trim().toLowerCase();
    var position = this.positionFilter();
    var club = this.clubFilter();

    return this.players()
      .filter((player) => {
        var fullName = `${player.firstName} ${player.lastName}`.toLowerCase();
        var matchesSearch = !search || fullName.includes(search);
        var matchesPosition = !position || player.position === position;
        var matchesClub = !club || player.club === club;

        return matchesSearch && matchesPosition && matchesClub;
      })
      .slice()
      .sort((first, second) => this.comparePlayers(first, second));
  });

  readonly playerForm = this.formBuilder.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    birthYear: [null as number | null, [Validators.required, Validators.min(1950), Validators.max(2020)]],
    height: [null as number | null, [Validators.required, Validators.min(120), Validators.max(230)]],
    position: ['', Validators.required],
    club: ['', Validators.required],
    note: ['']
  });

  constructor() {
    this.http.get('http://localhost:8080/api/test', { responseType: 'text' }).subscribe({
      next: (message) => this.backendMessage.set(message),
      error: () => this.backendMessage.set('Backend nije dostupan ili token nije validan.')
    });
    this.loadPlayerOverview();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }

  showAddPlayer(): void {
    if (!this.isScout) {
      return;
    }

    this.resetPlayerForm();
    this.selectedPlayer.set(null);
    this.playerMessage.set('');
    this.playerError.set('');
    this.activeView.set('add-player');
  }

  showDashboard(): void {
    this.activeView.set('dashboard');
  }

  showPlayers(): void {
    this.playerMessage.set('');
    this.playerError.set('');
    this.activeView.set('players');
    this.loadPlayerOverview();
  }

  showPlayerDetails(player: PlayerResponse): void {
    this.selectedPlayer.set(player);
    this.activeView.set('player-details');
  }

  showEditPlayer(player: PlayerResponse): void {
    if (!this.isScout) {
      return;
    }

    this.selectedPlayer.set(player);
    this.playerMessage.set('');
    this.playerError.set('');
    this.playerForm.reset({
      firstName: player.firstName,
      lastName: player.lastName,
      birthYear: player.birthYear,
      height: player.height,
      position: player.position || '',
      club: player.club || '',
      note: player.note || ''
    });
    this.activeView.set('edit-player');
  }

  savePlayer(): void {
    if (this.playerForm.invalid) {
      this.playerForm.markAllAsTouched();
      return;
    }

    this.playerMessage.set('');
    this.playerError.set('');
    this.isSavingPlayer.set(true);

    var selectedPlayer = this.selectedPlayer();
    var request = this.activeView() === 'edit-player' && selectedPlayer
      ? this.http.put<PlayerResponse>(`http://localhost:8080/api/players/${selectedPlayer.id}`, this.playerForm.getRawValue())
      : this.http.post<PlayerResponse>('http://localhost:8080/api/players', this.playerForm.getRawValue());

    request.subscribe({
      next: (savedPlayer) => {
        this.resetPlayerForm();
        this.selectedPlayer.set(savedPlayer);
        this.isSavingPlayer.set(false);
        this.playerMessage.set('Igrac je sacuvan u bazi.');
        this.loadPlayerOverview();
        this.activeView.set('players');
      },
      error: () => {
        this.isSavingPlayer.set(false);
        this.playerError.set('Igrac nije sacuvan. Proveri podatke i da li je backend pokrenut.');
      }
    });
  }

  get isScout(): boolean {
    return this.user()?.role === 'SKAUT';
  }

  get stats() {
    return [
      { label: 'Igraci', value: this.playerCount() },
      { label: 'Posmatranja', value: 0 },
      { label: 'Procene', value: 0 }
    ];
  }

  isActiveNav(label: string): boolean {
    if (['players', 'add-player', 'player-details', 'edit-player'].includes(this.activeView())) {
      return label === 'Igraci';
    }

    return label === 'Dashboard';
  }

  handleNav(label: string): void {
    if (label === 'Dashboard') {
      this.showDashboard();
      return;
    }

    if (label === 'Igraci') {
      this.showPlayers();
    }
  }

  updateSearch(value: string): void {
    this.searchTerm.set(value);
  }

  updatePosition(value: string): void {
    this.positionFilter.set(value);
  }

  updateClub(value: string): void {
    this.clubFilter.set(value);
  }

  updateSort(value: string): void {
    this.sortBy.set(value as 'newest' | 'oldest' | 'name' | 'position' | 'club');
  }

  get roleTitle(): string {
    var role = this.user()?.role;

    if (!role) {
      return 'Scouting igraca';
    }

    var titles: Record<UserRole, string> = {
      ADMIN: 'Administracija',
      SKAUT: 'Scouting igraca',
      STATISTICAR: 'Statistika',
      STRUCNI_STAB: 'Strucni stab',
      SPORTSKI_DIREKTOR: 'Sportski direktor'
    };

    return titles[role];
  }

  private loadPlayerOverview(): void {
    this.http.get<PlayerResponse[]>('http://localhost:8080/api/players').subscribe({
      next: (players) => {
        this.players.set(players);
        this.playerCount.set(players.length);
      },
      error: () => {
        this.players.set([]);
        this.playerCount.set(0);
      }
    });

    this.http.get<PlayerSummary>('http://localhost:8080/api/players/summary').subscribe({
      next: (summary) => this.playerCount.set(summary.players),
      error: () => this.playerCount.set(0)
    });
  }

  private resetPlayerForm(): void {
    this.playerForm.reset({
      firstName: '',
      lastName: '',
      birthYear: null,
      height: null,
      position: '',
      club: '',
      note: ''
    });
  }

  private uniqueValues(values: Array<string | null>): string[] {
    return values
      .filter((value): value is string => !!value)
      .filter((value, index, array) => array.indexOf(value) === index)
      .sort((first, second) => first.localeCompare(second));
  }

  private comparePlayers(first: PlayerResponse, second: PlayerResponse): number {
    if (this.sortBy() === 'oldest') {
      return first.id - second.id;
    }

    if (this.sortBy() === 'name') {
      return `${first.firstName} ${first.lastName}`.localeCompare(`${second.firstName} ${second.lastName}`);
    }

    if (this.sortBy() === 'position') {
      return (first.position || '').localeCompare(second.position || '');
    }

    if (this.sortBy() === 'club') {
      return (first.club || '').localeCompare(second.club || '');
    }

    return second.id - first.id;
  }
}
