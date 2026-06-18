import { Component } from '@angular/core';
import { InboxComponent } from '../../features/inbox/inbox.component';

@Component({
  selector: 'app-director-inbox',
  standalone: true,
  imports: [InboxComponent],
  template: '<app-inbox />'
})
export class DirectorInboxComponent {}
