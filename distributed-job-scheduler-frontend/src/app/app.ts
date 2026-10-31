import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DashboardLayout } from './layout/dashboard-layout/dashboard-layout';

@Component({
  imports: [RouterOutlet, DashboardLayout],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('distributed-job-scheduler-frontend');
}
