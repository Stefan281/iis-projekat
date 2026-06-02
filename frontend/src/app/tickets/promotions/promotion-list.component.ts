import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Promotion } from '../ticketing.models';
import { PromotionService } from '../promotion.service';

@Component({
  selector: 'app-promotion-list',
  imports: [RouterLink],
  template: `
    <main class="page">
      <header class="page-header">
        <div>
          <p>Ticket sales</p>
          <h1>Promotions</h1>
        </div>
        <a class="button primary" routerLink="/promotions/new">Add promotion</a>
      </header>

      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Discount</th>
            <th>Start date</th>
            <th>End date</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          @for (promotion of promotions(); track promotion.id) {
            <tr>
              <td>{{ promotion.name }}</td>
              <td>{{ promotion.discountPercentage }}%</td>
              <td>{{ promotion.startDate }}</td>
              <td>{{ promotion.endDate }}</td>
              <td>{{ promotion.status }}</td>
              <td class="actions">
                <a routerLink="/promotions/{{ promotion.id }}/edit">Edit</a>
                <button type="button" (click)="delete(promotion.id)">Delete</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </main>
  `
})
export class PromotionListComponent implements OnInit {
  readonly promotions = signal<Promotion[]>([]);

  constructor(private readonly promotionService: PromotionService) {}

  ngOnInit(): void {
    this.load();
  }

  delete(id: number): void {
    this.promotionService.delete(id).subscribe(() => this.load());
  }

  private load(): void {
    this.promotionService.getAll().subscribe((promotions) => this.promotions.set(promotions));
  }
}
