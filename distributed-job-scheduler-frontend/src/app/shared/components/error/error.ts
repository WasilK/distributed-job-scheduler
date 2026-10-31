import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  imports: [MatButtonModule, MatIconModule],
  selector: 'app-error',
  styleUrl: './error.css',
  templateUrl: './error.html',
})
export class Error {
  message = input<string>('Something went wrong. Please try again.');
  retry = output<void>();
}
