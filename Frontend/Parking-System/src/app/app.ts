/**
 * Root App Component
 * This is the main component of the Parking System application
 * It serves as the container for all other components and handles routing
 */

import { Component, signal } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  // Application title displayed in the UI
  protected readonly title = signal('Parking-System');
}
