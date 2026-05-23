import { Component } from '@angular/core';

@Component({
  selector: 'app-analiza-troskova',
  standalone: true,
  template: `
    <div class="placeholder-page">
      <span class="icon">📈</span>
      <h2>Analiza troškova</h2>
      <p>Dostupno u Sprintu 3.</p>
    </div>
  `,
  styles: [`
    .placeholder-page {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 60vh;
      gap: 12px;
      color: #9E9E9E;
    }
    .icon { font-size: 48px; }
    h2 { font-size: 20px; font-weight: 700; color: #424242; margin: 0; }
    p { font-size: 14px; margin: 0; }
  `]
})
export class AnalizaTroskovaComponent {}
