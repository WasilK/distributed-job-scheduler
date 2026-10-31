import { Routes } from '@angular/router';
import { Dashboard } from './features/dashboard/dashboard';
import { Jobs } from './features/jobs/jobs';

export const routes: Routes = [
  {
    path: '',
    title: 'Job Schedluer Dashboard',
    component: Dashboard,
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      {
        path: 'dashboard',
        component: Dashboard,
      }
    ],
  },
];
