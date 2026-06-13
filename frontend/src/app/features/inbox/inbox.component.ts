import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InboxService } from '../../core/services/inbox.service';
import { Message } from '../../core/models/models';

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

  messages = signal<Message[]>([]);
  replyTo = signal<number | null>(null);
  isSending = signal(false);

  replyForm = this.fb.nonNullable.group({
    text: ['', Validators.required]
  });

  ngOnInit() { this.load(); }

  load() {
    this.inboxService.getMessages().subscribe({
      next: (list) => this.messages.set(list),
      error: () => {}
    });
  }

  openReply(id: number) {
    this.replyTo.set(id);
    this.replyForm.reset();
    this.inboxService.markAsRead(id).subscribe({ error: () => {} });
  }

  sendReply() {
    if (this.replyForm.invalid || !this.replyTo()) return;
    this.isSending.set(true);
    const text = this.replyForm.getRawValue().text;
    this.inboxService.reply(this.replyTo()!, text).subscribe({
      next: () => { this.replyTo.set(null); this.replyForm.reset(); this.load(); this.isSending.set(false); },
      error: () => this.isSending.set(false)
    });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('sr-RS', { day: 'numeric', month: 'long', year: 'numeric' });
  }

  unreadCount(): number {
    return this.messages().filter(p => !p.procitana).length;
  }
}
