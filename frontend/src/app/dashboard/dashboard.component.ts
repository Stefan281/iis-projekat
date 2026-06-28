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

interface MetricResponse {
  id: number;
  name: string;
  standardMetric: boolean;
  unitOfMeasure: string;
  description: string | null;
}

interface PerformanceResponse {
  id: number;
  playerId: number;
  playerName: string;
  position: string | null;
  metricId: number;
  metricName: string;
  unitOfMeasure: string | null;
  value: number;
  comment: string | null;
  recordedAt: string;
}

interface ObservationResponse {
  id: number;
  playerId: number;
  playerName: string;
  position: string | null;
  observationDate: string;
  period: string;
  note: string | null;
}

interface PlayerAnalysisResponse {
  id: number;
  playerId: number;
  playerName: string;
  position: string | null;
  analysisDate: string;
  conclusion: string;
  note: string | null;
  analystName: string;
}

interface PlayerRecommendationResponse {
  id: number;
  playerId: number;
  playerName: string;
  position: string | null;
  analysisId: number | null;
  analysisConclusion: string | null;
  recommendationDate: string;
  criteria: string | null;
  explanation: string;
  recommendedByName: string;
}

interface RecommendationCriterionResponse {
  id: number;
  metricId: number;
  metricName: string;
  unitOfMeasure: string | null;
  comparison: 'MINIMUM' | 'MAXIMUM';
  thresholdValue: number;
  weight: number;
  required: boolean;
}

interface RecommendationModelResponse {
  id: number;
  name: string;
  position: string | null;
  minimumHeight: number | null;
  minimumScore: number;
  createdByName: string;
  createdAt: string;
  criteria: RecommendationCriterionResponse[];
}

interface RecommendationResultResponse {
  id: number;
  modelId: number;
  playerId: number;
  playerName: string;
  position: string | null;
  club: string | null;
  height: number | null;
  score: number;
  status: 'PREPORUCEN' | 'UZI_IZBOR' | 'NASTAVITI_PRACENJE' | 'NE_ISPUNJAVA';
  explanation: string;
  calculatedAt: string;
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
  readonly playerMessage = signal('');
  readonly playerError = signal('');
  readonly isSavingPlayer = signal(false);
  readonly metricMessage = signal('');
  readonly metricError = signal('');
  readonly isSavingMetric = signal(false);
  readonly performanceMessage = signal('');
  readonly performanceError = signal('');
  readonly isSavingPerformance = signal(false);
  readonly observationMessage = signal('');
  readonly observationError = signal('');
  readonly isSavingObservation = signal(false);
  readonly analysisMessage = signal('');
  readonly analysisError = signal('');
  readonly isSavingAnalysis = signal(false);
  readonly recommendationMessage = signal('');
  readonly recommendationError = signal('');
  readonly isSavingRecommendation = signal(false);
  readonly modelMessage = signal('');
  readonly modelError = signal('');
  readonly isSavingModel = signal(false);
  readonly isCalculatingModel = signal(false);
  readonly activeView = signal<'dashboard' | 'players' | 'add-player' | 'player-details' | 'edit-player' | 'metrics' | 'add-metric' | 'edit-metric' | 'performances' | 'add-performance' | 'observations' | 'add-observation' | 'analyses' | 'add-analysis' | 'edit-analysis' | 'recommendations' | 'add-recommendation' | 'edit-recommendation' | 'recommendation-models' | 'add-recommendation-model' | 'edit-recommendation-model' | 'recommendation-results'>('dashboard');
  readonly playerCount = signal(0);
  readonly players = signal<PlayerResponse[]>([]);
  readonly selectedPlayer = signal<PlayerResponse | null>(null);
  readonly metrics = signal<MetricResponse[]>([]);
  readonly selectedMetric = signal<MetricResponse | null>(null);
  readonly performances = signal<PerformanceResponse[]>([]);
  readonly playerPerformances = signal<PerformanceResponse[]>([]);
  readonly observations = signal<ObservationResponse[]>([]);
  readonly playerObservations = signal<ObservationResponse[]>([]);
  readonly analyses = signal<PlayerAnalysisResponse[]>([]);
  readonly selectedAnalysis = signal<PlayerAnalysisResponse | null>(null);
  readonly recommendations = signal<PlayerRecommendationResponse[]>([]);
  readonly selectedRecommendation = signal<PlayerRecommendationResponse | null>(null);
  readonly recommendationModels = signal<RecommendationModelResponse[]>([]);
  readonly selectedRecommendationModel = signal<RecommendationModelResponse | null>(null);
  readonly recommendationResults = signal<RecommendationResultResponse[]>([]);
  readonly performanceDrafts = signal<Record<number, { value: number | null; comment: string }>>({});
  readonly searchTerm = signal('');
  readonly observationSearchTerm = signal('');
  readonly observationPositionFilter = signal('');
  readonly observationDateFilter = signal('');
  readonly performanceSearchTerm = signal('');
  readonly metricSearchTerm = signal('');
  readonly performancePositionFilter = signal('');
  readonly performanceMetricFilter = signal('');
  readonly positionFilter = signal('');
  readonly clubFilter = signal('');
  readonly sortBy = signal<'newest' | 'oldest' | 'name' | 'position' | 'club'>('newest');
  readonly recommendationSearchTerm = signal('');

  readonly navItems = computed(() => {
    if (this.user()?.role === 'STRUCNI_STAB') {
      return [
        { label: 'Dashboard', icon: 'D' },
        { label: 'Igraci', icon: 'I' },
        { label: 'Performanse', icon: 'F' },
        { label: 'Analize', icon: 'A' }
      ];
    }

    if (this.user()?.role === 'SPORTSKI_DIREKTOR') {
      return [
        { label: 'Dashboard', icon: 'D' },
        { label: 'Igraci', icon: 'I' },
        { label: 'Performanse', icon: 'F' },
        { label: 'Analize', icon: 'A' },
        { label: 'Preporuke', icon: 'P' },
        { label: 'Sistem preporuke', icon: 'S' }
      ];
    }

    return [
      { label: 'Dashboard', icon: 'D' },
      { label: 'Igraci', icon: 'I' },
      { label: 'Posmatranja', icon: 'P' },
      { label: 'Performanse', icon: 'F' },
      { label: 'Metrike', icon: 'M' }
    ];
  });

  readonly recentPlayers = computed(() => this.players().slice().sort((a, b) => b.id - a.id).slice(0, 5));
  readonly positions = computed(() => this.uniqueValues(this.players().map((player) => player.position)));
  readonly clubs = computed(() => this.uniqueValues(this.players().map((player) => player.club)));
  readonly filteredMetrics = computed(() => {
    var search = this.metricSearchTerm().trim().toLowerCase();

    return this.metrics()
      .filter((metric) => !search || metric.name.toLowerCase().includes(search) || metric.unitOfMeasure.toLowerCase().includes(search))
      .slice()
      .sort((first, second) => first.name.localeCompare(second.name));
  });
  readonly filteredPerformances = computed(() => {
    var search = this.performanceSearchTerm().trim().toLowerCase();
    var position = this.performancePositionFilter();
    var metricId = this.performanceMetricFilter();

    return this.performances()
      .filter((performance) => {
        var matchesSearch = !search || performance.playerName.toLowerCase().includes(search);
        var matchesPosition = !position || performance.position === position;
        var matchesMetric = !metricId || String(performance.metricId) === metricId;

        return matchesSearch && matchesPosition && matchesMetric;
      })
      .slice()
      .sort((first, second) => second.id - first.id);
  });
  readonly filteredObservations = computed(() => {
    var search = this.observationSearchTerm().trim().toLowerCase();
    var position = this.observationPositionFilter();
    var date = this.observationDateFilter();

    return this.observations()
      .filter((observation) => {
        var matchesSearch = !search || observation.playerName.toLowerCase().includes(search);
        var matchesPosition = !position || observation.position === position;
        var matchesDate = !date || observation.observationDate === date;

        return matchesSearch && matchesPosition && matchesDate;
      })
      .slice()
      .sort((first, second) => second.id - first.id);
  });
  readonly selectedPlayerObservations = computed(() => {
    var player = this.selectedPlayer();

    if (!player) {
      return [];
    }

    var playerId = player.id;

    return this.playerObservations().filter((observation) => observation.playerId === playerId);
  });
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
  readonly filteredAnalyses = computed(() => this.analyses().slice().sort((first, second) => second.id - first.id));
  readonly filteredRecommendations = computed(() => {
    var search = this.recommendationSearchTerm().trim().toLowerCase();

    return this.recommendations()
      .filter((recommendation) => {
        var text = `${recommendation.playerName} ${recommendation.position || ''} ${recommendation.criteria || ''} ${recommendation.explanation}`.toLowerCase();
        return !search || text.includes(search);
      })
      .slice()
      .sort((first, second) => second.id - first.id);
  });

  readonly playerForm = this.formBuilder.group({
    selectedPlayerId: [null as number | null],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    birthYear: [null as number | null, [Validators.required, Validators.min(1950), Validators.max(2020)]],
    height: [null as number | null, [Validators.required, Validators.min(120), Validators.max(230)]],
    position: ['', Validators.required],
    club: ['', Validators.required],
    note: ['']
  });

  readonly metricForm = this.formBuilder.group({
    selectedMetricId: [null as number | null],
    name: ['', Validators.required],
    standardMetric: [true, Validators.required],
    unitOfMeasure: ['', Validators.required],
    description: ['']
  });

  readonly performanceForm = this.formBuilder.group({
    playerId: [null as number | null, Validators.required],
    metricId: [null as number | null, Validators.required],
    value: [null as number | null, Validators.required],
    comment: ['']
  });

  readonly observationForm = this.formBuilder.group({
    playerId: [null as number | null, Validators.required],
    observationDate: ['', Validators.required],
    period: ['', Validators.required],
    note: ['']
  });

  readonly analysisForm = this.formBuilder.group({
    playerId: [null as number | null, Validators.required],
    analysisDate: ['', Validators.required],
    conclusion: ['', Validators.required],
    note: ['']
  });

  readonly recommendationForm = this.formBuilder.group({
    playerId: [null as number | null, Validators.required],
    analysisId: [null as number | null],
    recommendationDate: ['', Validators.required],
    criteria: [''],
    explanation: ['', Validators.required]
  });

  readonly recommendationModelForm = this.formBuilder.group({
    name: ['', Validators.required],
    position: [''],
    minimumHeight: [null as number | null, Validators.min(120)],
    minimumScore: [70, [Validators.required, Validators.min(0), Validators.max(100)]],
    criteria: this.formBuilder.array([] as ReturnType<typeof this.createCriterionGroup>[])
  });

  constructor() {
    this.loadPlayerOverview();
    this.loadMetrics();
    this.loadPerformances();
    this.loadObservations();
    this.loadAnalyses();
    this.loadRecommendations();
    this.loadRecommendationModels();
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

  showMetrics(): void {
    this.metricMessage.set('');
    this.metricError.set('');
    this.activeView.set('metrics');
    this.loadMetrics();
  }

  showPerformances(): void {
    this.performanceMessage.set('');
    this.performanceError.set('');
    this.activeView.set('performances');
    this.loadPerformances();
  }

  showObservations(): void {
    this.observationMessage.set('');
    this.observationError.set('');
    this.activeView.set('observations');
    this.loadObservations();
  }

  showAnalyses(): void {
    this.analysisMessage.set('');
    this.analysisError.set('');
    this.activeView.set('analyses');
    this.loadAnalyses();
  }

  showRecommendations(): void {
    this.recommendationMessage.set('');
    this.recommendationError.set('');
    this.activeView.set('recommendations');
    this.loadRecommendations();
  }

  showRecommendationModels(): void {
    this.modelMessage.set('');
    this.modelError.set('');
    this.activeView.set('recommendation-models');
    this.loadRecommendationModels();
  }

  showAddRecommendationModel(): void {
    this.selectedRecommendationModel.set(null);
    this.modelMessage.set('');
    this.modelError.set('');
    this.resetRecommendationModelForm();
    this.addRecommendationCriterion();
    this.activeView.set('add-recommendation-model');
  }

  showEditRecommendationModel(model: RecommendationModelResponse): void {
    this.selectedRecommendationModel.set(model);
    this.modelMessage.set('');
    this.modelError.set('');
    this.resetRecommendationModelForm();
    this.recommendationModelForm.patchValue({
      name: model.name,
      position: model.position || '',
      minimumHeight: model.minimumHeight,
      minimumScore: model.minimumScore
    });
    model.criteria.forEach((criterion) => this.addRecommendationCriterion(criterion));
    this.activeView.set('edit-recommendation-model');
  }

  get recommendationCriteria() {
    return this.recommendationModelForm.controls.criteria;
  }

  addRecommendationCriterion(criterion?: RecommendationCriterionResponse): void {
    this.recommendationCriteria.push(this.createCriterionGroup(criterion));
  }

  removeRecommendationCriterion(index: number): void {
    if (this.recommendationCriteria.length > 1) {
      this.recommendationCriteria.removeAt(index);
    }
  }

  saveRecommendationModel(): void {
    if (this.recommendationModelForm.invalid || this.recommendationCriteria.length === 0) {
      this.recommendationModelForm.markAllAsTouched();
      this.modelError.set('Popuni obavezna polja i dodaj najmanje jedan kriterijum.');
      return;
    }

    var totalWeight = this.recommendationCriteria.getRawValue()
      .reduce((sum, criterion) => sum + Number(criterion.weight || 0), 0);
    if (Math.abs(totalWeight - 100) > 0.01) {
      this.modelError.set(`Zbir tezina kriterijuma mora biti 100. Trenutni zbir je ${totalWeight}.`);
      return;
    }

    this.modelError.set('');
    this.isSavingModel.set(true);
    var selectedModel = this.selectedRecommendationModel();
    var request = this.activeView() === 'edit-recommendation-model' && selectedModel
      ? this.http.put<RecommendationModelResponse>(`http://localhost:8080/api/recommendation-models/${selectedModel.id}`, this.recommendationModelForm.getRawValue())
      : this.http.post<RecommendationModelResponse>('http://localhost:8080/api/recommendation-models', this.recommendationModelForm.getRawValue());

    request.subscribe({
      next: () => {
        this.isSavingModel.set(false);
        this.modelMessage.set('Model preporuke je sacuvan u bazi.');
        this.loadRecommendationModels();
        this.activeView.set('recommendation-models');
      },
      error: (error) => {
        this.isSavingModel.set(false);
        this.modelError.set(error.error?.detail || error.error?.message || 'Model preporuke nije sacuvan.');
      }
    });
  }

  deleteRecommendationModel(model: RecommendationModelResponse): void {
    this.http.delete(`http://localhost:8080/api/recommendation-models/${model.id}`).subscribe({
      next: () => {
        this.modelMessage.set('Model preporuke je obrisan.');
        this.loadRecommendationModels();
      },
      error: () => this.modelError.set('Model preporuke nije obrisan.')
    });
  }

  calculateRecommendationModel(model: RecommendationModelResponse): void {
    this.selectedRecommendationModel.set(model);
    this.modelError.set('');
    this.isCalculatingModel.set(true);
    this.http.post<RecommendationResultResponse[]>(`http://localhost:8080/api/recommendation-models/${model.id}/calculate`, {}).subscribe({
      next: (results) => {
        this.recommendationResults.set(results);
        this.isCalculatingModel.set(false);
        this.activeView.set('recommendation-results');
      },
      error: (error) => {
        this.isCalculatingModel.set(false);
        this.modelError.set(error.error?.detail || error.error?.message || 'Rang-lista nije izracunata.');
      }
    });
  }

  showRecommendationResults(model: RecommendationModelResponse): void {
    this.selectedRecommendationModel.set(model);
    this.modelError.set('');
    this.http.get<RecommendationResultResponse[]>(`http://localhost:8080/api/recommendation-models/${model.id}/results`).subscribe({
      next: (results) => {
        this.recommendationResults.set(results);
        this.activeView.set('recommendation-results');
      },
      error: () => this.modelError.set('Rezultati nisu ucitani.')
    });
  }

  showAddAnalysis(): void {
    var firstPlayer = this.players()[0];
    this.selectedAnalysis.set(null);
    this.analysisMessage.set('');
    this.analysisError.set('');
    this.analysisForm.reset({
      playerId: firstPlayer?.id || null,
      analysisDate: new Date().toISOString().slice(0, 10),
      conclusion: '',
      note: ''
    });
    this.activeView.set('add-analysis');
  }

  showEditAnalysis(analysis: PlayerAnalysisResponse): void {
    this.selectedAnalysis.set(analysis);
    this.analysisMessage.set('');
    this.analysisError.set('');
    this.analysisForm.reset({
      playerId: analysis.playerId,
      analysisDate: analysis.analysisDate,
      conclusion: analysis.conclusion,
      note: analysis.note || ''
    });
    this.activeView.set('edit-analysis');
  }

  showAddRecommendation(): void {
    var firstPlayer = this.players()[0];
    this.selectedRecommendation.set(null);
    this.recommendationMessage.set('');
    this.recommendationError.set('');
    this.recommendationForm.reset({
      playerId: firstPlayer?.id || null,
      analysisId: null,
      recommendationDate: new Date().toISOString().slice(0, 10),
      criteria: '',
      explanation: ''
    });
    this.activeView.set('add-recommendation');
  }

  showEditRecommendation(recommendation: PlayerRecommendationResponse): void {
    this.selectedRecommendation.set(recommendation);
    this.recommendationMessage.set('');
    this.recommendationError.set('');
    this.recommendationForm.reset({
      playerId: recommendation.playerId,
      analysisId: recommendation.analysisId,
      recommendationDate: recommendation.recommendationDate,
      criteria: recommendation.criteria || '',
      explanation: recommendation.explanation
    });
    this.activeView.set('edit-recommendation');
  }

  showAddObservation(): void {
    this.observationMessage.set('');
    this.observationError.set('');
    this.performanceError.set('');
    var firstPlayer = this.players()[0];
    this.observationForm.reset({
      playerId: firstPlayer?.id || null,
      observationDate: new Date().toISOString().slice(0, 10),
      period: 'Cela utakmica',
      note: ''
    });
    this.activeView.set('add-observation');

    if (firstPlayer) {
      this.selectedPlayer.set(firstPlayer);
      this.loadPlayerPerformances(firstPlayer.id);
    }
  }

  showAddMetric(): void {
    this.resetMetricForm();
    this.selectedMetric.set(null);
    this.metricMessage.set('');
    this.metricError.set('');
    this.activeView.set('add-metric');
  }

  showEditMetric(metric: MetricResponse): void {
    this.selectedMetric.set(metric);
    this.metricMessage.set('');
    this.metricError.set('');
    this.metricForm.reset({
      selectedMetricId: metric.id,
      name: metric.name,
      standardMetric: metric.standardMetric,
      unitOfMeasure: metric.unitOfMeasure,
      description: metric.description || ''
    });
    this.activeView.set('edit-metric');
  }

  showPlayerDetails(player: PlayerResponse): void {
    this.selectedPlayer.set(player);
    this.activeView.set('player-details');
    this.loadPlayerPerformances(player.id);
    this.loadPlayerObservations(player.id);
  }

  showObservationPlayerDetails(observation: ObservationResponse): void {
    var player = this.players().find((item) => item.id === observation.playerId);

    if (player) {
      this.showPlayerDetails(player);
    }
  }

  showEditPlayer(player: PlayerResponse): void {
    if (!this.isScout) {
      return;
    }

    this.selectedPlayer.set(player);
    this.playerMessage.set('');
    this.playerError.set('');
    this.playerForm.reset({
      selectedPlayerId: player.id,
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

  selectPlayerForEdit(playerId: string): void {
    var id = Number(playerId);
    var player = this.players().find((item) => item.id === id);

    if (!player) {
      return;
    }

    this.showEditPlayer(player);
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

  saveMetric(): void {
    if (this.metricForm.invalid) {
      this.metricForm.markAllAsTouched();
      return;
    }

    this.metricMessage.set('');
    this.metricError.set('');
    this.isSavingMetric.set(true);

    var selectedMetric = this.selectedMetric();
    var request = this.activeView() === 'edit-metric' && selectedMetric
      ? this.http.put<MetricResponse>(`http://localhost:8080/api/metrics/${selectedMetric.id}`, this.metricForm.getRawValue())
      : this.http.post<MetricResponse>('http://localhost:8080/api/metrics', this.metricForm.getRawValue());

    request.subscribe({
      next: () => {
        this.resetMetricForm();
        this.selectedMetric.set(null);
        this.isSavingMetric.set(false);
        this.metricMessage.set('Metrika je sacuvana u bazi.');
        this.loadMetrics();
        this.activeView.set('metrics');
      },
      error: () => {
        this.isSavingMetric.set(false);
        this.metricError.set('Metrika nije sacuvana. Proveri podatke ili da li naziv vec postoji.');
      }
    });
  }

  showAddPerformance(player?: PlayerResponse): void {
    var targetPlayer = player || this.selectedPlayer();

    if (!targetPlayer) {
      this.performanceError.set('Prvo izaberi igraca za unos performansi.');
      return;
    }

    this.performanceMessage.set('');
    this.performanceError.set('');
    this.performanceForm.reset({
      playerId: targetPlayer.id,
      metricId: null,
      value: null,
      comment: ''
    });
    this.selectedPlayer.set(targetPlayer);
    this.activeView.set('add-performance');
  }

  savePerformance(): void {
    if (this.performanceForm.invalid) {
      this.performanceForm.markAllAsTouched();
      return;
    }

    this.performanceMessage.set('');
    this.performanceError.set('');
    this.isSavingPerformance.set(true);

    this.http.post<PerformanceResponse>('http://localhost:8080/api/performances', this.performanceForm.getRawValue()).subscribe({
      next: () => {
        this.isSavingPerformance.set(false);
        this.performanceMessage.set('Performansa je sacuvana u bazi.');
        this.loadPerformances();
        var selectedPlayer = this.selectedPlayer();
        if (selectedPlayer) {
          this.loadPlayerPerformances(selectedPlayer.id);
          this.activeView.set('player-details');
        } else {
          this.activeView.set('performances');
        }
      },
      error: () => {
        this.isSavingPerformance.set(false);
        this.performanceError.set('Performansa nije sacuvana. Proveri podatke i da li je backend pokrenut.');
      }
    });
  }

  saveObservation(): void {
    if (this.observationForm.invalid) {
      this.observationForm.markAllAsTouched();
      return;
    }

    this.observationMessage.set('');
    this.observationError.set('');
    this.isSavingObservation.set(true);

    this.http.post<ObservationResponse>('http://localhost:8080/api/observations', this.observationForm.getRawValue()).subscribe({
      next: () => {
        this.isSavingObservation.set(false);
        this.observationMessage.set('Posmatranje je sacuvano u bazi.');
        this.loadObservations();
        this.activeView.set('observations');
      },
      error: () => {
        this.isSavingObservation.set(false);
        this.observationError.set('Posmatranje nije sacuvano. Proveri podatke i backend.');
      }
    });
  }

  saveAnalysis(): void {
    if (this.analysisForm.invalid) {
      this.analysisForm.markAllAsTouched();
      return;
    }

    this.analysisMessage.set('');
    this.analysisError.set('');
    this.isSavingAnalysis.set(true);

    var selectedAnalysis = this.selectedAnalysis();
    var request = this.activeView() === 'edit-analysis' && selectedAnalysis
      ? this.http.put<PlayerAnalysisResponse>(`http://localhost:8080/api/player-analyses/${selectedAnalysis.id}`, this.analysisForm.getRawValue())
      : this.http.post<PlayerAnalysisResponse>('http://localhost:8080/api/player-analyses', this.analysisForm.getRawValue());

    request.subscribe({
      next: () => {
        this.isSavingAnalysis.set(false);
        this.analysisMessage.set('Analiza je sacuvana u bazi.');
        this.selectedAnalysis.set(null);
        this.loadAnalyses();
        this.activeView.set('analyses');
      },
      error: () => {
        this.isSavingAnalysis.set(false);
        this.analysisError.set('Analiza nije sacuvana. Proveri podatke i backend.');
      }
    });
  }

  saveRecommendation(): void {
    if (this.recommendationForm.invalid) {
      this.recommendationForm.markAllAsTouched();
      return;
    }

    this.recommendationMessage.set('');
    this.recommendationError.set('');
    this.isSavingRecommendation.set(true);

    var selectedRecommendation = this.selectedRecommendation();
    var request = this.activeView() === 'edit-recommendation' && selectedRecommendation
      ? this.http.put<PlayerRecommendationResponse>(`http://localhost:8080/api/player-recommendations/${selectedRecommendation.id}`, this.recommendationForm.getRawValue())
      : this.http.post<PlayerRecommendationResponse>('http://localhost:8080/api/player-recommendations', this.recommendationForm.getRawValue());

    request.subscribe({
      next: () => {
        this.isSavingRecommendation.set(false);
        this.recommendationMessage.set('Preporuka je sacuvana u bazi.');
        this.selectedRecommendation.set(null);
        this.loadRecommendations();
        this.activeView.set('recommendations');
      },
      error: () => {
        this.isSavingRecommendation.set(false);
        this.recommendationError.set('Preporuka nije sacuvana. Proveri podatke i backend.');
      }
    });
  }

  deleteAnalysis(analysis: PlayerAnalysisResponse): void {
    this.http.delete(`http://localhost:8080/api/player-analyses/${analysis.id}`).subscribe({
      next: () => {
        this.analysisMessage.set('Analiza je obrisana.');
        this.loadAnalyses();
      },
      error: () => this.analysisError.set('Analiza nije obrisana.')
    });
  }

  deleteRecommendation(recommendation: PlayerRecommendationResponse): void {
    this.http.delete(`http://localhost:8080/api/player-recommendations/${recommendation.id}`).subscribe({
      next: () => {
        this.recommendationMessage.set('Preporuka je obrisana.');
        this.loadRecommendations();
      },
      error: () => this.recommendationError.set('Preporuka nije obrisana.')
    });
  }

  selectMetricForEdit(metricId: string): void {
    var id = Number(metricId);
    var metric = this.metrics().find((item) => item.id === id);

    if (!metric) {
      return;
    }

    this.showEditMetric(metric);
  }

  get isScout(): boolean {
    return this.user()?.role === 'SKAUT';
  }

  get stats() {
    return [
      { label: 'Igraci', value: this.playerCount() },
      { label: 'Posmatranja', value: this.observations().length },
      { label: 'Preporuke', value: this.recommendations().length }
    ];
  }

  isActiveNav(label: string): boolean {
    if (['players', 'add-player', 'player-details', 'edit-player'].includes(this.activeView())) {
      return label === 'Igraci';
    }

    if (['metrics', 'add-metric', 'edit-metric'].includes(this.activeView())) {
      return label === 'Metrike';
    }

    if (['performances', 'add-performance'].includes(this.activeView())) {
      return label === 'Performanse';
    }

    if (['observations', 'add-observation'].includes(this.activeView())) {
      return label === 'Posmatranja';
    }

    if (['analyses', 'add-analysis', 'edit-analysis'].includes(this.activeView())) {
      return label === 'Analize';
    }

    if (['recommendations', 'add-recommendation', 'edit-recommendation'].includes(this.activeView())) {
      return label === 'Preporuke';
    }

    if (['recommendation-models', 'add-recommendation-model', 'edit-recommendation-model', 'recommendation-results'].includes(this.activeView())) {
      return label === 'Sistem preporuke';
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
      return;
    }

    if (label === 'Metrike') {
      this.showMetrics();
      return;
    }

    if (label === 'Performanse') {
      this.showPerformances();
      return;
    }

    if (label === 'Posmatranja') {
      this.showObservations();
      return;
    }

    if (label === 'Analize') {
      this.showAnalyses();
      return;
    }

    if (label === 'Preporuke') {
      this.showRecommendations();
      return;
    }

    if (label === 'Sistem preporuke') {
      this.showRecommendationModels();
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

  updateMetricSearch(value: string): void {
    this.metricSearchTerm.set(value);
  }

  updatePerformanceSearch(value: string): void {
    this.performanceSearchTerm.set(value);
  }

  updatePerformancePosition(value: string): void {
    this.performancePositionFilter.set(value);
  }

  updatePerformanceMetric(value: string): void {
    this.performanceMetricFilter.set(value);
  }

  updateObservationSearch(value: string): void {
    this.observationSearchTerm.set(value);
  }

  updateObservationPosition(value: string): void {
    this.observationPositionFilter.set(value);
  }

  updateObservationDate(value: string): void {
    this.observationDateFilter.set(value);
  }

  updateRecommendationSearch(value: string): void {
    this.recommendationSearchTerm.set(value);
  }

  selectObservationPlayer(playerId: string): void {
    var id = Number(playerId);
    var player = this.players().find((item) => item.id === id);

    if (!player) {
      return;
    }

    this.selectedPlayer.set(player);
    this.loadPlayerPerformances(player.id);
  }

  updatePerformanceDraft(performance: PerformanceResponse, field: 'value' | 'comment', value: string): void {
    var drafts = { ...this.performanceDrafts() };
    var current = drafts[performance.id] || { value: performance.value, comment: performance.comment || '' };
    drafts[performance.id] = {
      ...current,
      [field]: field === 'value' ? Number(value) : value
    };
    this.performanceDrafts.set(drafts);
  }

  performanceDraftValue(performance: PerformanceResponse): number | null {
    return this.performanceDrafts()[performance.id]?.value ?? performance.value;
  }

  performanceDraftComment(performance: PerformanceResponse): string {
    return this.performanceDrafts()[performance.id]?.comment ?? performance.comment ?? '';
  }

  savePerformanceDraft(performance: PerformanceResponse): void {
    var draft = this.performanceDrafts()[performance.id] || { value: performance.value, comment: performance.comment || '' };
    this.http.put<PerformanceResponse>(`http://localhost:8080/api/performances/${performance.id}`, {
      playerId: performance.playerId,
      metricId: performance.metricId,
      value: draft.value,
      comment: draft.comment
    }).subscribe({
      next: () => {
        this.performanceMessage.set('Performansa je izmenjena.');
        this.loadPerformances();
        this.loadPlayerPerformances(performance.playerId);
      },
      error: () => this.performanceError.set('Performansa nije izmenjena.')
    });
  }

  formatPerformanceValue(performance: PerformanceResponse): string {
    var unit = performance.unitOfMeasure || '';

    if (unit === '%') {
      return `${performance.value}%`;
    }

    return `${performance.value}${unit ? ' ' + unit : ''}`;
  }

  recommendationStatusLabel(status: RecommendationResultResponse['status']): string {
    var labels: Record<RecommendationResultResponse['status'], string> = {
      PREPORUCEN: 'Preporucen',
      UZI_IZBOR: 'Uzi izbor',
      NASTAVITI_PRACENJE: 'Nastaviti pracenje',
      NE_ISPUNJAVA: 'Ne ispunjava'
    };
    return labels[status];
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

  generatePdfReport(): void {
    var reportWindow = window.open('', '_blank', 'width=1100,height=800');

    if (!reportWindow) {
      alert('Pregledac je blokirao otvaranje izvestaja. Dozvoli pop-up prozor za aplikaciju.');
      return;
    }

    reportWindow.document.open();
    reportWindow.document.write(this.buildPdfReportHtml());
    reportWindow.document.close();
    reportWindow.focus();
  }

  private buildPdfReportHtml(): string {
    var generatedAt = new Date().toLocaleString('sr-RS');
    var userName = this.user()
      ? `${this.user()?.firstName || ''} ${this.user()?.lastName || ''}`.trim() || this.roleTitle
      : this.roleTitle;
    var selectedModel = this.selectedRecommendationModel();
    var resultsTitle = selectedModel
      ? `Rang-lista za model: ${selectedModel.name}`
      : 'Rang-lista sistema preporuke';

    return `<!doctype html>
      <html lang="sr">
        <head>
          <meta charset="utf-8">
          <title>PDF izvestaj</title>
          <style>
            @page { margin: 18mm; }
            * { box-sizing: border-box; }
            body {
              margin: 0;
              color: #172033;
              font-family: Arial, sans-serif;
              line-height: 1.45;
            }
            header {
              display: flex;
              justify-content: space-between;
              gap: 24px;
              border-bottom: 2px solid #0c7fda;
              padding-bottom: 16px;
              margin-bottom: 24px;
            }
            h1, h2, h3, p { margin-top: 0; }
            h1 { margin-bottom: 6px; font-size: 28px; }
            h2 { margin: 28px 0 12px; font-size: 19px; color: #0f3358; }
            .meta {
              min-width: 230px;
              color: #53647c;
              font-size: 13px;
              text-align: right;
            }
            .summary {
              display: grid;
              grid-template-columns: repeat(4, 1fr);
              gap: 10px;
              margin-bottom: 8px;
            }
            .summary div {
              border: 1px solid #d8e0ea;
              border-radius: 8px;
              padding: 12px;
              background: #f7fafc;
            }
            .summary span {
              display: block;
              color: #62728a;
              font-size: 12px;
              font-weight: 700;
            }
            .summary strong {
              display: block;
              margin-top: 4px;
              font-size: 22px;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              margin-bottom: 16px;
              page-break-inside: auto;
            }
            tr { page-break-inside: avoid; page-break-after: auto; }
            th, td {
              border-bottom: 1px solid #e6ebf1;
              padding: 8px;
              text-align: left;
              vertical-align: top;
              font-size: 12px;
            }
            th {
              background: #eef5fb;
              color: #172033;
              font-size: 12px;
            }
            .empty {
              border: 1px solid #e6ebf1;
              border-radius: 8px;
              padding: 12px;
              color: #62728a;
              background: #f7fafc;
            }
            @media print {
              .no-print { display: none; }
            }
          </style>
        </head>
        <body>
          <header>
            <div>
              <h1>Izvestaj skauting sistema</h1>
              <p>Automatski izvestaj napravljen na osnovu podataka ucitanih iz aplikacije.</p>
            </div>
            <div class="meta">
              <strong>${this.escapeHtml(this.roleTitle)}</strong><br>
              Korisnik: ${this.escapeHtml(userName)}<br>
              Datum: ${this.escapeHtml(generatedAt)}
            </div>
          </header>

          <section class="summary">
            <div><span>Igraci</span><strong>${this.playerCount()}</strong></div>
            <div><span>Metrike</span><strong>${this.metrics().length}</strong></div>
            <div><span>Performanse</span><strong>${this.performances().length}</strong></div>
            <div><span>Posmatranja</span><strong>${this.observations().length}</strong></div>
          </section>

          <h2>Igraci</h2>
          ${this.buildReportTable(this.players(), [
            { header: 'Ime i prezime', cell: (player) => `${player.firstName} ${player.lastName}` },
            { header: 'Pozicija', cell: (player) => player.position },
            { header: 'Klub', cell: (player) => player.club },
            { header: 'Godiste', cell: (player) => player.birthYear },
            { header: 'Visina', cell: (player) => player.height ? `${player.height} cm` : null }
          ], 'Nema evidentiranih igraca.')}

          <h2>Metrike</h2>
          ${this.buildReportTable(this.metrics(), [
            { header: 'Naziv', cell: (metric) => metric.name },
            { header: 'Tip', cell: (metric) => metric.standardMetric ? 'Standardna' : 'Specijalna' },
            { header: 'Jedinica', cell: (metric) => metric.unitOfMeasure },
            { header: 'Opis', cell: (metric) => metric.description }
          ], 'Nema evidentiranih metrika.')}

          <h2>Performanse</h2>
          ${this.buildReportTable(this.performances(), [
            { header: 'Igrac', cell: (performance) => performance.playerName },
            { header: 'Pozicija', cell: (performance) => performance.position },
            { header: 'Metrika', cell: (performance) => performance.metricName },
            { header: 'Vrednost', cell: (performance) => `${performance.value}${performance.unitOfMeasure ? ' ' + performance.unitOfMeasure : ''}` },
            { header: 'Komentar', cell: (performance) => performance.comment }
          ], 'Nema evidentiranih performansi.')}

          <h2>Posmatranja</h2>
          ${this.buildReportTable(this.observations(), [
            { header: 'Igrac', cell: (observation) => observation.playerName },
            { header: 'Pozicija', cell: (observation) => observation.position },
            { header: 'Datum', cell: (observation) => observation.observationDate },
            { header: 'Period', cell: (observation) => observation.period },
            { header: 'Napomena', cell: (observation) => observation.note }
          ], 'Nema evidentiranih posmatranja.')}

          <h2>Analize</h2>
          ${this.buildReportTable(this.analyses(), [
            { header: 'Igrac', cell: (analysis) => analysis.playerName },
            { header: 'Pozicija', cell: (analysis) => analysis.position },
            { header: 'Datum', cell: (analysis) => analysis.analysisDate },
            { header: 'Zakljucak', cell: (analysis) => analysis.conclusion },
            { header: 'Analiticar', cell: (analysis) => analysis.analystName }
          ], 'Nema evidentiranih analiza.')}

          <h2>Rucne preporuke</h2>
          ${this.buildReportTable(this.recommendations(), [
            { header: 'Igrac', cell: (recommendation) => recommendation.playerName },
            { header: 'Pozicija', cell: (recommendation) => recommendation.position },
            { header: 'Datum', cell: (recommendation) => recommendation.recommendationDate },
            { header: 'Kriterijumi', cell: (recommendation) => recommendation.criteria },
            { header: 'Obrazlozenje', cell: (recommendation) => recommendation.explanation }
          ], 'Nema evidentiranih preporuka.')}

          <h2>Modeli preporuke</h2>
          ${this.buildReportTable(this.recommendationModels(), [
            { header: 'Naziv', cell: (model) => model.name },
            { header: 'Pozicija', cell: (model) => model.position || 'Sve pozicije' },
            { header: 'Minimalna visina', cell: (model) => model.minimumHeight ? `${model.minimumHeight} cm` : null },
            { header: 'Minimalna ocena', cell: (model) => `${model.minimumScore}/100` },
            { header: 'Broj kriterijuma', cell: (model) => model.criteria.length }
          ], 'Nema evidentiranih modela preporuke.')}

          <h2>${this.escapeHtml(resultsTitle)}</h2>
          ${this.buildReportTable(this.recommendationResults(), [
            { header: 'Rang', cell: (_result, index) => `#${index + 1}` },
            { header: 'Igrac', cell: (result) => result.playerName },
            { header: 'Pozicija / klub', cell: (result) => `${result.position || '-'} / ${result.club || '-'}` },
            { header: 'Visina', cell: (result) => result.height ? `${result.height} cm` : null },
            { header: 'Ocena', cell: (result) => `${result.score}/100` },
            { header: 'Status', cell: (result) => this.recommendationStatusLabel(result.status) },
            { header: 'Obrazlozenje', cell: (result) => result.explanation }
          ], 'Rang-lista nije ucitana ili nije izracunata.')}

          <script>
            window.addEventListener('load', function () {
              setTimeout(function () { window.print(); }, 250);
            });
          </script>
        </body>
      </html>`;
  }

  private buildReportTable<T>(
    items: T[],
    columns: Array<{ header: string; cell: (item: T, index: number) => unknown }>,
    emptyMessage: string
  ): string {
    if (!items.length) {
      return `<p class="empty">${this.escapeHtml(emptyMessage)}</p>`;
    }

    var header = columns.map((column) => `<th>${this.escapeHtml(column.header)}</th>`).join('');
    var rows = items.map((item, index) => {
      var cells = columns
        .map((column) => `<td>${this.escapeHtml(column.cell(item, index))}</td>`)
        .join('');

      return `<tr>${cells}</tr>`;
    }).join('');

    return `<table><thead><tr>${header}</tr></thead><tbody>${rows}</tbody></table>`;
  }

  private escapeHtml(value: unknown): string {
    var text = value === null || value === undefined || value === '' ? '-' : String(value);
    var replacements: Record<string, string> = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    };

    return text.replace(/[&<>"']/g, (character) => replacements[character]);
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

  private loadMetrics(): void {
    this.http.get<MetricResponse[]>('http://localhost:8080/api/metrics').subscribe({
      next: (metrics) => this.metrics.set(metrics),
      error: () => this.metrics.set([])
    });
  }

  private loadPerformances(): void {
    this.http.get<PerformanceResponse[]>('http://localhost:8080/api/performances').subscribe({
      next: (performances) => this.performances.set(performances),
      error: () => this.performances.set([])
    });
  }

  private loadObservations(): void {
    this.http.get<ObservationResponse[]>('http://localhost:8080/api/observations').subscribe({
      next: (observations) => this.observations.set(observations),
      error: () => this.observations.set([])
    });
  }

  private loadAnalyses(): void {
    this.http.get<PlayerAnalysisResponse[]>('http://localhost:8080/api/player-analyses').subscribe({
      next: (analyses) => this.analyses.set(analyses),
      error: () => this.analyses.set([])
    });
  }

  private loadRecommendations(): void {
    this.http.get<PlayerRecommendationResponse[]>('http://localhost:8080/api/player-recommendations').subscribe({
      next: (recommendations) => this.recommendations.set(recommendations),
      error: () => this.recommendations.set([])
    });
  }

  private loadRecommendationModels(): void {
    this.http.get<RecommendationModelResponse[]>('http://localhost:8080/api/recommendation-models').subscribe({
      next: (models) => this.recommendationModels.set(models),
      error: () => this.recommendationModels.set([])
    });
  }

  private loadPlayerObservations(playerId: number): void {
    this.playerObservations.set([]);
    this.http.get<ObservationResponse[]>(`http://localhost:8080/api/observations?playerId=${playerId}`).subscribe({
      next: (observations) => this.playerObservations.set(observations.filter((observation) => observation.playerId === playerId)),
      error: () => this.playerObservations.set([])
    });
  }

  private loadPlayerPerformances(playerId: number): void {
    this.http.get<PerformanceResponse[]>(`http://localhost:8080/api/performances?playerId=${playerId}`).subscribe({
      next: (performances) => {
        this.playerPerformances.set(performances);
        this.performanceDrafts.set(Object.fromEntries(performances.map((performance) => [
          performance.id,
          { value: performance.value, comment: performance.comment || '' }
        ])));
      },
      error: () => this.playerPerformances.set([])
    });
  }

  private resetPlayerForm(): void {
    this.playerForm.reset({
      selectedPlayerId: null,
      firstName: '',
      lastName: '',
      birthYear: null,
      height: null,
      position: '',
      club: '',
      note: ''
    });
  }

  private resetMetricForm(): void {
    this.metricForm.reset({
      selectedMetricId: null,
      name: '',
      standardMetric: true,
      unitOfMeasure: '',
      description: ''
    });
  }

  private createCriterionGroup(criterion?: RecommendationCriterionResponse) {
    return this.formBuilder.group({
      metricId: [criterion?.metricId ?? null as number | null, Validators.required],
      comparison: [criterion?.comparison ?? 'MINIMUM', Validators.required],
      thresholdValue: [criterion?.thresholdValue ?? null as number | null, [Validators.required, Validators.min(0.01)]],
      weight: [criterion?.weight ?? null as number | null, [Validators.required, Validators.min(0.01), Validators.max(100)]],
      required: [criterion?.required ?? false]
    });
  }

  private resetRecommendationModelForm(): void {
    this.recommendationModelForm.reset({
      name: '',
      position: '',
      minimumHeight: null,
      minimumScore: 70
    });
    this.recommendationCriteria.clear();
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
