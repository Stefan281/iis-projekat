import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NotificationsService } from '../core/services/notifications.service';
import { TripsService } from '../core/services/trips.service';
import { Obavestenje, Putovanje } from '../core/models/models';

interface CalendarDay {
  date: Date;
  dayNum: number;
  currentMonth: boolean;
  isToday: boolean;
  tripIds: number[];
  isTripStart: boolean;
  isTripEnd: boolean;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private obavestenjaService = inject(NotificationsService);
  private putovanjaService = inject(TripsService);
  private fb = inject(FormBuilder);

  readonly MESECI = [
    'Januar', 'Februar', 'Mart', 'April', 'Maj', 'Jun',
    'Jul', 'Avgust', 'Septembar', 'Oktobar', 'Novembar', 'Decembar'
  ];

  readonly TRIP_COLORS = [
    { bg: '#E3F2FD', solid: '#1976D2' },
    { bg: '#F3E5F5', solid: '#7B1FA2' },
    { bg: '#E8F5E9', solid: '#388E3C' },
    { bg: '#FFF3E0', solid: '#F57C00' },
    { bg: '#FCE4EC', solid: '#C2185B' },
    { bg: '#E0F2F1', solid: '#00796B' },
  ];

  tripColorMap = new Map<number, number>();

  obavestenja = signal<Obavestenje[]>([]);
  putovanja = signal<Putovanje[]>([]);
  showForm = signal(false);
  selectedPutovanje = signal<Putovanje | null>(null);
  calendarViewDate = signal(new Date());
  isSaving = signal(false);

  novoObavestenjeForm = this.fb.nonNullable.group({
    tekst: ['', Validators.required]
  });

  get calendarDays(): CalendarDay[] {
    const year = this.calendarViewDate().getFullYear();
    const month = this.calendarViewDate().getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const today = new Date();
    today.setHours(0,0,0,0);

    const start = new Date(firstDay);
    const dow = firstDay.getDay();
    const offset = dow === 0 ? 6 : dow - 1;
    start.setDate(start.getDate() - offset);

    const days: CalendarDay[] = [];
    const cur = new Date(start);

    while (cur <= lastDay || days.length % 7 !== 0 || days.length < 35) {
      const d = new Date(cur);
      d.setHours(0,0,0,0);

      let isTripStart = false;
      let isTripEnd = false;

      const tripIds = this.putovanja().filter(p => {
        const polaska = new Date(p.departureDate);
        polaska.setHours(0,0,0,0);
        const povratka = p.returnDate ? new Date(p.returnDate) : polaska;
        povratka.setHours(0,0,0,0);
        if (d.getTime() === polaska.getTime()) isTripStart = true;
        if (d.getTime() === povratka.getTime()) isTripEnd = true;
        return d >= polaska && d <= povratka;
      }).map(p => p.id);

      days.push({
        date: d,
        dayNum: d.getDate(),
        currentMonth: d.getMonth() === month,
        isToday: d.getTime() === today.getTime(),
        tripIds,
        isTripStart,
        isTripEnd
      });

      cur.setDate(cur.getDate() + 1);
      if (days.length >= 42) break;
    }
    return days;
  }

  getTripColor(tripId: number) {
    const idx = this.tripColorMap.get(tripId) ?? 0;
    return this.TRIP_COLORS[idx];
  }

  getDayCellBg(day: CalendarDay): string | null {
    if (day.tripIds.length === 0) return null;
    return this.getTripColor(day.tripIds[0]).bg;
  }

  getDayCircleBg(day: CalendarDay): string | null {
    if (day.tripIds.length === 0) return null;
    if (day.isTripStart || day.isTripEnd) return this.getTripColor(day.tripIds[0]).solid;
    return null;
  }

  getDayColorBg(day: CalendarDay): string | null {
    if (day.tripIds.length === 0) return null;
    return this.getTripColor(day.tripIds[0]).bg;
  }

  getDayColorSolid(day: CalendarDay): string | null {
    if (day.tripIds.length === 0) return null;
    return this.getTripColor(day.tripIds[0]).solid;
  }

  predstojecaPutovanja = computed(() => {
    const today = new Date();
    today.setHours(0,0,0,0);
    return this.putovanja()
      .filter(p => new Date(p.departureDate) >= today)
      .sort((a, b) => new Date(a.departureDate).getTime() - new Date(b.departureDate).getTime())
      .slice(0, 5);
  });

  get calendarMonthLabel(): string {
    const d = this.calendarViewDate();
    return `${this.MESECI[d.getMonth()]} ${d.getFullYear()}`;
  }

  ngOnInit() {
    this.loadAnnouncements();
    this.loadTrips();
  }

  loadAnnouncements() {
    this.obavestenjaService.getAll().subscribe({
      next: (list) => this.obavestenja.set(list),
      error: () => {}
    });
  }

  loadTrips() {
    this.putovanjaService.getAll().subscribe({
      next: (list) => {
        this.putovanja.set(list);
        list.forEach((p, i) => this.tripColorMap.set(p.id, i % this.TRIP_COLORS.length));
      },
      error: () => {}
    });
  }

  prevMonth() {
    const d = new Date(this.calendarViewDate());
    d.setMonth(d.getMonth() - 1);
    this.calendarViewDate.set(d);
  }

  nextMonth() {
    const d = new Date(this.calendarViewDate());
    d.setMonth(d.getMonth() + 1);
    this.calendarViewDate.set(d);
  }

  clickDay(day: CalendarDay) {
    if (day.tripIds.length > 0) {
      const trip = this.putovanja().find(p => p.id === day.tripIds[0]);
      if (trip) this.selectedPutovanje.set(trip);
    }
  }

  selectTripForDetail(p: Putovanje) {
    this.selectedPutovanje.set(p);
  }

  backToCalendar() {
    this.selectedPutovanje.set(null);
  }

  submitAnnouncement() {
    if (this.novoObavestenjeForm.invalid) return;
    this.isSaving.set(true);
    const tekst = this.novoObavestenjeForm.getRawValue().tekst;
    this.obavestenjaService.create(tekst).subscribe({
      next: () => {
        this.showForm.set(false);
        this.novoObavestenjeForm.reset();
        this.loadAnnouncements();
        this.isSaving.set(false);
      },
      error: () => this.isSaving.set(false)
    });
  }

  getDaysUntil(dateStr: string): number {
    const today = new Date();
    today.setHours(0,0,0,0);
    const d = new Date(dateStr);
    d.setHours(0,0,0,0);
    return Math.ceil((d.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }

  showWarning(p: Putovanje): boolean {
    return this.getDaysUntil(p.departureDate) < 30 && p.status !== 'CONFIRMED';
  }

  formatDateRange(p: Putovanje): string {
    const polaska = new Date(p.departureDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    if (!p.returnDate) return polaska;
    const povratka = new Date(p.returnDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    return `${polaska}–${povratka}`;
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      IN_PROCESSING: 'U fazi obrade',
      AWAITING_APPROVAL: 'Čeka odobrenje',
      CONFIRMED: 'Odobreno',
      REJECTED: 'Odbijeno',
      COMPLETED: 'Završeno'
    };
    return map[status] ?? status;
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'long', year: 'numeric' });
  }
}
