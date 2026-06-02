import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { UserRole } from '../auth/auth.models';

interface NavItem {
  label: string;
  path: string;
  icon: string;
  roles: UserRole[];
}

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.css'
})
export class AppShellComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.authService.currentUser;
  readonly currentUrl = signal(this.router.url);

  readonly navItems: NavItem[] = [
    { label: 'Pocetna', path: '/dashboard', icon: 'H', roles: ['CUSTOMER', 'MANAGER', 'ADMIN', 'STATISTICAR', 'STRUCNI_STAB'] },
    { label: 'Utakmice', path: '/matches', icon: 'U', roles: ['CUSTOMER', 'MANAGER', 'ADMIN', 'STATISTICAR', 'STRUCNI_STAB'] },
    { label: 'Moje karte', path: '/my-tickets', icon: 'K', roles: ['CUSTOMER'] },
    { label: 'Moje rezervacije', path: '/my-reservations', icon: 'R', roles: ['CUSTOMER'] },
    { label: 'Rezervacije', path: '/my-reservations', icon: 'R', roles: ['MANAGER', 'ADMIN', 'STATISTICAR', 'STRUCNI_STAB'] },
    { label: 'Sedista i zone', path: '/seats', icon: 'S', roles: ['MANAGER', 'STATISTICAR', 'STRUCNI_STAB'] },
    { label: 'Zone', path: '/zones', icon: 'Z', roles: ['ADMIN'] },
    { label: 'Sedista', path: '/seats', icon: 'S', roles: ['ADMIN'] }
  ];

  readonly visibleNavItems = computed(() => {
    const role = this.user()?.role;
    return role ? this.navItems.filter((item) => item.roles.includes(role)) : [];
  });

  readonly pageTitle = computed(() => {
    const role = this.user()?.role;
    const url = this.currentUrl();

    if (url.startsWith('/matches')) return 'Utakmice';
    if (url.startsWith('/my-tickets')) return 'Moje karte';
    if (url.startsWith('/my-reservations')) return role === 'CUSTOMER' ? 'Moje rezervacije' : 'Rezervacije';
    if (url.startsWith('/seats')) return 'Upravljanje sedistima';
    if (url.startsWith('/zones')) return 'Zone';

    if (role === 'ADMIN') return 'Administratorski panel';
    if (role === 'MANAGER' || role === 'STATISTICAR' || role === 'STRUCNI_STAB') return 'Menadzerski panel';
    return `Dobro jutro, ${this.user()?.firstName ?? 'kupac'}`;
  });

  readonly pageDescription = computed(() => {
    const role = this.user()?.role;
    const url = this.currentUrl();

    if (url.startsWith('/matches')) return 'Pregled i kupovina karata za utakmice.';
    if (url.startsWith('/my-tickets')) return 'Pregled svih kupljenih karata.';
    if (url.startsWith('/my-reservations')) return 'Pregled aktivnih i istorijskih rezervacija.';
    if (url.startsWith('/seats')) return 'Postavljanje kapaciteta i cena po zonama.';
    if (url.startsWith('/zones')) return 'Upravljanje zonama hale.';

    if (role === 'ADMIN') return 'Pregled kljucnih metrika sistema.';
    if (role === 'MANAGER' || role === 'STATISTICAR' || role === 'STRUCNI_STAB') return 'Pregled prodaje i aktivnosti.';
    return 'Evo pregleda danasnjih aktivnosti i utakmica.';
  });

  constructor() {
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => this.currentUrl.set(event.urlAfterRedirects));
  }

  initials(): string {
    const user = this.user();
    if (!user) return '';
    return `${user.firstName[0] ?? ''}${user.lastName[0] ?? ''}`.toUpperCase();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
