import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { ChangeDetectorRef } from '@angular/core';
@Component({
  selector: 'app-vehicle-entry',
  imports: [FormsModule, CommonModule],
  standalone: true,
  templateUrl: './vehicle-entry.html',
  styleUrl: './vehicle-entry.css',
})
export class VehicleEntry implements OnInit, AfterViewInit {
  vehicle = {
    vehicleNumber: '',
    vehicleType: '',
    parkingLotId: ''
  }
  indianPlateRegex = /^[A-Z]{2}[ -]?[0-9]{2}[ -]?[A-Z]{1,2}[ -]?[0-9]{4}$/;
  validationErrors: { [key: string]: string } = {};
  constructor(private authService: AuthService, private cdr: ChangeDetectorRef) { }
  vehicleTypes = ['CAR', 'BIKE'];
  parkingLots: any[] = [];
  refreshParkingLots(): void {
    this.authService.getParkingLots().subscribe({
      next: (response) => {
        if (response.success) {
          this.parkingLots = response.data;
        } else {
          alert(response.message);
        }
      },
      error: (err) => {
        if(err.status==0)
          {
            alert("Server is down. Please try again later.");
            window.location.href="/";
            return;
          }
        console.error(err);
      }
    });
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.refreshParkingLots();
  }
  validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;

    // Validate vehicle number
    if (!this.vehicle.vehicleNumber.trim()) {
      this.validationErrors['vehicleNumber'] = 'Vehicle Number is required';
      isValid = false;
    } else if (!this.indianPlateRegex.test(this.vehicle.vehicleNumber.trim().toUpperCase())) {
      this.validationErrors['vehicleNumber'] = 'Invalid vehicle number format. Expected format: XX-12-AB-1234';
      isValid = false;
    }

    // Validate vehicle type
    if (!this.vehicle.vehicleType) {
      this.validationErrors['vehicleType'] = 'Vehicle Type is required';
      isValid = false;
    }

    // Validate parking lot
    if (!this.vehicle.parkingLotId) {
      this.validationErrors['parkingLotId'] = 'Parking Lot is required';
      isValid = false;
    }

    return isValid;
  }

  onSubmit() {
     this.vehicle.vehicleNumber = this.vehicle.vehicleNumber.trimStart().toUpperCase().trimEnd();
    if (!this.validateForm()) {
      alert('Please fix the validation errors');
      return;
    }

   
    
    this.authService.vehicleEntry(this.vehicle).subscribe(
      {
        next: (response) => {
          console.log(response);
          if (response.success) {
            alert(response.message);
            
            this.refreshParkingLots();
            this.cdr.detectChanges();
          }
          else {
            alert(response.message);
          }
        },
        error: (err) => {
          if(err.status==0)
          {
            alert("Server is down. Please try again later.");
            return;
          }
          alert(err.error.message);

          console.error('There was an error!', err);
        },
      }
    )
  }

  onBack() {
    window.location.href = "/";
    console.log("Back button clicked");
  }
}
