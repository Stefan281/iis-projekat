import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationsService } from '../../core/services/notifications.service';
import { TripsService } from '../../core/services/trips.service';
import { AccommodationService } from '../../core/services/accommodation.service';
import { TransportService } from '../../core/services/transport.service';
import { InboxService } from '../../core/services/inbox.service';
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
  selector: 'app-direktor-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './direktor-dashboard.component.html',
  styleUrl: './direktor-dashboard.component.css'
})
export class DirektorDashboardComponent implements OnInit {
  private obavestenjaService = inject(NotificationsService);
  private putovanjaService = inject(TripsService);
  private smestajService = inject(AccommodationService);
  private transportService = inject(TransportService);
  private inboxService = inject(InboxService);

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
  loadingDetail = signal(false);

  showRejectForm = signal(false);
  rejectReason = '';
  isActing = signal(false);

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
    this.loadObavestenja();
    this.loadPutovanja();
  }

  loadObavestenja() {
    this.obavestenjaService.getAll().subscribe({
      next: (list) => this.obavestenja.set(list),
      error: () => {}
    });
  }

  loadPutovanja() {
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

  getDayCellBg(day: CalendarDay): string | null {
    if (day.tripIds.length === 0) return null;
    return this.getTripColor(day.tripIds[0]).bg;
  }

  getDayCircleBg(day: CalendarDay): string | null {
    if (day.tripIds.length === 0) return null;
    if (day.isTripStart || day.isTripEnd) return this.getTripColor(day.tripIds[0]).solid;
    return null;
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
    this.showRejectForm.set(false);
    this.rejectReason = '';
    this.loadingDetail.set(true);

    this.smestajService.getIzabran(p.id).subscribe({
      next: (s) => this.selectedSmestaj.set(s),
      error: () => {}
    });

    this.transportService.getIzabran(p.id).subscribe({
      next: (t) => { this.selectedTransport.set(t); this.loadingDetail.set(false); },
      error: () => this.loadingDetail.set(false)
    });
  }

  backToCalendar() {
    this.selectedPutovanje.set(null);
    this.showRejectForm.set(false);
    this.rejectReason = '';
  }

  odobri() {
    const p = this.selectedPutovanje();
    if (!p) return;
    this.isActing.set(true);
    this.putovanjaService.updateStatus(p.id, 'CONFIRMED').subscribe({
      next: () => {
        this.putovanja.update(list => list.map(t => t.id === p.id ? { ...t, status: 'CONFIRMED' } : t));
        this.selectedPutovanje.set({ ...p, status: 'CONFIRMED' });
        this.isActing.set(false);
      },
      error: () => this.isActing.set(false)
    });
  }

  odbij() {
    const p = this.selectedPutovanje();
    if (!p || !this.rejectReason.trim()) return;
    this.isActing.set(true);
    this.putovanjaService.updateStatus(p.id, 'REJECTED').subscribe({
      next: () => {
        // TODO: send rejection message when backend inbox endpoint is ready
        if (p.organizatorId) {
          this.inboxService.posalji({ primalacId: p.organizatorId, tekst: this.rejectReason, putovanjeId: p.id }).subscribe({ error: () => {} });
        }
        this.putovanja.update(list => list.map(t => t.id === p.id ? { ...t, status: 'REJECTED' } : t));
        this.selectedPutovanje.set({ ...p, status: 'REJECTED' });
        this.showRejectForm.set(false);
        this.rejectReason = '';
        this.isActing.set(false);
      },
      error: () => this.isActing.set(false)
    });
  }

  ukupniTrosak(): number {
    const s = this.selectedSmestaj();
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

  formatDateRange(p: Putovanje): string {
    const polaska = new Date(p.departureDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    if (!p.returnDate) return polaska;
    const povratka = new Date(p.returnDate).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric' });
    return `${polaska}–${povratka}`;
  }

  formatDatum(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'long', year: 'numeric' });
  }

  formatDatumKratko(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'numeric', year: 'numeric' });
  }

  getDaysUntil(dateStr: string): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const d = new Date(dateStr);
    d.setHours(0, 0, 0, 0);
    return Math.ceil((d.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }
}
