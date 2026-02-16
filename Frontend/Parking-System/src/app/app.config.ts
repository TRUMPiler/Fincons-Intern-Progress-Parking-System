/**
 * Application configuration file
 * Defines core providers and routing configuration for the Angular application
 */

import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import { routes } from './app.routes';
import Aura from '@primeuix/themes/aura'
// Application configuration object that registers core providers and application-wide services
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(), // Enable global error handling
    provideRouter(routes),// Configure routing with application routes
    providePrimeNG({
      ripple: true,
      theme:{
        preset: Aura,
        // options:
        // {
        //   darkModeSelector: 'none' 
        // }
      }
    }), 
  ]
};
