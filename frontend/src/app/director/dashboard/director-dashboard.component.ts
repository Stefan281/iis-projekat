import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationsService } from '../../core/services/notifications.service';
import { TripsService } from '../../core/services/trips.service';
import { AccommodationService } from '../../core/services/accommodation.service';
import { TransportService } from '../../core/services/transport.service';
import { Announcement, AccommodationOffer, TransportOffer, Trip } from '../../core/models/models';

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
  selector: 'app-director-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './director-dashboard.component.html',
  styleUrl: './director-dashboard.component.css'
})
export class DirectorDashboardComponent implements OnInit {
  private announcementsService = inject(NotificationsService);
  private tripsService = inject(TripsService);
  private accommodationService = inject(AccommodationService);
  private transportService = inject(TransportService);

  readonly MONTHS = [
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

  announcements = signal<Announcement[]>([]);
  trips = signal<Trip[]>([]);
  selectedTrip = signal<Trip | null>(null);
  calendarViewDate = signal(new Date());

  selectedAccommodation = signal<AccommodationOffer | null>(null);
  selectedTransport = signal<TransportOffer | null>(null);
  loadingDetail = signal(false);

  showRejectForm = signal(false);
  rejectReason = '';
  isActing = signal(false);

  tripColorMap = new Map<number, number>();

  get calendarMonthLabel(): string {
    const d = this.calendarViewDate();
    return `${this.MONTHS[d.getMonth()]} ${d.getFullYear()}`;
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

      const tripIds = this.trips().filter(p => {
        const departure = new Date(p.departureDate);
        departure.setHours(0, 0, 0, 0);
        const ret = p.returnDate ? new Date(p.returnDate) : departure;
        ret.setHours(0, 0, 0, 0);
        if (d.getTime() === departure.getTime()) isTripStart = true;
        if (d.getTime() === ret.getTime()) isTripEnd = true;
        return d >= departure && d <= ret;
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

  upcomingTrips = computed(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return this.trips()
      .filter(p => new Date(p.departureDate) >= today)
      .sort((a, b) => new Date(a.departureDate).getTime() - new Date(b.departureDate).getTime());
  });

  ngOnInit() {
    this.loadAnnouncements();
    this.loadTrips();
  }

  loadAnnouncements() {
    this.announcementsService.getAll().subscribe({
      next: (list) => this.announcements.set(list),
      error: () => {}
    });
  }

  loadTrips() {
    this.tripsService.getAll().subscribe({
      next: (list) => {
        this.trips.set(list);
        list.forEach((p, i) => this.tripColorMap.set(p.id, i % this.TRIP_COLORS.length));
      },
      error: () => {}
    });
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
      const trip = this.trips().find(p => p.id === day.tripIds[0]);
      if (trip) this.openDetail(trip);
    }
  }

  openDetail(p: Trip) {
    this.selectedTrip.set(p);
    this.selectedAccommodation.set(null);
    this.selectedTransport.set(null);
    this.showRejectForm.set(false);
    this.rejectReason = '';
    this.loadingDetail.set(true);

    this.accommodationService.getSelected(p.id).subscribe({
      next: (s) => this.selectedAccommodation.set(s),
      error: () => {}
    });

    this.transportService.getSelected(p.id).subscribe({
      next: (t) => { this.selectedTransport.set(t); this.loadingDetail.set(false); },
      error: () => this.loadingDetail.set(false)
    });
  }

  backToCalendar() {
    this.selectedTrip.set(null);
    this.showRejectForm.set(false);
    this.rejectReason = '';
  }

  approve() {
    const p = this.selectedTrip();
    if (!p) return;
    this.isActing.set(true);
    this.tripsService.updateStatus(p.id, 'CONFIRMED').subscribe({
      next: () => {
        this.trips.update(list => list.map(t => t.id === p.id ? { ...t, status: 'CONFIRMED' } : t));
        this.selectedTrip.set({ ...p, status: 'CONFIRMED' });
        this.isActing.set(false);
      },
      error: () => this.isActing.set(false)
    });
  }

  reject() {
    const p = this.selectedTrip();
    if (!p || !this.rejectReason.trim()) return;
    const reason = this.rejectReason.trim();
    this.isActing.set(true);
    this.tripsService.updateStatus(p.id, 'REJECTED', reason).subscribe({
      next: () => {
        this.trips.update(list => list.map(t => t.id === p.id ? { ...t, status: 'REJECTED', razlogOdbijanja: reason } : t));
        this.selectedTrip.set({ ...p, status: 'REJECTED', razlogOdbijanja: reason });
        this.showRejectForm.set(false);
        this.rejectReason = '';
        this.isActing.set(false);
      },
      error: () => this.isActing.set(false)
    });
  }

  totalCost(): number {
    const s = this.selectedAccommodation();
    const t = this.selectedTransport();
    return (s?.cena ?? 0) + (t?.cena ?? 0);
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

  formatDateRange(p: Trip): string {
    const departure = new Date(p.departureDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    if (!p.returnDate) return departure;
    const ret = new Date(p.returnDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    return `${departure}–${ret}`;
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'long', year: 'numeric' });
  }

  formatDateShort(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric', year: 'numeric' });
  }

  getDaysUntil(dateStr: string): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const d = new Date(dateStr);
    d.setHours(0, 0, 0, 0);
    return Math.ceil((d.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }

  getTransportIcon(type: string): string {
    const map: Record<string, string> = {
      'AVION': '✈️', 'AUTOBUS': '🚌', 'KOMBI': '🚐', 'VOZ': '🚆', 'BROD': '🚢'
    };
    return map[type?.toUpperCase()] ?? '🚌';
  }

  getTransportLabel(type: string): string {
    const map: Record<string, string> = {
      'AVION': 'Avion', 'AUTOBUS': 'Autobus', 'KOMBI': 'Kombi', 'VOZ': 'Voz', 'BROD': 'Brod'
    };
    return map[type?.toUpperCase()] ?? type;
  }
}
