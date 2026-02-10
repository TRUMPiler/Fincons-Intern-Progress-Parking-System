import { CommonModule } from '@angular/common';
import { AfterViewInit,inject, Component, OnInit,OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../auth.service';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import { MessageService } from 'primeng/api';
import { WebSocket1Service } from '../websocket1.service';
import { Subject, takeUntil } from 'rxjs';
import { Select } from 'primeng/select';

@Component({
  selector: 'app-vehicle-entry',
  imports: [FormsModule, CommonModule, DialogModule, ToastModule, RippleModule, Select],
  standalone: true,
  providers: [MessageService],
  templateUrl: './vehicle-entry.html',
  styleUrl: './vehicle-entry.css',
})
export class VehicleEntry implements OnInit, OnDestroy {
  // Vehicle model uses camelCase keys expected by the backend
  vehicle: { vehicleNumber: string; vehicleType: string; parkingLotId: string | number } = {
    vehicleNumber: '',
    vehicleType: '',
    parkingLotId: ''
  };

  indianPlateRegex = /^[A-Z]{2}[ -]?[0-9]{2}[ -]?[A-Z]{1,2}[ -]?[0-9]{4}$/;
  validationErrors: { [key: string]: string } = {};

  vehicleTypes = ['CAR', 'BIKE'];
  parkingLots: any[] = [];
  dialogVisible = false;
  destroy$ = new Subject<void>(); 
  // Exit dialog state
  showExitDialog = false;
  exitVehicleNumber: string = '';
  exitBillData: any[] = [];
  exitShowBillPopup = false;
  private messageService = inject(MessageService);
  constructor(private authService: AuthService, private cdr: ChangeDetectorRef,private webSocket1Service: WebSocket1Service) {
    
  }
  Math=Math;
  refreshParkingLots(): void {
    this.authService.getALLParkingLots().subscribe({
      next: (response) => {
        if (response.success) {
          if (Array.isArray(response.data)) {
            console.log('Received parking lots data:', response.data);
            this.parkingLots = response.data;
            
          } else if (response.data && Array.isArray((response.data as any).content)) {
            console.log('Received parking lots data:', response.data.content);
            this.parkingLots = response.data.content;
            this.parkingLots.forEach((lot: any) => {
              lot.totalSlots = lot.parkingSlots ? lot.parkingSlots.length : 0;
              lot.availableSlots = lot.parkingSlots ? lot.parkingSlots.filter((s: any) => s.status === 'AVAILABLE').length : 0;
              lot.occupiedSlots = lot.totalSlots - lot.availableSlots;
              lot.occupancyPercentage = lot.totalSlots > 0 ? Math.round((lot.occupiedSlots / lot.totalSlots) * 100) : 0;
            });
             this.parkingLots.forEach(lot => {
              this.loadSlotOfLots(lot.id);
            });
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
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
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
    
   }
  showDialog(): void {
    // this.refreshParkingLots();
    this.dialogVisible = true;
  }
  
  openExitDialog(): void {
    this.showExitDialog = true;
  }

  closeExitDialog(): void {
    this.showExitDialog = false;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onExitSubmit(): void {
    this.exitVehicleNumber = (this.exitVehicleNumber || '').trimStart().toUpperCase().trimEnd();
    this.authService.vehicleExit(this.exitVehicleNumber).subscribe({
      next: (response) => {
        if (response.success) {
          this.exitBillData = [response.data];
          this.messageService.add({ severity: 'success', summary: 'Success', detail: response.message, key: 'tl' ,life: 3000});
          this.showExitDialog = false;
          this.exitShowBillPopup = true;
          console.log('Exit Bill Data:', this.exitBillData);
          this.cdr.markForCheck();
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: response.message, key: 'error', life: 3000 });
        }
      },
      error: (err: any) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
          return;
        }
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || err.message || 'Error', key: 'error', life: 3000 });
      }
    });
  }

  confirmExit(): void {
    this.exitShowBillPopup = false;
    this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Vehicle exited successfully', key: 'tl', life: 3000 });
    this.exitVehicleNumber = '';
    this.closeExitDialog();
  }

  closeExitPopup(): void {
    this.exitShowBillPopup = false;
  }

  validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;

    if (!this.vehicle.vehicleNumber.trim()) {
      this.validationErrors['vehicleNumber'] = 'Vehicle Number is required';
      isValid = false;
    } else if (!this.indianPlateRegex.test(this.vehicle.vehicleNumber.trim().toUpperCase())) {
      this.validationErrors['vehicleNumber'] = 'Invalid vehicle number format. Expected format: XX-12-AB-1234';
      isValid = false;
    }

    if (!this.vehicle.vehicleType) {
      this.validationErrors['vehicleType'] = 'Vehicle Type is required';
      isValid = false;
    }

    if (!this.vehicle.parkingLotId) {
      this.validationErrors['parkingLotId'] = 'Parking Lot is required';
      isValid = false;
    }

    return isValid;
  }

  onSubmit(form:NgForm): void {

    if (!this.validateForm()) {
      this.messageService.add({ severity: 'error', summary: 'Validation Error', detail: 'Please fix the validation errors', key: 'error', life: 3000 });
      
      return;
    }

    if(this.parkingLots.length === 0) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No parking lots available. Please try again later.', key: 'error', life: 3000 });
      return;
    }
    if(this.parkingLots.find(lot => lot.id == this.vehicle.parkingLotId)?.availableSlots === 0) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Selected parking lot is full. Please select another lot.', key: 'error', life: 3000 });
      return;
    }
    // Prepare payload: ensure parkingLotId is numeric if provided
    const payload = {
      vehicleNumber: this.vehicle.vehicleNumber,
      vehicleType: this.vehicle.vehicleType,
      parkingLotId: this.vehicle.parkingLotId === '' ? null : Number(this.vehicle.parkingLotId)
    };

    console.log('Submitting vehicle entry:', payload);

    this.authService.vehicleEntry(payload).subscribe({
      next: (response) => {
        console.log(response);
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: response.message, key: 'tl', life: 3000 });
        
          this.dialogVisible = false;
          this.clearForm(form);
          this.cdr.detectChanges();
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: response.message, key: 'error', life: 3000 });
        }
      },
      error: (err) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
          window.location.href = '/';
          return;
        }
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || err.message || 'Error', key: 'error', life: 3000 });
        console.error('There was an error!', err);
      }
    });
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
  getAvailableSlots(lot: any): number {
    if (!lot.parkingSlots || !Array.isArray(lot.parkingSlots)) return 0;
    return lot.parkingSlots.filter((s: any) => s.status === 'AVAILABLE').length;
  }
  getPercentageFilled(lot: any): number {
    if (!lot.parkingSlots || !Array.isArray(lot.parkingSlots) || lot.parkingSlots.length === 0) return 0;
    const available = this.getAvailableSlots(lot);
    return ((lot.parkingSlots.length - available) / lot.parkingSlots.length) * 100;
  }
  clearForm(form: NgForm) {
    form.resetForm();
  }

  onBack() {
    window.location.href = "/";
    console.log("Back button clicked");
  }
}
