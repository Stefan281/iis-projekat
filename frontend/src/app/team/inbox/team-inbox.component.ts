import { Component } from '@angular/core';

@Component({
  selector: 'app-team-inbox',
  standalone: true,
  template: `
    <div class="placeholder-page">
      <span class="material-icons">inbox</span>
      <h2>Inbox</h2>
      <p>Poruke i zahtevi – dostupno u Sprintu 2.</p>
    </div>
  `,
  styles: [`
    .placeholder-page {
      padding: 48px 24px;
      text-align: center;
      color: #757575;
    }
    .placeholder-page .material-icons {
      font-size: 64px;
      color: #BDBDBD;
      margin-bottom: 12px;
    }
    .placeholder-page h2 {
      font-size: 20px;
      font-weight: 700;
      color: #212121;
      margin: 0 0 8px 0;
    }
    .placeholder-page p {
      font-size: 14px;
      margin: 0;
    }
  `]
})
export class TeamInboxComponent {}
