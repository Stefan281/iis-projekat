import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PassengersService } from '../../../../core/services/passengers.service';
import { Passenger } from '../../../../core/models/models';

@Component({
  selector: 'app-passengers-tab',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './passengers-tab.component.html',
  styleUrl: './passengers-tab.component.css'
})
export class PassengersTabComponent implements OnInit {
  @Input() tripId!: number;

  private passengersService = inject(PassengersService);

  passengers = signal<Passenger[]>([]);
  editingRoomFor = signal<number | null>(null);

  ngOnInit() { this.load(); }

  load() {
    this.passengersService.getAll(this.tripId).subscribe({
      next: (rows) => this.passengers.set(rows),
      error: () => {}
    });
  }

  /** Replace a single row (keyed by user id) with the given changes. */
  private patch(userId: number, changes: Partial<Passenger>) {
    this.passengers.update(list =>
      list.map(p => p.id === userId ? { ...p, ...changes } : p)
    );
  }

  // CHECKBOX — toggles membership immediately, no Save button needed
  togglePassenger(passenger: Passenger) {
    const newChecked = !passenger.added;
    this.patch(passenger.id, { added: newChecked }); // optimistic
    this.passengersService.toggle(this.tripId, passenger.id, newChecked).subscribe({
      next: (updated) => this.patch(passenger.id, {
        added: updated.added,
        participantId: updated.participantId ?? undefined,
        sobaBroj: updated.sobaBroj,
        dokumentacijaStatus: updated.dokumentacijaStatus
      }),
      error: () => this.patch(passenger.id, { added: !newChecked }) // revert
    });
  }

  toggleAll(event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    this.passengers().forEach(p => {
      if (p.added !== checked) this.togglePassenger(p);
    });
  }

  get allChecked(): boolean {
    return this.passengers().length > 0 && this.passengers().every(p => p.added);
  }

  // ROOM NUMBER — inline edit, auto-save on blur/Enter
  startEditRoom(id: number) {
    this.editingRoomFor.set(id);
  }

  saveRoom(passenger: Passenger, roomNumber: string) {
    this.editingRoomFor.set(null);
    if (!passenger.participantId) return; // room only applies to members on the trip
    const trimmed = roomNumber?.trim() ?? '';
    this.patch(passenger.id, { sobaBroj: trimmed }); // optimistic
    this.passengersService.updateRoom(this.tripId, passenger.participantId, trimmed).subscribe({
      next: (updated) => this.patch(passenger.id, { sobaBroj: updated.sobaBroj }),
      error: () => {}
    });
  }

  // DOCUMENTATION — dropdown, auto-save on change (works for members and non-members)
  onDocStatusChange(passenger: Passenger) {
    const status = passenger.dokumentacijaStatus ?? 'TO_CHECK';
    if (passenger.participantId) {
      this.passengersService.updateDocumentation(this.tripId, passenger.participantId, status)
        .subscribe({ error: () => {} });
    } else {
      this.passengersService.setDocumentationForNonParticipant(this.tripId, passenger.id, status)
        .subscribe({
          next: (updated) => this.patch(passenger.id, {
            participantId: updated.participantId ?? undefined
          }),
          error: () => {}
        });
    }
  }

  documentStatusClass(status?: string): string {
    switch (status) {
      case 'COMPLETE': return 'doc-complete';
      case 'MISSING': return 'doc-missing';
      default: return 'doc-to-check';
    }
  }
}
