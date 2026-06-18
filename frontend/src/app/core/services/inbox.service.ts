import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Conversation {
  userId: number;
  firstName: string;
  lastName: string;
  unreadCount: number;
}

export interface ChatMessage {
  id: number;
  senderId: number;
  text: string;
  sentAt: string;
}

export interface UserOption {
  userId: number;
  firstName: string;
  lastName: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class InboxService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/messages';

  getConversations() {
    return this.http.get<Conversation[]>(`${this.base}/conversations`);
  }

  getChat(otherUserId: number) {
    return this.http.get<ChatMessage[]>(`${this.base}/chat/${otherUserId}`);
  }

  send(recipientId: number, text: string) {
    return this.http.post<ChatMessage>(`${this.base}/send`, { recipientId, text });
  }

  getAllUsers() {
    return this.http.get<UserOption[]>(`${this.base}/users`);
  }

  getUnreadCount() {
    return this.http.get<{ count: number }>(`${this.base}/unread-count`);
  }
}
