import { ChangeDetectorRef, Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InboxService, Conversation, ChatMessage, UserOption } from '../../core/services/inbox.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.css'
})
export class InboxComponent implements OnInit, OnDestroy {
  private inboxService = inject(InboxService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  view: 'list' | 'chat' = 'list';
  conversations: Conversation[] = [];
  messages: ChatMessage[] = [];
  allUsers: UserOption[] = [];
  showUserPicker = false;
  selectedUser: Conversation | null = null;
  newMessage = '';
  currentUserId: number = 0;
  private pollTimer: any;

  ngOnInit() {
    this.currentUserId = this.authService.getStoredUser()?.id ?? 0;
    this.loadConversations();
  }

  loadConversations() {
    this.inboxService.getConversations().subscribe(data => {
      this.conversations = data;
      this.cdr.markForCheck();
    });
  }

  openChat(user: Conversation) {
    clearInterval(this.pollTimer);
    this.selectedUser = user;
    this.showUserPicker = false;
    this.view = 'chat';
    this.messages = [];
    this.loadMessages();
    this.pollTimer = setInterval(() => this.loadMessages(), 3000);
  }

  loadMessages() {
    if (!this.selectedUser) return;
    this.inboxService.getChat(this.selectedUser.userId).subscribe(msgs => {
      this.messages = msgs;
      this.cdr.markForCheck();
      setTimeout(() => this.scrollToBottom(), 50);
    });
  }

  scrollToBottom() {
    const list = document.querySelector('.message-list');
    if (list) list.scrollTop = list.scrollHeight;
  }

  backToList() {
    clearInterval(this.pollTimer);
    this.view = 'list';
    this.selectedUser = null;
    this.messages = [];
    this.loadConversations();
  }

  send() {
    const text = this.newMessage.trim();
    if (!text || !this.selectedUser) return;
    this.newMessage = '';
    this.inboxService.send(this.selectedUser.userId, text).subscribe(() => {
      this.loadMessages();
    });
  }

  openUserPicker() {
    if (this.showUserPicker) {
      this.showUserPicker = false;
      return;
    }
    this.inboxService.getAllUsers().subscribe(users => {
      this.allUsers = users;
      this.showUserPicker = true;
      this.cdr.markForCheck();
    });
  }

  startNewChat(user: UserOption) {
    const conv: Conversation = {
      userId: user.userId,
      firstName: user.firstName,
      lastName: user.lastName,
      unreadCount: 0
    };
    this.openChat(conv);
  }

  isMyMessage(msg: ChatMessage): boolean {
    return msg.senderId === this.currentUserId;
  }

  ngOnDestroy() {
    clearInterval(this.pollTimer);
  }
}
