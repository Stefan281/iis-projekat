import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { OpponentPlayer, OpponentTeam } from './opponent-team.models';
import { OpponentTeamsService } from './opponent-teams.service';

type ViewMode = 'view' | 'create' | 'edit';
type TeamType = 'HOME' | 'OPPONENT';

@Component({
  selector: 'app-opponent-teams',
  imports: [ReactiveFormsModule],
  templateUrl: './opponent-teams.component.html',
  styleUrl: './opponent-teams.component.css'
})
export class OpponentTeamsComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly opponentTeamsService = inject(OpponentTeamsService);

  readonly teams = signal<OpponentTeam[]>([]);
  readonly selectedTeam = signal<OpponentTeam | null>(null);
  readonly activeTeamType = signal<TeamType>('OPPONENT');
  readonly mode = signal<ViewMode>('view');
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');

  readonly isFormMode = computed(() => this.mode() === 'create' || this.mode() === 'edit');
  readonly visibleTeams = computed(() => this.teams().filter((team) => this.getTeamType(team) === this.activeTeamType()));
  readonly canDeleteSelected = computed(() => {
    const team = this.selectedTeam();
    return !!team?.id && this.getTeamType(team) !== 'HOME';
  });

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    wins: [0, [Validators.required, Validators.min(0), Validators.max(99)]],
    losses: [0, [Validators.required, Validators.min(0), Validators.max(99)]],
    city: ['', Validators.required],
    coach: ['', Validators.required],
    playStyle: [''],
    note: [''],
    players: this.formBuilder.array([this.createPlayerGroup()])
  });

  constructor() {
    this.loadTeams();
  }

  get players(): FormArray {
    return this.form.controls.players;
  }

  selectTeam(team: OpponentTeam): void {
    this.selectedTeam.set(team);
    this.mode.set('view');
    this.errorMessage.set('');
  }

  selectTeamType(teamType: TeamType): void {
    this.activeTeamType.set(teamType);
    this.mode.set('view');
    this.errorMessage.set('');
    this.selectedTeam.set(this.visibleTeams()[0] ?? null);
  }

  startCreate(): void {
    this.activeTeamType.set('OPPONENT');
    this.selectedTeam.set(null);
    this.mode.set('create');
    this.errorMessage.set('');
    this.form.reset({
      name: '',
      wins: 0,
      losses: 0,
      city: '',
      coach: '',
      playStyle: '',
      note: ''
    });
    this.replacePlayers([this.emptyPlayerValue()]);
  }

  startEdit(): void {
    const team = this.selectedTeam();

    if (!team) {
      return;
    }

    this.mode.set('edit');
    this.errorMessage.set('');
    this.form.patchValue({
      name: team.name,
      wins: team.wins,
      losses: team.losses,
      city: team.city,
      coach: team.coach,
      playStyle: team.playStyle ?? '',
      note: team.note ?? ''
    });
    this.replacePlayers(team.players.length ? team.players : [this.emptyPlayerValue()]);
  }

  cancelForm(): void {
    this.mode.set('view');
    this.errorMessage.set('');
  }

  addPlayer(): void {
    this.players.push(this.createPlayerGroup());
  }

  removePlayer(index: number): void {
    if (this.players.length === 1) {
      this.players.at(0).reset(this.emptyPlayerValue());
      return;
    }

    this.players.removeAt(index);
  }

  save(): void {
    this.errorMessage.set('');

    const validationMessage = this.getValidationMessage();

    if (validationMessage) {
      this.form.markAllAsTouched();
      this.errorMessage.set(validationMessage);
      return;
    }

    const payload = this.form.getRawValue();
    const selected = this.selectedTeam();
    const team: OpponentTeam = {
      name: payload.name.trim(),
      wins: payload.wins,
      losses: payload.losses,
      city: payload.city.trim(),
      coach: payload.coach.trim(),
      playStyle: payload.playStyle.trim(),
      note: payload.note.trim() || null,
      teamType: this.mode() === 'edit' ? this.getTeamType(selected) : 'OPPONENT',
      players: payload.players
        .filter((player) => this.hasPlayerData(player))
        .map((player) => ({
          id: player.id || undefined,
          fullName: player.fullName.trim(),
          jerseyNumber: player.jerseyNumber,
          position: player.position.trim(),
          height: player.height,
          age: player.age,
          playerStatus: player.playerStatus
        }))
    };

    if (this.mode() === 'edit') {
      if (!selected?.id) {
        return;
      }

      this.opponentTeamsService.update(selected.id, team).subscribe({
        next: (updatedTeam) => this.afterSave(updatedTeam),
        error: (error: HttpErrorResponse) => this.errorMessage.set(this.getApiErrorMessage(error))
      });
      return;
    }

    this.opponentTeamsService.create(team).subscribe({
      next: (createdTeam) => this.afterSave(createdTeam),
      error: (error: HttpErrorResponse) => this.errorMessage.set(this.getApiErrorMessage(error))
    });
  }

  deleteSelected(): void {
    const team = this.selectedTeam();

    if (!team?.id) {
      return;
    }

    if (!this.canDeleteSelected()) {
      this.errorMessage.set('Nas tim ne moze biti obrisan.');
      return;
    }

    const confirmed = confirm(`Obrisati kompletan zapis za tim ${team.name}?`);

    if (!confirmed) {
      return;
    }

    this.opponentTeamsService.delete(team.id).subscribe({
      next: () => {
        const remainingTeams = this.teams().filter((item) => item.id !== team.id);
        this.teams.set(remainingTeams);
        this.selectedTeam.set(this.visibleTeams()[0] ?? null);
        this.mode.set('view');
      },
      error: () => this.errorMessage.set('Nije moguce obrisati tim.')
    });
  }

  private loadTeams(): void {
    this.isLoading.set(true);
    this.opponentTeamsService.getAll().subscribe({
      next: (teams) => {
        this.teams.set(teams);
        this.selectedTeam.set(this.visibleTeams()[0] ?? null);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Nije moguce ucitati timove.');
        this.isLoading.set(false);
      }
    });
  }

  private afterSave(savedTeam: OpponentTeam): void {
    const teams = this.teams();
    const existingIndex = teams.findIndex((team) => team.id === savedTeam.id);

    if (existingIndex >= 0) {
      this.teams.set(teams.map((team) => (team.id === savedTeam.id ? savedTeam : team)));
    } else {
      this.teams.set([...teams, savedTeam]);
    }

    this.activeTeamType.set(this.getTeamType(savedTeam));
    this.selectedTeam.set(savedTeam);
    this.mode.set('view');
  }

  private replacePlayers(players: OpponentTeam['players']): void {
    this.players.clear();
    players.forEach((player) => this.players.push(this.createPlayerGroup(player)));
  }

  private createPlayerGroup(player: OpponentPlayer = this.emptyPlayerValue()) {
    return this.formBuilder.nonNullable.group({
      id: [player.id ?? 0],
      fullName: [player.fullName],
      jerseyNumber: [player.jerseyNumber, [Validators.required, Validators.min(0), Validators.max(99)]],
      position: [player.position],
      height: [player.height, [Validators.required, Validators.min(120), Validators.max(250)]],
      age: [player.age, [Validators.required, Validators.min(12), Validators.max(60)]],
      playerStatus: [player.playerStatus ?? 'BENCH']
    });
  }

  private emptyPlayerValue(): OpponentPlayer {
    return {
      id: 0,
      fullName: '',
      jerseyNumber: 0,
      position: '',
      height: 180,
      age: 18,
      playerStatus: 'BENCH' as const
    };
  }

  private getValidationMessage(): string {
    const payload = this.form.getRawValue();

    if (!payload.name.trim() || !payload.city.trim() || !payload.coach.trim()) {
      return 'Popunite naziv tima, grad i trenera pre cuvanja.';
    }

    if (this.hasDuplicateTeamName(payload.name)) {
      return 'Vec postoji tim sa tim nazivom.';
    }

    if (payload.wins < 0 || payload.losses < 0 || payload.wins > 99 || payload.losses > 99) {
      return 'Broj pobeda i poraza mora biti izmedju 0 i 99.';
    }

    if (payload.players.some((player) => this.hasInvalidNumericPlayerValue(player))) {
      return 'Broj dresa, visina i godine igraca moraju biti u dozvoljenom opsegu.';
    }

    const filledPlayers = payload.players.filter((player) => this.hasPlayerData(player));

    if (filledPlayers.some((player) => !player.fullName.trim() || !player.position.trim())) {
      return 'Za svakog unetog igraca popunite ime i prezime, broj dresa, poziciju, visinu i godine.';
    }

    if (this.hasDuplicateJerseyNumber(filledPlayers)) {
      return 'U okviru istog tima ne mogu postojati dva igraca sa istim brojem.';
    }

    if (this.form.invalid) {
      return 'Uneti podaci nisu validni.';
    }

    return '';
  }

  private hasDuplicateTeamName(name: string): boolean {
    const normalizedName = name.trim().toLocaleLowerCase();
    const selectedId = this.mode() === 'edit' ? this.selectedTeam()?.id : null;

    return this.teams().some((team) => {
      return team.name.trim().toLocaleLowerCase() === normalizedName && team.id !== selectedId;
    });
  }

  private hasDuplicateJerseyNumber(players: OpponentTeam['players']): boolean {
    const usedNumbers = new Set<number>();

    for (const player of players) {
      if (usedNumbers.has(player.jerseyNumber)) {
        return true;
      }

      usedNumbers.add(player.jerseyNumber);
    }

    return false;
  }

  private hasPlayerData(player: OpponentTeam['players'][number]): boolean {
    return !!player.fullName.trim() || !!player.position.trim();
  }

  private hasInvalidNumericPlayerValue(player: OpponentTeam['players'][number]): boolean {
    if (!this.hasPlayerData(player)) {
      return player.jerseyNumber < 0 || player.height < 0 || player.age < 0;
    }

    return player.jerseyNumber < 0
      || player.jerseyNumber > 99
      || player.height < 120
      || player.height > 250
      || player.age < 12
      || player.age > 60;
  }

  private getApiErrorMessage(error: HttpErrorResponse): string {
    return error.error?.message ?? 'Nije moguce sacuvati tim.';
  }

  private getTeamType(team: OpponentTeam | null | undefined): TeamType {
    return team?.teamType ?? 'OPPONENT';
  }
}
