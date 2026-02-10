import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Form, FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ChangeDetectorRef } from '@angular/core';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import { MessageService } from 'primeng/api';
import { WebSocket1Service } from '../websocket1.service';
import { Subject,takeUntil } from 'rxjs';
@Component({
  selector: 'app-parkinglot-registeration',
  imports: [FormsModule, CommonModule, TableModule, ButtonModule,DialogModule, ToastModule, RippleModule],
  standalone: true,
  providers: [MessageService],
  templateUrl: 'parkinglot-registeration.html',
  styleUrl: './parkinglot-registeration.css',
})
export class ParkinglotRegisteration implements OnInit,OnDestroy {
  public parkinglot = {
    name: '',
    location: '',
    totalSlots: '',
    basePricePerHour: '',
    parkingSlots: []
  }
  destroy$ = new Subject<void>();
  page: number = 0;
  first: number = 0;
  rows: number = 10;
  totalRecords: number = 0;
  sortField: string = 'id';
  sortDirection: 'asc' | 'desc' = 'asc';
  public parkingLots: any[] = [];
  validationErrors: { [key: string]: string } = {};
  errorMessage = '';
  DialogVisible = false;
  // Expose Math utilities to the template for any numeric computations
  Math = Math;
 private messageService = inject(MessageService);
 private webSocketService = inject(WebSocket1Service);
  constructor(public authService: AuthService, private cdr: ChangeDetectorRef) {}
  validateForm(): boolean {
    this.validationErrors = {};
    let isValid = true;
    // Helper for string validations
    const checkString = (key: string, value: any, min = 3, max = 100, pattern?: RegExp) => {
      const v = String(value ?? '').trim();
      if (!v) { this.validationErrors[key] = `${key} is required`; return false; }
      if (v.length < min) { this.validationErrors[key] = `${key} must be at least ${min} characters`; return false; }
      if (v.length > max) { this.validationErrors[key] = `${key} must not exceed ${max} characters`; return false; }
      if (pattern && !pattern.test(v)) { this.validationErrors[key] = `${key} contains invalid characters`; return false; }
      if (/^\d+$/.test(v)) { this.validationErrors[key] = `${key} cannot be entirely numeric`; return false; }
      return true;
    };
  
    if (!checkString('name', this.parkinglot.name, 3, 50, /^[a-zA-Z0-9\s]+$/)) isValid = false;
    if (!checkString('location', this.parkinglot.location, 3, 100, /^[a-zA-Z0-9\s,.-]+$/)) isValid = false;
   
    // Numeric validations
    const slots = Number(this.parkinglot.totalSlots);
    if (!this.parkinglot.totalSlots || this.parkinglot.totalSlots === '') { this.validationErrors['totalSlots'] = 'Total Slots is required'; isValid = false; }
    else if (!Number.isInteger(slots) || slots <= 0) { this.validationErrors['totalSlots'] = 'Total Slots must be a positive whole number'; isValid = false; }

    const price = Number(this.parkinglot.basePricePerHour);
    if (!this.parkinglot.basePricePerHour || this.parkinglot.basePricePerHour === '') { this.validationErrors['basePricePerHour'] = 'Base Price per Hour is required'; isValid = false; }
    else if (isNaN(price) || price <= 0) { this.validationErrors['basePricePerHour'] = 'Base Price must be greater than 0'; isValid = false; }

    return isValid;
  }

  ngOnInit(): void {
    this.loadParkingLots({first: 0, rows: this.rows});
  }
  onBack(): void {
    window.location.href = '/';
  }
  showDialog():void
    {
      this.DialogVisible=true;
    }
  onSubmit(form: NgForm) {
    if (!this.validateForm()) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Please fix the validation errors', key: 'error' ,life: 3000});
      return;
    }
    this.DialogVisible=false;

    this.authService.RegisterParkingLot(this.parkinglot).subscribe(
      {
        next: (response) => {
          console.log(response);
          if (response.success) {
            this.messageService.add({ severity: 'success', summary: 'Success', detail: response.message, key: 'tl', life: 3000 });
            this.parkinglot = {
              name: '',
              location: '',
              totalSlots: '',
              basePricePerHour: '',
              parkingSlots: []
            };
            this.clearForm(form);
            this.loadParkingLots({first: this.first, rows: this.rows});
            this.cdr.markForCheck();
          }
          else {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: response.message, key: 'error', life: 3000 });
          }
        },
        error: (err) => {
          if (err.status == 0) {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
            return;
          }
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message, key: 'error', life: 3000 });

          if (err.error.message === 'Validation Failed') {
            let errors: { [key: string]: string } = err.error.data;
            let message = "";

            Object.values(errors).forEach(errorMsg => {
              message += errorMsg + "\n";
            });

            console.log(err);
            this.messageService.add({ severity: 'error', summary: 'Error', detail: message, key: 'error', life: 3000 });
          }
          console.error('There was an error!', err);
        },
      }
    );
  }
  // Handler for server-side pagination requests and responses
  loadParkingLots(event: {first: number, rows: number}) {
    this.first = event.first;
    this.rows = event.rows;
    this.page = Math.floor(this.first / this.rows);
    this.authService.getALLParkingLots()
      .subscribe({
        next: (response: any) => {
          const data = response?.data ?? response ?? {};
          const list = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : []);
          for (let i = 0; i < list.length; i++) {
              this.webSocketService.SubscribeToHighAlertUpdates(list[i].id)
              .pipe(takeUntil(this.destroy$))
              .subscribe((message)=> {
                console.log('Received high alert update for parking lot:', list[i].id, message.payload.message);
                this.messageService.add({ severity: 'warn', summary: 'High Alert', detail: `High alert for parking lot ${message.payload.parkingLotId}: ${message.payload.message}`, key: 'tl', life: 5000 });
                this.cdr.markForCheck();
              });
          }
           
          this.parkingLots = list;
          this.totalRecords = data.totalElements ?? data.totalRecords ?? list.length ?? 0;
          this.cdr.markForCheck();
        },
        error: (err) => {
          if(err.status == 0) {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
            setTimeout(() => { window.location.href = "/"; }, 3000);
            return;
          }
          
          console.error(err)}

      });
  }

  getPageCount(): number {
    return Math.ceil(this.totalRecords / this.rows) || 1;
  }

  goToNextPage() {
    if ((this.first + this.rows) < this.totalRecords) {
      this.first += this.rows;
      this.page = Math.floor(this.first / this.rows);
      this.loadParkingLots({first: this.first, rows: this.rows});
    }
  }

  goToPreviousPage() {
    if (this.first > 0) {
      this.first -= this.rows;
      this.page = Math.floor(this.first / this.rows);
      this.loadParkingLots({first: this.first, rows: this.rows});
    }
  }

  onPageSizeChange(newSize: any) {
    const parsed = Number(newSize) || 10;
    this.rows = parsed;
    this.first = 0;
    this.page = 0;
    this.loadParkingLots({first: this.first, rows: this.rows});
  }
  onHighAlert(parkingLotId: string) {
    this.webSocketService.SubscribeToHighAlertUpdates(parkingLotId).subscribe({
      next: (data) => {
        console.log('Received high alert update:', data);
        this.messageService.add({ severity: 'warn', summary: 'High Alert', detail: `High alert for parking lot ${data.parkingLotId}: ${data.message}`, key: 'tl', life: 3000 });
      },
      error: (err) => {
        console.error('Error subscribing to high alert updates:', err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to subscribe to high alert updates. Please try again later.', key: 'error', life: 3000 });
      }
    });
  }
  clearForm(form:NgForm) {
    form.resetForm();
  }
  ngOnDestroy(): void {
   this.destroy$.next();
    this.destroy$.complete();
  }
  event(data: any) {
    sessionStorage.setItem("ID", data);
    window.location.href = "/show-info";
  }

}
