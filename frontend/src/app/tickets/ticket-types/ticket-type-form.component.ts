import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TicketTypeService } from '../ticket-type.service';

@Component({
  selector: 'app-ticket-type-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="page narrow">
      <header class="page-header">
        <div>
          <p>Ticket type</p>
          <h1>{{ isEditMode() ? 'Edit ticket type' : 'Add ticket type' }}</h1>
        </div>
        <a class="button" routerLink="/ticket-types">Back</a>
      </header>

      <form [formGroup]="form" (ngSubmit)="save()" class="entity-form">
        <label>Name <input type="text" formControlName="name" /></label>
        <label>Description <textarea formControlName="description"></textarea></label>
        <label>Coefficient <input type="number" min="0" step="0.01" formControlName="coefficient" /></label>
        <button class="button primary" type="submit" [disabled]="form.invalid">Save</button>
      </form>
    </main>
  `
})
export class TicketTypeFormComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  readonly isEditMode = signal(false);
  private ticketTypeId: number | null = null;

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    coefficient: [1, [Validators.required, Validators.min(0)]]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly ticketTypeService: TicketTypeService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.ticketTypeId = Number(id);
    this.isEditMode.set(true);
    this.ticketTypeService.getById(this.ticketTypeId).subscribe((ticketType) => this.form.patchValue(ticketType));
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request = this.form.getRawValue();
    const operation = this.ticketTypeId
      ? this.ticketTypeService.update(this.ticketTypeId, request)
      : this.ticketTypeService.create(request);

    operation.subscribe(() => this.router.navigateByUrl('/ticket-types'));
  }
}
