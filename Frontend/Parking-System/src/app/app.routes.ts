/**
 * Application routing configuration
 * Defines all routes for the Parking System application
 */

import { Routes } from '@angular/router';
import { ParkinglotRegisteration } from './parkinglot-registeration/parkinglot-registeration';
import { Homepage } from './homepage/homepage';
import { VehicleEntry } from './vehicle-entry/vehicle-entry';
import { ActiveSessionsComponent } from './active-sessions/active-sessions';
import { SessionHistoryComponent } from './session-history/session-history';
import { ShowInfo } from './show-info/show-info';
import { Reservation } from './reservation/reservation';
import { Topicstest } from './topicstest/topicstest';
// Array of application route definitions
export const routes: Routes = [
    // Route: parking lot registration page
    {
        path: 'register',
        component: ParkinglotRegisteration,
        title: 'Parking-System'
    },
    // Route: default home page
    {
        path: '',
        component: Homepage,
        title: 'Home'
    },
    // Route: vehicle entry page
    {
        path: 'vehicle-entry',
        component: VehicleEntry,
        title: 'Vehicle Entry'
    },
    // Route: active parking sessions
    {
        path: 'active-sessions',
        component: ActiveSessionsComponent,
        title: 'Active Sessions'
    },
    // Route: session history
    {
        path: 'session-history',
        component: SessionHistoryComponent,
        title: 'Session History'
    },
    {
        path: 'websocket-test',
        component: Topicstest,
        title: 'WebSocket Test'
    },
    // Route: parking lot information and management
    {
        path: 'show-info',
        component: ShowInfo,
        title: 'Parking Lot Info'
    },
    // Route: parking reservations
    {
        path: 'reservation',
        component: Reservation,
        title: 'Parking Reservation'
    },
    // Wildcard route: redirect unmatched paths to the home page
    {
        path: '**',
        redirectTo: '',
    }
];