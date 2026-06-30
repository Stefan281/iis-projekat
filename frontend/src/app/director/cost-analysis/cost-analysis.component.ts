import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CostAnalysisService } from '../../core/services/cost-analysis.service';

interface MonthOption {
  value: number;
  label: string;
}

@Component({
  selector: 'app-cost-analysis',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cost-analysis.component.html',
  styleUrl: './cost-analysis.component.css'
})
export class CostAnalysisComponent implements OnInit {
  private costAnalysisService = inject(CostAnalysisService);
  private changeDetector = inject(ChangeDetectorRef);

  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = 0;
  displayedYear: number = new Date().getFullYear();
  displayedMonth: number = 0;
  pendingYear: number = new Date().getFullYear();
  pendingMonth: number = 0;
  data: any = null;
  errorMessage: string = '';

  years: number[] = [];

  months: MonthOption[] = [
    { value: 0, label: 'Svi meseci' },
    { value: 1, label: 'Januar' },
    { value: 2, label: 'Februar' },
    { value: 3, label: 'Mart' },
    { value: 4, label: 'April' },
    { value: 5, label: 'Maj' },
    { value: 6, label: 'Jun' },
    { value: 7, label: 'Jul' },
    { value: 8, label: 'Avgust' },
    { value: 9, label: 'Septembar' },
    { value: 10, label: 'Oktobar' },
    { value: 11, label: 'Novembar' },
    { value: 12, label: 'Decembar' }
  ];

  ngOnInit() {
    this.selectedYear = new Date().getFullYear();
    this.selectedMonth = 0;
    this.displayedYear = this.selectedYear;
    this.displayedMonth = this.selectedMonth;
    this.buildYears();
    this.fetchData(this.selectedYear, this.selectedMonth);
  }

  buildYears() {
    const current = new Date().getFullYear();
    for (let y = current + 1; y >= current - 5; y--) {
      this.years.push(y);
    }
  }

  fetchData(year: number, month: number) {
    this.pendingYear = year;
    this.pendingMonth = month;
    this.costAnalysisService.getCostAnalysis(year, month).subscribe({
      next: this.onFetchSuccess.bind(this),
      error: this.onFetchError.bind(this)
    });
  }

  onFetchSuccess(response: any) {
    this.data = response;
    this.displayedYear = this.pendingYear;
    this.displayedMonth = this.pendingMonth;
    this.errorMessage = '';
    this.changeDetector.detectChanges();
  }

  onFetchError(error: any) {
    this.errorMessage = 'Greška pri učitavanju podataka.';
    this.changeDetector.detectChanges();
  }

  onShowClick() {
    this.fetchData(this.selectedYear, this.selectedMonth);
  }

  downloadPdf() {
    this.costAnalysisService.downloadReport(this.displayedYear, this.displayedMonth).subscribe({
      next: this.onPdfReady.bind(this),
      error: this.onPdfError.bind(this)
    });
  }

  onPdfReady(blob: Blob) {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = this.reportFileName();
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  onPdfError(error: any) {
    this.errorMessage = 'Greška pri generisanju izveštaja.';
    this.changeDetector.detectChanges();
  }

  reportFileName(): string {
    if (this.displayedMonth <= 0) {
      return 'Izvestaj-' + this.displayedYear + '.pdf';
    }
    return 'Izvestaj-' + this.monthLabel(this.displayedMonth) + '-' + this.displayedYear + '.pdf';
  }

  monthLabel(value: number): string {
    for (let i = 0; i < this.months.length; i++) {
      if (this.months[i].value === value) {
        return this.months[i].label;
      }
    }
    return '';
  }

  periodLabel(): string {
    if (this.displayedMonth <= 0) {
      return 'Godišnji prikaz - ' + this.displayedYear + '.';
    }
    return this.monthLabel(this.displayedMonth) + ' ' + this.displayedYear + '.';
  }

  getMaxMonthly(): number {
    if (!this.data) {
      return 0;
    }
    const months = this.data.monthlyBreakdown;
    let max = 0;
    for (let i = 0; i < months.length; i++) {
      if (months[i].totalCost > max) {
        max = months[i].totalCost;
      }
    }
    return max;
  }

  getMaxTrip(): number {
    if (!this.data) {
      return 0;
    }
    const trips = this.data.trips;
    let max = 0;
    for (let i = 0; i < trips.length; i++) {
      if (trips[i].totalCost > max) {
        max = trips[i].totalCost;
      }
    }
    return max;
  }

  getBarHeight(value: number, maxValue: number): string {
    if (maxValue <= 0) {
      return '4px';
    }
    return Math.max(4, (value / maxValue) * 120) + 'px';
  }

  getBarWidth(value: number, maxValue: number): string {
    if (maxValue <= 0) {
      return '4px';
    }
    return Math.max(4, (value / maxValue) * 300) + 'px';
  }

  formatMoney(value: number): string {
    return Math.round(value) + ' €';
  }

  formatDates(trip: any): string {
    if (!trip.returnDateFormatted) {
      return trip.departureDateFormatted;
    }
    return trip.departureDateFormatted + ' - ' + trip.returnDateFormatted;
  }

  monthAbbrev(label: string): string {
    return label.substring(0, 3);
  }
}
