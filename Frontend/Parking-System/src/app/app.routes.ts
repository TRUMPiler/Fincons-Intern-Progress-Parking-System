/**
 * Application routing configuration
 * Defines all routes for the Parking System application
 */

import { Routes } from '@angular/router';
import { ParkinglotRegisteration } from './parkinglot-registeration/parkinglot-registeration';
import { Homepage } from './homepage/homepage';
import { VehicleEntry } from './vehicle-entry/vehicle-entry';
import { VehicleExit } from './vehicle-exit/vehicle-exit';
import { ActiveSession } from './active-session/active-session';
import { ShowInfo } from './show-info/show-info';
import { Reservation } from './reservation/reservation';

// Array of route definitions for the application
export const routes: Routes = [
    // Route for parking lot registration page
    {
        path: 'register',
        component: ParkinglotRegisteration,
        title: 'Parking-System'
    },
    // Default home page route
    {
        path: '',
        component: Homepage,
        title: 'Home'
    },
    // Route for vehicle entry page
    {
        path: 'vehicle-entry',
        component: VehicleEntry,
        title: 'Vehicle Entry'
    },
    // Route for vehicle exit page
    {
        path: 'vehicle-exit',
        component: VehicleExit,
        title: 'Vehicle Exit'
    },
    // Route for active parking sessions
    {
        path: 'active-session',
        component: ActiveSession,
        title: 'Active Session'
    },
    // Route for parking lot information and management
    {
        path: 'show-info',
        component: ShowInfo,
        title: 'Parking Lot Info'
    },
    // Route for parking reservations
    {
        path: 'reservation',
        component: Reservation,
        title: 'Parking Reservation'
    },
    // Wildcard route: redirects any unmatched paths to home
    {
        path: '**',
        redirectTo: '',
    }
];