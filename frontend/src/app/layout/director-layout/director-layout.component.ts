import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { InboxService } from '../../core/services/inbox.service';

@Component({
  selector: 'app-director-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './director-layout.component.html',
  styleUrl: './director-layout.component.css'
})
export class DirectorLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private inboxService = inject(InboxService);
  private router = inject(Router);

  user = this.authService.currentUser;
  unreadCount = signal(0);

  ngOnInit() {
    this.inboxService.getUnreadCount().subscribe({
      next: (r) => this.unreadCount.set(r.count),
      error: () => {}
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
