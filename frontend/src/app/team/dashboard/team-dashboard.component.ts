import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationsService } from '../../core/services/notifications.service';
import { TripsService, RoomInfo } from '../../core/services/trips.service';
import { AccommodationService } from '../../core/services/accommodation.service';
import { TransportService } from '../../core/services/transport.service';
import { Obavestenje, PonudaSmestaja, PonudaTransporta, Putovanje } from '../../core/models/models';

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
  selector: 'app-team-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './team-dashboard.component.html',
  styleUrl: './team-dashboard.component.css'
})
export class TeamDashboardComponent implements OnInit {
  private obavestenjaService = inject(NotificationsService);
  private putovanjaService = inject(TripsService);
  private smestajService = inject(AccommodationService);
  private transportService = inject(TransportService);

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

  obavestenja = signal<Obavestenje[]>([]);
  putovanja = signal<Putovanje[]>([]);
  selectedPutovanje = signal<Putovanje | null>(null);
  calendarViewDate = signal(new Date());

  selectedSmestaj = signal<PonudaSmestaja | null>(null);
  selectedTransport = signal<PonudaTransporta | null>(null);
  roomInfo = signal<RoomInfo | null>(null);
  loadingDetail = signal(false);

  tripColorMap = new Map<number, number>();

  get calendarMonthLabel(): string {
    const d = this.calendarViewDate();
    return `${this.MESECI[d.getMonth()]} ${d.getFullYear()}`;
  }

  get calendarDays(): CalendarDay[] {
    const year = this.calendarViewDate().getFullYear();
    const month = this.calendarViewDate().getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const start = new Date(firstDay);
    const dow = firstDay.getDay();
    const offset = dow === 0 ? 6 : dow - 1;
    start.setDate(start.getDate() - offset);

    const days: CalendarDay[] = [];
    const cur = new Date(start);

    while (cur <= lastDay || days.length % 7 !== 0 || days.length < 35) {
      const d = new Date(cur);
      d.setHours(0, 0, 0, 0);

      let isTripStart = false;
      let isTripEnd = false;

      const tripIds = this.putovanja().filter(p => {
        const polaska = new Date(p.departureDate);
        polaska.setHours(0, 0, 0, 0);
        const povratka = p.returnDate ? new Date(p.returnDate) : polaska;
        povratka.setHours(0, 0, 0, 0);
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

  predstojecaPutovanja = computed(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return this.putovanja()
      .filter(p => new Date(p.departureDate) >= today)
      .sort((a, b) => new Date(a.departureDate).getTime() - new Date(b.departureDate).getTime())
      .slice(0, 5);
  });

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

  getTripColor(tripId: number) {
    const idx = this.tripColorMap.get(tripId) ?? 0;
    return this.TRIP_COLORS[idx];
  }

  getDayColorBg(day: CalendarDay): string | null {
    if (day.tripIds.length === 0) return null;
    return this.getTripColor(day.tripIds[0]).bg;
  }

  getDayColorSolid(day: CalendarDay): string | null {
    if (day.tripIds.length === 0) return null;
    return this.getTripColor(day.tripIds[0]).solid;
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
      if (trip) this.openDetail(trip);
    }
  }

  openDetail(p: Putovanje) {
    this.selectedPutovanje.set(p);
    this.selectedSmestaj.set(null);
    this.selectedTransport.set(null);
    this.roomInfo.set(null);
    this.loadingDetail.set(true);

    this.smestajService.getSelected(p.id).subscribe({
      next: (s) => this.selectedSmestaj.set(s),
      error: () => {}
    });

    this.transportService.getSelected(p.id).subscribe({
      next: (t) => { this.selectedTransport.set(t); this.loadingDetail.set(false); },
      error: () => this.loadingDetail.set(false)
    });

    this.putovanjaService.getRoomAssignment(p.id).subscribe({
      next: (r) => this.roomInfo.set(r)
    });
  }

  backToCalendar() {
    this.selectedPutovanje.set(null);
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

  formatDateRange(p: Putovanje): string {
    const polaska = new Date(p.departureDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    if (!p.returnDate) return polaska;
    const povratka = new Date(p.returnDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    return `${polaska}–${povratka}`;
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'long', year: 'numeric' });
  }

  formatDateShort(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric', year: 'numeric' });
  }
}
