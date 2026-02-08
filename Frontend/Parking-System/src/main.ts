/**
 * Main entry point for the Angular application
 * This file bootstraps the root App component with the application configuration
 */

// Import polyfills FIRST before any other imports
import './polyfills';

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
(window as any).global = window;
// Bootstrap the root App component with application configuration
bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
