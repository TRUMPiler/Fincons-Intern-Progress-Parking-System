import { Component, OnInit, AfterViewInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-parkinglot-registeration',
  imports: [FormsModule, CommonModule],
  standalone:true,
  templateUrl: './parkinglot-registeration.html',
  styleUrl: './parkinglot-registeration.css',
})
export class ParkinglotRegisteration implements OnInit, AfterViewInit {
  public parkinglot={
    name:'',
    location:'',
    totalSlots:'',
    basePricePerHour:'',
    parkingSlots:[]
  }

  public parkingLots: any[] = [];
  public countlots:any[]=[];
  validationErrors: { [key: string]: string } = {};
  errorMessage='';
  constructor(public authService:AuthService)
  {
   
  }
validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;

    // Validate parking lot name
    if (!this.parkinglot.name || !this.parkinglot.name.trim()) {
      this.validationErrors['name'] = 'Parking Lot Name is required';
      isValid = false;
    } else if (this.parkinglot.name.trim().length < 3) {
      this.validationErrors['name'] = 'Parking Lot Name must be at least 3 characters';
      isValid = false;
    }
    else if (this.parkinglot.name.trim().length > 50) {
      this.validationErrors['name'] = 'Parking Lot Name must not exceed 50 characters';
      isValid = false;
    }
    else if (!/^[a-zA-Z0-9\s]+$/.test(this.parkinglot.name.trim())) {
      this.validationErrors['name'] = 'Parking Lot Name can only contain alphanumeric characters and spaces';
      isValid = false;
    }
    else if (/^\d+$/.test(this.parkinglot.name.trim())) {
      this.validationErrors['name'] = 'Parking Lot Name cannot be entirely numeric';
      isValid = false;
    }
    // Validate location
    if (!this.parkinglot.location || !this.parkinglot.location.trim()) {
      this.validationErrors['location'] = 'Parking Location is required';
      isValid = false;
    } else if (this.parkinglot.location.trim().length < 3) {
      this.validationErrors['location'] = 'Parking Location must be at least 3 characters';
      isValid = false;
    }
    else if (this.parkinglot.location.trim().length > 100) {
      this.validationErrors['location'] = 'Parking Location must not exceed 100 characters';
      isValid = false;
    }
    else if (!/^[a-zA-Z0-9\s,.-]+$/.test(this.parkinglot.location.trim())) {
      this.validationErrors['location'] = 'Parking Location contains invalid characters';
      isValid = false;
    }
    else if (/^\d+$/.test(this.parkinglot.location.trim())) {
      this.validationErrors['location'] = 'Parking Location cannot be entirely numeric';
      isValid = false;
    }
    

    // Validate total slots
    if (!this.parkinglot.totalSlots || this.parkinglot.totalSlots === '') {
      this.validationErrors['totalSlots'] = 'Total Slots is required';
      isValid = false;
    } else if (Number(this.parkinglot.totalSlots) <= 0) {
      this.validationErrors['totalSlots'] = 'Total Slots must be greater than 0';
      isValid = false;
    } else if (!Number.isInteger(Number(this.parkinglot.totalSlots))) {
      this.validationErrors['totalSlots'] = 'Total Slots must be a whole number';
      isValid = false;
    }

    // Validate base price per hour
    if (!this.parkinglot.basePricePerHour || this.parkinglot.basePricePerHour === '') {
      this.validationErrors['basePricePerHour'] = 'Base Price per Hour is required';
      isValid = false;
    } else if (Number(this.parkinglot.basePricePerHour) <= 0) {
      this.validationErrors['basePricePerHour'] = 'Base Price must be greater than 0';
      isValid = false;
    }

    return isValid;
  }

ngOnInit(): void {
}

  ngAfterViewInit(): void {
    console.log("invoked")
    setTimeout(() => {
      this.authService.getParkingSlots().subscribe({
        next: (response) => {
          if (response.success) 
          {

            this.parkingLots = response.data;
          } 
          else 
          {
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
    }, 500);
  }


  onSubmit()
  {
    if (!this.validateForm()) {
      alert('Please fix the validation errors');
      return;
    }
    
      this.authService.RegisterParkingLot(this.parkinglot).subscribe(
      {
        next: (response) => {
          console.log(response);
          if(response.success)
          {
            alert(response.message);
            this.parkinglot = {
              name: '',
              location: '',
              totalSlots: '',
              basePricePerHour: '',
              parkingSlots: []
            };
            this.refreshParkingLots();
          }
          else
          {
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

  refreshParkingLots(): void {
    this.authService.getParkingSlots().subscribe({
      next: (response) => {
        if (response.success) {
          this.parkingLots = response.data;
        } else {
          alert(response.message);
        }
      },
      error: (err) => {
        console.error(err);
      }
    });
  }
  onBack()
  {
    window.location.href="/";
    console.log("Back button clicked");
  }
  event(data:any)
  {
    sessionStorage.setItem("ID",data);
    window.location.href="/show-info";
  }
}
