import { Routes } from '@angular/router';
import { ParkinglotRegisteration } from './parkinglot-registeration/parkinglot-registeration';
import { Homepage } from './homepage/homepage';
import { VehicleEntry } from './vehicle-entry/vehicle-entry';
import { VehicleExit } from './vehicle-exit/vehicle-exit';
import { ActiveSession } from './active-session/active-session';

import { ShowInfo } from './show-info/show-info';
import { Reservation } from './reservation/reservation';

export const routes: Routes = [
    {
        path: 'register',
        component: ParkinglotRegisteration,
        title: 'Parking-System'
    },
    {
        path: '',
        component:Homepage,
        title: 'Home'
    },
    {
        path:'vehicle-entry',
        component:VehicleEntry,
        title:'Vehicle Entry'
    },
    {
        path: 'vehicle-exit',
        component:VehicleExit,
        title:'Vehicle Exit'
    },
    {
        path:'active-session',
        component:ActiveSession,
        title:'Active Session'
    },
     {
        path:'show-info',
        component:ShowInfo,
        title:'Parking Lot Info'
    
    },
    {
        path:'reservation',
        component:Reservation,
        title:'Parking Reservation'
    },
    {

        path: '**',
        redirectTo: '',
        
    }
   

];