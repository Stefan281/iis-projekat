import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PromotionService } from '../promotion.service';
import { PromotionStatus } from '../ticketing.models';

@Component({
  selector: 'app-promotion-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="page narrow">
      <header class="page-header">
        <div>
          <p>Promotion</p>
          <h1>{{ isEditMode() ? 'Edit promotion' : 'Add promotion' }}</h1>
        </div>
        <a class="button" routerLink="/promotions">Back</a>
      </header>

      <form [formGroup]="form" (ngSubmit)="save()" class="entity-form">
        <label>Name <input type="text" formControlName="name" /></label>
        <label>Discount percentage <input type="number" min="0" step="0.01" formControlName="discountPercentage" /></label>
        <label>Start date <input type="date" formControlName="startDate" /></label>
        <label>End date <input type="date" formControlName="endDate" /></label>
        <label>Status
          <select formControlName="status">
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
            <option value="EXPIRED">EXPIRED</option>
          </select>
        </label>
        <button class="button primary" type="submit" [disabled]="form.invalid">Save</button>
      </form>
    </main>
  `
})
export class PromotionFormComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  readonly isEditMode = signal(false);
  private promotionId: number | null = null;

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    discountPercentage: [0, [Validators.required, Validators.min(0)]],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    status: ['ACTIVE' as PromotionStatus, Validators.required]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly promotionService: PromotionService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.promotionId = Number(id);
    this.isEditMode.set(true);
    this.promotionService.getById(this.promotionId).subscribe((promotion) => this.form.patchValue(promotion));
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request = this.form.getRawValue();
    const operation = this.promotionId
      ? this.promotionService.update(this.promotionId, request)
      : this.promotionService.create(request);

    operation.subscribe(() => this.router.navigateByUrl('/promotions'));
  }
}
