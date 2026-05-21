import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InboxService } from '../../core/services/inbox.service';
import { Poruka } from '../../core/models/models';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.css'
})
export class InboxComponent implements OnInit {
  private inboxService = inject(InboxService);
  private fb = inject(FormBuilder);

  poruke = signal<Poruka[]>([]);
  replyTo = signal<number | null>(null);
  isSending = signal(false);

  replyForm = this.fb.nonNullable.group({
    tekst: ['', Validators.required]
  });

  ngOnInit() { this.load(); }

  load() {
    this.inboxService.getInbox().subscribe({
      next: (list) => this.poruke.set(list),
      error: () => {}
    });
  }

  openReply(id: number) {
    this.replyTo.set(id);
    this.replyForm.reset();
    this.inboxService.oznacProcitano(id).subscribe({ error: () => {} });
  }

  sendReply() {
    if (this.replyForm.invalid || !this.replyTo()) return;
    this.isSending.set(true);
    const tekst = this.replyForm.getRawValue().tekst;
    this.inboxService.odgovori(this.replyTo()!, tekst).subscribe({
      next: () => { this.replyTo.set(null); this.replyForm.reset(); this.load(); this.isSending.set(false); },
      error: () => this.isSending.set(false)
    });
  }

  formatDatum(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'long', year: 'numeric' });
  }

  unreadCount(): number {
    return this.poruke().filter(p => !p.procitana).length;
  }
}
