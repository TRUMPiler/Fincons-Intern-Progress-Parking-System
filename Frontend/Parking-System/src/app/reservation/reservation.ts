import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnDestroy,OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../auth.service';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { WebSocket1Service } from '../websocket1.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-reservation',
  imports: [FormsModule, CommonModule, TableModule, ToastModule, RippleModule, ButtonModule, DialogModule,SelectModule],
  standalone: true,
  providers: [MessageService],
  templateUrl: './reservation.html',
  styleUrl: './reservation.css',
})
export class Reservation implements OnInit, OnDestroy{
  Math=Math;
  vehicle = {
    vehicleNumber: '',
    vehicleType: '',
    parkingLotId: ''
  }
  private webSocket1Service: WebSocket1Service;
  private destroy$ = new Subject<void>();
  visible: boolean = false;
  indianPlateRegex = /^[A-Z]{2}[ -]?[0-9]{2}[ -]?[A-Z]{1,2}[ -]?[0-9]{4}$/;
  validationErrors: { [key: string]: string } = {};
  constructor(private authService: AuthService, private cdr: ChangeDetectorRef, private messageService: MessageService, ) {
    this.messageService = inject(MessageService);
    this.webSocket1Service = inject(WebSocket1Service);
  }
  pageNumber: number = 0;
  pageSize: number = 10;
  totalRecords: number = 0;
  isSorted: boolean = true;
  totalPages: number = 0;
  sortField: string = 'parkingSlotId';
  sortDirection: 'asc' | 'desc' = 'desc';
  vehicleTypes = ['CAR', 'BIKE'];
  parkingLots: any[] = [];
  reservation: any[] = [];
  displayedReservations: any[] = [];
  // No pagination: always show full reservation list
  refreshReservation(): void {
    this.authService.getReservationsPaginated(
      this.pageNumber,
      this.pageSize,
      this.sortField,
      this.sortDirection,
    ).subscribe({
      next: (response) => {
        if (response.success) {
          console.log(this.pageNumber, this.pageSize, this.sortField, this.sortDirection);
          console.log('Received reservation response:', response);
          // Ensure reservation is always an array
          if (Array.isArray(response.data)) {
           
          } else if (response.data && Array.isArray((response.data as any).content)) {
             console.log('Received reservations data:', response.data.content);
            this.reservation = response.data.content;
            console.log('Loaded reservations:', this.reservation.length);
            this.totalRecords = response.data.totalElements ?? 0;
            this.totalPages = this.Math.ceil(this.totalRecords / this.pageSize);
            console.log('Total records:', this.totalRecords, 'Total pages:', this.totalPages);
            this.cdr.markForCheck();
          } else {
            this.reservation = [];
            console.warn('Unexpected reservation data format:', response.data);
          }
          // show entire list
          this.displayedReservations = this.reservation;
          console.log('Loaded reservations:', this.reservation.length);
         
        } else {
          alert(response.message);
        }
        this.cdr.markForCheck();
      },
      
      error: (err) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 5000 });

          setTimeout(() => { window.location.href = '/'; }, 5000);
          return;
        }
        console.error(err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || err.message });
      }
    });
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  // no pagination helpers — entire list is shown in `displayedReservations`
  ChangingSorting(field: string) {
    if(this.isSorted==false||(this.isSorted==true&&this.sortField!=field)){
      this.sortField=field;
      this.sortDirection='asc';
      this.isSorted=true;
      this.refreshReservation();
    }
    else if(this.isSorted==true&&this.sortField==field&&this.sortDirection=='asc'){
      this.sortDirection='desc';
      this.refreshReservation();
    }
    else
    {
      this.isSorted=false;
      this.sortField='parkingSlotId';
      this.sortDirection='desc';
      this.refreshReservation();
    }
    
    this.cdr.markForCheck();
  }
  refreshParkingLots(): void {
    this.authService.getParkingLots().subscribe({
      next: (response) => {
        if (response.success) {
          if (Array.isArray(response.data)) {
            // console.log('Received parking lots data:', response.data);
            this.parkingLots = response.data;
            console.log('Received parking lots data:', response.data.content);
            
            this.parkingLots = response.data;
            
            this.parkingLots.forEach((lot: any) => {
              lot.totalSlots = lot.parkingSlots ? lot.parkingSlots.length : 0;
              lot.availableSlots = lot.parkingSlots ? lot.parkingSlots.filter((s: any) => s.status === 'AVAILABLE').length : 0;
              lot.occupiedSlots = lot.totalSlots - lot.availableSlots;
              lot.occupancyPercentage = lot.totalSlots > 0 ? Math.round((lot.occupiedSlots / lot.totalSlots) * 100) : 0;
            });
             this.parkingLots.forEach(lot => {
              this.loadSlotOfLots(lot.id);
            });
            console.log('Loaded parking lots:', this.parkingLots.length);
             this.cdr.markForCheck();
          } else if (response.data && Array.isArray((response.data as any).content)) {
            console.log('Received parking lots data:', response.data.content);
            
            this.parkingLots = response.data;
            
            this.parkingLots.forEach((lot: any) => {
              lot.totalSlots = lot.parkingSlots ? lot.parkingSlots.length : 0;
              lot.availableSlots = lot.parkingSlots ? lot.parkingSlots.filter((s: any) => s.status === 'AVAILABLE').length : 0;
              lot.occupiedSlots = lot.totalSlots - lot.availableSlots;
              lot.occupancyPercentage = lot.totalSlots > 0 ? Math.round((lot.occupiedSlots / lot.totalSlots) * 100) : 0;
            });
             this.parkingLots.forEach(lot => {
              this.loadSlotOfLots(lot.id);
            });
            console.log('Loaded parking lots:', this.parkingLots.length);
             this.cdr.markForCheck();
          } else {
            this.parkingLots = [];
            console.warn('Unexpected parking lots data format:', response.data);
          }
        } else {
          alert(response.message);
        }
       
      },
      error: (err) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life:5000 });
         setTimeout(() => {
          window.location.href = '/';
         }, 5000);
          
          return;
        }
        console.error(err);
      }
    });
  }
  ngOnInit(): void {
    this.refreshParkingLots();
    this.refreshReservation();
  }
  showDialog() {
    this.visible = true;
  }   
   loadSlotOfLots(parkingLotId: string | number): void
    {
      console.log('Subscribing to slot updates for parking lot ID:', parkingLotId);
      this.webSocket1Service.DashBoardUpdates(parkingLotId.toString())
        .pipe(takeUntil(this.destroy$))
        .subscribe((message) => {
          try {
            console.log('Received slot update message for parking lot ID:', parkingLotId, 'Message:', message);
            const payload = message.payload || {};
            const updatedSlots = Array.isArray(payload.slots) ? payload.slots : [];
            const lotId = payload.parkingLotId ?? payload.parkingLot?.id ?? parkingLotId;
  
            // Update the parkingLots array immutably so Angular change detection picks it up
            this.parkingLots = this.parkingLots.map(lot => {
              if (lot.id === lotId) {
                const totalSlots = updatedSlots.length || (lot.totalSlots ?? lot.parkingSlots?.length ?? 0);
                const availableSlots = updatedSlots.filter((s: any) => s.status === 'AVAILABLE').length;
                const occupiedSlots = totalSlots - availableSlots;
                const occupancyPercentage = totalSlots === 0 ? 0 : Math.round((occupiedSlots / totalSlots) * 100);
  
                return {
                  ...lot,
                  parkingSlots: updatedSlots.length ? updatedSlots : lot.parkingSlots,
                  totalSlots,
                  availableSlots,
                  occupiedSlots,
                  occupancyPercentage
                };
              }
              return lot;
            });
  
            // If payload contains aggregate fields, sync them too
            if (payload.availableSlots !== undefined || payload.occupiedSlots !== undefined || payload.occupancyPercentage !== undefined) {
              this.parkingLots = this.parkingLots.map(lot => {
                if (lot.id === payload.parkingLotId) {
                  return {
                    ...lot,
                    availableSlots: payload.availableSlots ?? lot.availableSlots,
                    occupiedSlots: payload.occupiedSlots ?? lot.occupiedSlots,
                    occupancyPercentage: payload.occupancyPercentage ?? lot.occupancyPercentage
                  };
                }
                return lot;
              });
            }
  
            this.cdr.markForCheck();
          } catch (err) {
            console.error('Error processing dashboard update message:', err);
          }
        });
    }
  validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;

    // Validate vehicle number
    if (!this.vehicle.vehicleNumber.trim()||this.vehicle.vehicleNumber.trim() === ''||this.vehicle.vehicleNumber.length === 0) {
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

  onPageSizeChange(newSize: number) {
    this.pageSize=newSize;

    console.log('Page size changed to:', this.pageSize);
    this.pageNumber=0;
    this.refreshReservation();
  }
  getNextPage() {
    console.log('Next page requested. Current page:', this.pageNumber, 'Total pages:', this.totalPages);
    if(this.pageNumber<this.totalPages-1){
      this.pageNumber++;
      console.log('Navigated to page:', this.pageNumber);
      this.refreshReservation();
    }
  }
  StartPage():number{
    if(this.totalRecords === 0) {
      return 0;
    }
    return this.pageNumber*this.pageSize+1;
  }
  EndPage():number{
    if(this.totalRecords === 0) { 
      return 0;
    }
    return (this.pageNumber+1)*this.pageSize;
  }
  

  getPreviousPage() {
      if(this.pageNumber>0){
        this.pageNumber--;
        this.refreshReservation();
      }
  }

  onSubmit(form: NgForm) {
  
    if (!this.validateForm()) {

      this.messageService.add({ severity: 'error', summary: 'Validation Error', detail: 'Please fix the validation errors and try again.', key: 'error', life:5000 });
      return;

    }
    if(this.parkingLots.at(this.parkingLots.findIndex(lot => lot.id === this.vehicle.parkingLotId))?.availableSlots === 0){
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Selected parking lot is full. Please select another lot.', key: 'error', life:5000 });
      return;
    }
    this.authService.ParkingLotReservation(this.vehicle).subscribe(
      {
        next: (response) => {
          console.log(response);
          if (response.success) {
            this.messageService.add({ severity: 'success', summary: 'Success', detail: "Parking Slot Reserved Successfully", key: 'tl', life:5000 });
            this.visible=false;
            form.resetForm();
            this.refreshReservation();
          }
          else {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: response.message, key: 'error', life:5000 });
          }
          this.cdr.markForCheck();
        },
        error: (err) => {
          if (err.status == 0) {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life:5000 });
            setTimeout(() => {
              window.location.href = "/";
            }, 5000);
            return;
          }
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message, key: 'error', life:5000 });

          console.error('There was an error!', err);
        },
      }
    )
  }

  onBack() {
    window.location.href = "/";
    console.log("Back button clicked");
  }
  Clear(form: NgForm) {
    form.resetForm();
  }
  onCancleReservation(id: any) {
    this.authService.CancelReservation(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: "Reservation Cancelled Successfully", key: 'tl', life:5000 });
          this.refreshReservation();
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: response.message, key: 'error', life:5000 });
        }
      },
      error: (err) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life:5000 });
          setTimeout(() => {
            window.location.href = "/";
          }, 5000);
          return;
        }
        console.error(err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message, key: 'error', life:5000 });
      }
    });
  }
  onArrival(id: any) {
    this.authService.ArrivalReservation(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: "Marked as Arrived Successfully", key: 'tl', life:5000 });
          this.refreshReservation();
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: response.message, key: 'error', life:5000 });
        }
      },
      error: (err) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life:5000 });
          window.location.href = "/";
          return;
        }
        console.error(err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message, key: 'error', life:5000 });
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
