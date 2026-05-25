import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { InboxService } from '../../core/services/inbox.service';

@Component({
  selector: 'app-team-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './team-layout.component.html',
  styleUrl: './team-layout.component.css'
})
export class TeamLayoutComponent implements OnInit {
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

  getRoleLabel(role: string | undefined): string {
    const map: Record<string, string> = {
      'IGRAC': 'Igrač',
      'STATISTICAR': 'Statističar',
      'STRUCNI_STAB': 'Stručni štab'
    };
    return map[role ?? ''] ?? 'Član tima';
  }

  logout() {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
