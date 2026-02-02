/**
 * Homepage Component
 * This is the main landing page of the Parking System
 * Provides navigation links to all major features of the application
 */

import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-homepage',
  imports: [RouterModule],
  standalone: true,
  templateUrl: './homepage.html',
  styleUrl: './homepage.css',
})
export class Homepage {}
