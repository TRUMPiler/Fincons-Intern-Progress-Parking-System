/**
 * Vehicle Exit Component
 * Handles the vehicle exit process and generates parking bills
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../auth.service';
import { DialogModule } from 'primeng/dialog';
import { ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-vehicle-exit',
  imports: [FormsModule, CommonModule, DialogModule],
  standalone: true,
  templateUrl: './vehicle-exit.html',
  styleUrl: './vehicle-exit.css',
})
export class VehicleExit implements OnInit {
  exitVehicleNumber: string = '';
  exitBillData: any[] = [];
  exitShowBillPopup = false;

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {}

  onExitSubmit(): void {
    this.exitVehicleNumber = (this.exitVehicleNumber || '')
      .trimStart()
      .toUpperCase()
      .trimEnd();

    if (!this.exitVehicleNumber) {
      alert('Please enter a vehicle number');
      return;
    }

    this.authService.vehicleExit(this.exitVehicleNumber).subscribe({
      next: (response) => {
        if (response.success) {
          this.exitBillData = [response.data];
          alert(response.message);
          this.exitShowBillPopup = true;
          this.cdr.markForCheck();
        } else {
          alert(response.message);
        }
      },
      error: (err: any) => {
        if (err.status == 0) {
          alert('Server is down. Please try again later.');
          return;
        }
        alert(err.error?.message || err.message || 'Error occurred');
      }
    });
  }

  confirmExit(): void {
    this.exitShowBillPopup = false;
    alert('Vehicle exited successfully');
    this.exitVehicleNumber = '';
  }

  closeExitPopup(): void {
    this.exitShowBillPopup = false;
  }

  onBack(): void {
    this.router.navigate(['/']);
  }
}
