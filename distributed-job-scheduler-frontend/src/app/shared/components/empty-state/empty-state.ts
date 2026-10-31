import { Component, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  imports: [MatButtonModule, MatIconModule],
  selector: 'app-empty-state',
  styleUrl: './empty-state.css',
  templateUrl: './empty-state.html',
})
export class EmptyState {
  title = input<string>('Nothing here yet');
  message = input<string>('There is no data to display.');
  icon = input<string>('inbox');
  actionLabel = input<string>('');
}
