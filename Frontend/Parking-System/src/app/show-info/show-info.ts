import { Component, OnInit, OnDestroy,inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { Subject, Subscription, takeUntil } from 'rxjs';
import { WebSocketService } from '../websocket.service';
import { WebSocket1Service } from '../websocket1.service';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { FormsModule } from '@angular/forms';
import { ChangeDetectorRef } from '@angular/core';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import { MessageService } from 'primeng/api';
@Component({
  selector: 'app-show-info',
  imports: [CommonModule, TableModule, InputTextModule, ButtonModule, FormsModule, ToastModule, RippleModule],
  standalone: true,
  providers: [MessageService],
  templateUrl: './show-info.html',
  styleUrl: './show-info.css',
})
export class ShowInfo implements OnInit, OnDestroy {
  public parkingLotInfo: any[] = [];
  public parkingSlots: any[] = [];
  public loading = false;
  
  // Pagination and sorting state for the parking slots table
  pageNumber = 0;
  pageSize = 10;
  totalSlotRecords = 0;
  sortField = 'id';
  sortDirection: 'asc' | 'desc' = 'asc';
  currentLotId: any = null;
  destroy$=new Subject<void>();
  private slotUpdateSub?: Subscription;
  
  constructor(private auth: AuthService, private websocket: WebSocketService, private cdr: ChangeDetectorRef, private messageService: MessageService) { 
    this.messageService = inject(MessageService);
  }

  private scheduleUpdate(fn: () => void) {
    Promise.resolve().then(() => {
      try { fn(); } catch (e) { console.error(e); }
      try { this.cdr.markForCheck(); } catch {}
    });
  }
  private WebSocket1Service: WebSocket1Service = inject(WebSocket1Service);
  ngOnInit(): void {
    let id = sessionStorage.getItem("ID");
    this.currentLotId = id;
    this.subscribeForHighAlert(this.currentLotId);
    console.debug('ShowInfo initialized for parking lot id:', this.currentLotId);
    this.loadParkingLotInfo(id);
    this.loadParkingSlots(id);
    this.getLiveOccupancy();

    if (this.currentLotId) {

      this.websocket.subscribeToSlotTopic(this.currentLotId);

      this.slotUpdateSub = this.websocket.slotUpdatesFor({ lotId: this.currentLotId }).subscribe((payload: any) => {
      
        const incomingId = payload?.slotId ?? payload?.id ?? payload?.parkingSlotId ?? payload?.slotNumber;
        if (!incomingId) return;
        const idx = this.parkingSlots.findIndex(s => String(s.id ?? s.slotId ?? s.slotNumber) === String(incomingId));
        if (idx === -1) return;

        const source = payload?.payload ?? payload?.data ?? payload;
        const updated = { ...this.parkingSlots[idx], ...source };
        updated.status = source?.status ?? source?.newStatus ?? updated.status;
        console.log('ShowInfo - Received slot update for slot id', incomingId, 'with payload:', payload, 'Updating slot at index', idx, 'to:', updated);
        this.scheduleUpdate(() => {
          this.parkingSlots[idx] = updated;
          this.parkingSlots = [...this.parkingSlots];
        });
      });
    }
    
  }
  subscribeForHighAlert(lotId: any)
  {
    this.WebSocket1Service.SubscribeToHighAlertUpdates(lotId)
    .pipe(takeUntil(this.destroy$))
    .subscribe((message) => {
      console.log('Received high alert message:', message);
      this.messageService.add({ severity: 'warn', summary: 'High Alert', detail: `High alert for parking lot ${message.payload.parkingLotId}: ${message.payload.message}`, key: 'tl', life: 5000 });
      
    });
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.slotUpdateSub) {
      this.slotUpdateSub.unsubscribe();
    }
    if (this.currentLotId) {
      this.websocket.unsubscribeSlotTopic(this.currentLotId);
    }
  }

  getLiveOccupancy()
  {
    
    this.WebSocket1Service.DashBoardUpdates(this.currentLotId)
    .pipe(takeUntil(this.destroy$))
    .subscribe((message) => {
      console.log('Received dashboard update message:', message);
      if(message.payload.parkingLotId == this.currentLotId) {
          this.parkingLotInfo[0].availableSlots = message.payload.availableSlots;
          this.parkingLotInfo[0].occupiedSlots = message.payload.occupiedSlots;
          this.parkingLotInfo[0].occupancyPercentage=message.payload.occupancyPercentage;
          console.log('Updated occupancy for parking lot', this.currentLotId, 'to:', this.parkingLotInfo[0].availableSlots);
          
      } 
      // this.messageService.add({ severity: 'info', summary: 'Dashboard Update', detail: `Current occupancy for parking lot ${message.parkingLotId}: ${message.occupancy}/${message.capacity}`, key: 'tl', life: 5000 });
    });
  }


  /**
   * Loads parking lot information
   */
  loadParkingLotInfo(id: any): void {
    this.auth.getParkingLotInfo(id).subscribe({
      next: (response) => {
        this.parkingLotInfo = [response.data];
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.',key: 'error', life: 3000 });
          window.location.href = '/';
          return;
        }
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message ?? 'Error loading parking lot info', key: 'error', life: 3000 });
        
        console.log(err);
      },
    });
  }

  /**
   * Loads parking slots with pagination
   */
  loadParkingSlots(id: any): void {
    this.loading = true;
    this.auth.getParkingSlotsPaginated(id, this.pageNumber, this.pageSize, this.sortField, this.sortDirection)
      .subscribe({
        next: (response: any) => {
          const data = response?.data ?? response ?? {};
          const content = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : []);
          const rows = content;
          const total = (data.totalElements ?? data.totalRecords) ?? rows.length ?? 0;
          console.log('ShowInfo - Loaded parking slots for lot id', id, 'with pagination:', {
            page: this.pageNumber,
            size: this.pageSize,            sortField: this.sortField,
            sortDirection: this.sortDirection,
          }, 'Received response:', response);
          this.scheduleUpdate(() => {
            this.parkingSlots = rows;
            this.totalSlotRecords = total;
            this.loading = false;
            this.cdr.markForCheck();
          });
        },
        error: (err: any) => {
          this.loading = false;
          if (err?.status === 0) {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
            setTimeout(() => { window.location.href = "/"; }, 3000);
          
            return;
          }
          // this.messageService.add({ severity: 'error', summary: 'Error', detail: err?.error?.message ?? 'Error loading parking slots', key: 'error', life: 3000 });
          this.parkingSlots = [];
          this.totalSlotRecords = 0;
        }
      });
  }

  // PrimeNG table events
  onPage(event: any) {
    // event: {first, rows, page}
    this.pageNumber = event.page || Math.floor((event.first || 0) / (event.rows || this.pageSize));
    this.pageSize = event.rows || this.pageSize;
    this.loadParkingSlots(this.currentLotId);
  }

  getPageCount(): number {
    return Math.max(1, Math.ceil(this.totalSlotRecords / this.pageSize));
  }

  goToNextPage(): void {
    if ((this.pageNumber + 1) * this.pageSize < this.totalSlotRecords) {
      this.pageNumber++;
      this.loadParkingSlots(this.currentLotId);
    }
  }

  goToPreviousPage(): void {
    if (this.pageNumber > 0) {
      this.pageNumber--;
      this.loadParkingSlots(this.currentLotId);
    }
  }

  onPageSizeChange(newSize: any): void {
    const parsed = Number(newSize) || 10;
    this.pageSize = parsed;
    this.pageNumber = 0;
    this.loadParkingSlots(this.currentLotId);
  }

  onSort(event: any) {
    // event.field, event.order (1 or -1)
    if (event && event.field) {
      this.sortField = event.field;
      this.sortDirection = event.order === -1 ? 'desc' : 'asc';
      this.pageNumber = 0;
      this.loadParkingSlots(this.currentLotId);
    }
  }

  // (search removed) - pagination updates handled by `onPage` and `onSort`

  /**
   * Returns the appropriate Tailwind CSS classes for the slot status
   */
  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      AVAILABLE: 'text-green-600 font-semibold',
      OCCUPIED: 'text-red-600 font-semibold',
      RESERVED: 'text-yellow-600 font-semibold',
      UNDER_SERVICE: 'text-gray-500 font-semibold',
    };
    return map[status?.toUpperCase?.()] ?? 'text-blue-600 font-semibold';
  }

  onDelete(id: number): void {
    this.auth.ParkingLotDelete(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Parking Lot Deleted Successfully', key: 'tl', life: 3000 });  
          setTimeout(() => { window.location.href = "/"; }, 3000);
        }
      },
      error: (err: any) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
          setTimeout(() => { window.location.href = "/"; }, 3000);
          return;
        }
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message, key: 'error', life: 3000   });
        console.log(err);
      },
    });
  }

  onReactivate(id: number): void {
    this.auth.ParkingLotActivate(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Parking Lot Reactivated Successfully', key: 'tl', life: 3000 });
          setTimeout(() => { window.location.href = "/"; }, 3000);
        }
      },
      error: (err: any) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
          setTimeout(() => { window.location.href = "/"; }, 3000);
          return;
        }
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message, key: 'error', life: 3000 });
        console.log(err);
      },
    });
  }

  onChangeSLotStatus(id: number, status: string): void {
    let data = {
      "status": status,
      "id": id
    };
    this.auth.ChangeSlotStatus(data).subscribe({
      next: (response) => {
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Parking Slot Status Changed Successfully', key: 'tl', life: 3000 });
          const lotId = sessionStorage.getItem("ID");
          this.loadParkingSlots(lotId);
        }
      },
      error: (err: any) => {
        if (err.status == 0) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Server is down. Please try again later.', key: 'error', life: 3000 });
          window.location.href = "/";
          return;
        }
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error.message, key: 'error', life: 3000 });
        console.log(err);
      }
    });
  }

  onBack(): void {
    window.location.href = '/';
  }
}
