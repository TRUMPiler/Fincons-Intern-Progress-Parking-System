import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../auth.service';
import { Subject } from 'rxjs';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-active-session',
  imports: [CommonModule, ToastModule, RippleModule],
  standalone: true,
  templateUrl: './active-session.html',
  styleUrls: ['./active-session.css'],
})
export class ActiveSession implements OnInit, OnDestroy {
  sessionData: any[] = [];
  sessionHistory: any[] = [];
  
  private destroy$ = new Subject<void>();

  constructor(private authService: AuthService, private messageService: MessageService) {}

  ngOnInit(): void {
    // Initial load of active sessions via HTTP
    this.loadActiveSessions();
    // Initial load of session history via HTTP
   
  }

  /**
   * Fetches the list of active sessions from the backend via HTTP.
   */
  loadActiveSessions(): void {
    this.authService.getActiveSessions().subscribe({
      next: (response) => {
        console.log('Active Session Component - Received response for active sessions:', response);
        if (response.success) {
          this.sessionData = [response.data];
          console.log('Loaded', this.sessionData.length, 'active sessions');
        } else {
          console.error('Failed to load active sessions:', response.message);
        }
      },
      error: (err) => {
        if (err.status === 0) {
          console.error('Server is down. Please try again later.');
        } else {
          console.error('Error loading active sessions:', err.error.message);
        }
      },
    });
  }

  /**
   * Fetches the list of historical sessions from the backend via HTTP.
   */
  loadSessionHistory(): void {
    this.authService.getHistorySessions().subscribe({
      next: (response) => {
        if (response.success) {
          this.sessionHistory = [response.data];
          console.log('Loaded', this.sessionHistory.length, 'historical sessions');
        } else {
          console.error('Failed to load session history:', response.message);
        }
      },
      error: (err) => {
        if (err.status === 0) {
          console.error('Server is down. Please try again later.');
        } else {
          console.error('Error loading session history:', err.error.message);
        }
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  }