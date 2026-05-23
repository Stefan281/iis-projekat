import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { InboxService } from '../../core/services/inbox.service';

@Component({
  selector: 'app-direktor-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './direktor-layout.component.html',
  styleUrl: './direktor-layout.component.css'
})
export class DirektorLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private inboxService = inject(InboxService);
  private router = inject(Router);

  user = this.authService.currentUser;
  unreadCount = signal(0);

  ngOnInit() {
    this.inboxService.oznaciBroj().subscribe({
      next: (r) => this.unreadCount.set(r.count),
      error: () => {}
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
