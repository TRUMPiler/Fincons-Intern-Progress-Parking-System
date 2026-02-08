import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import { MessageService } from 'primeng/api';
@Component({
  selector: 'app-reservation',
  imports: [FormsModule, CommonModule, TableModule, ToastModule, RippleModule],
  standalone: true,
  providers: [MessageService],
  templateUrl: './reservation.html',
  styleUrl: './reservation.css',
})
export class Reservation {    
  vehicle = {
    vehicleNumber: '',
    vehicleType: '',
    parkingLotId: ''
  }

  indianPlateRegex = /^[A-Z]{2}[ -]?[0-9]{2}[ -]?[A-Z]{1,2}[ -]?[0-9]{4}$/;
  validationErrors: { [key: string]: string } = {};
  constructor(private authService: AuthService, private cdr: ChangeDetectorRef, private messageService: MessageService) {
    this.messageService=inject(MessageService);
   }
  vehicleTypes = ['CAR', 'BIKE'];
  parkingLots: any[] = [];
  reservation: any[] = [];
  displayedReservations: any[] = [];
  // pagination state (client-side paging over the loaded reservations)
  pageNumber = 0;
  pageSize = 10;
  totalReservationRecords = 0;
  refreshReservation(): void {
    this.authService.getReservations().subscribe({
      next: (response) => {
        if (response.success) {
          // Ensure reservation is always an array
          if (Array.isArray(response.data)) {
            this.reservation = response.data;
          } else if (response.data && Array.isArray((response.data as any).content)) {
            this.reservation = (response.data as any).content;
          } else {
            this.reservation = [];
            console.warn('Unexpected reservation data format:', response.data);
          }
          this.totalReservationRecords = this.reservation.length;
          this.updateDisplayedReservations();
          console.log('Loaded reservations:', this.reservation.length);
          this.cdr.markForCheck();
        } else {
          alert(response.message);
        }
      },
      error: (err) => {
        if(err.status==0)
          {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.' });
          
            window.location.href="/";
            return;
          }
        console.error(err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message });
      }
    });
  }

  private updateDisplayedReservations() {
    const start = this.pageNumber * this.pageSize;
    this.displayedReservations = this.reservation.slice(start, start + this.pageSize);
    this.totalReservationRecords = this.reservation.length;
    
  }

  getPageCount(): number {
    return Math.max(1, Math.ceil(this.totalReservationRecords / this.pageSize));
  }

  goToNextPage() {
    if ((this.pageNumber + 1) < this.getPageCount()) {
      this.pageNumber++;
      this.updateDisplayedReservations();
      this.cdr.markForCheck();
    }
  }

  goToPreviousPage() {
    if (this.pageNumber > 0) {
      this.pageNumber--;
      this.updateDisplayedReservations();
      this.cdr.markForCheck();
    }
  }

  onPageSizeChange(newSize: number) {
    this.pageSize = Number(newSize) || 10;
    this.pageNumber = 0;
    this.updateDisplayedReservations();
    this.cdr.markForCheck();
  }
  refreshParkingLots(): void {
    this.authService.getALLParkingLots().subscribe({
      next: (response) => {
        if (response.success) {
          // Ensure parkingLots is always an array
          if (Array.isArray(response.data)) {
            this.parkingLots = response.data;
          } else if (response.data && Array.isArray((response.data as any).content)) {
            this.parkingLots = (response.data as any).content;
          } else {
            this.parkingLots = [];
            console.warn('Unexpected parking lots data format:', response.data);
            this.cdr.markForCheck();
          }
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: response.message });
        }
      },
      error: (err) => {
        if(err.status==0)
          {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.' });
            window.location.href="/";
            return;
          }
        console.error(err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message });
      }
    });
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.refreshParkingLots();
    this.refreshReservation();
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

    
     
    this.authService.ParkingLotReservation(this.vehicle).subscribe(
      {
        next: (response) => {
          console.log(response);
          if (response.success) {
            alert(response.message);
            
            this.refreshParkingLots();
            this.refreshReservation();
          }
          else {
            alert(response.message);
          }
          this.cdr.markForCheck();
        },
        error: (err) => {
          if(err.status==0)
          {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.' });
            return;
          }
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message });

          console.error('There was an error!', err);
        },
      }
    )
  }

  onBack() {
    window.location.href = "/";
    console.log("Back button clicked");
  }

  onCancleReservation(id:any){
    this.authService.CancelReservation(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: "Reservation Cancelled Successfully" });
          this.refreshReservation();
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: response.message });
        }
      },
      error: (err) => {
        if(err.status==0)
          {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.' });
            window.location.href="/";
            return;
          }
        console.error(err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message });
      }
    });
  }
  onArrival(id:any)
  {
    this.authService.ArrivalReservation(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: "Marked as Arrived Successfully" });
          this.refreshReservation();
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
        alert(err.error.message);
      }
    });
  }

  /**
   * Returns the CSS class for reservation status
   */
  getStatusClass(status: string): string {
    switch (status.toUpperCase()) {
      case 'ACTIVE':
        return 'status-active';
      case 'PENDING':
        return 'status-pending';
      case 'CANCELLED':
        return 'status-cancelled';
      case 'COMPLETED':
        return 'status-completed';
      default:
        return 'status-secondary';
    }
  }
}
