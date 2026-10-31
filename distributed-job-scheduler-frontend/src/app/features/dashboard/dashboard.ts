import { Component } from '@angular/core';
import { Loading } from '../../shared/components/loading/loading';
import { Error } from '../../shared/components/error/error';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { environment } from '../../../environments/environment';
@Component({
  imports: [Loading, Error, EmptyState],
  selector: 'app-dashboard',
  styleUrl: './dashboard.css',
  templateUrl: './dashboard.html',
})
export class Dashboard {
  handleRetry(): void {
    console.log('Retry button clicked');
  }
  apiUrl = environment.apiUrl;
}
