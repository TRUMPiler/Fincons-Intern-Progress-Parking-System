/**
 * Application configuration file
 * Defines core providers and routing configuration for the Angular application
 */

import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';

// Application configuration object containing all necessary providers
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(), // Enable global error handling
    provideRouter(routes) // Configure routing with application routes
  ]
};
