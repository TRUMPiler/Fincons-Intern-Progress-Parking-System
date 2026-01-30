import { CommonModule } from '@angular/common';
import { AfterContentInit, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-vehicle-exit',
  imports: [FormsModule, CommonModule],
  standalone: true,
  templateUrl: './vehicle-exit.html',
  styleUrl: './vehicle-exit.css',
})
export class VehicleExit implements OnInit {
  vehicleNumber: string = '';
  billData: any[] = [];
  showBillPopup: boolean = false;
  constructor(public authService: AuthService,private cdr:ChangeDetectorRef) { }
  ngOnInit(): void {
  }
  
  onSubmit() {
    this.vehicleNumber=this.vehicleNumber.trimStart().toUpperCase().trimEnd();
    this.authService.vehicleExit(this.vehicleNumber).subscribe(
      {
        next: (response) => {
            if(response.success)
            {
              this.billData=[response.data];
              console.log(this.billData);
             
              
              alert(response.message);
              this.showBillPopup=true;
              this.cdr.detectChanges();
             
            }
            else
            {         
              alert(response.message);
            }
      },
      error(err) {
        if(err.status==0)
          {
            alert("Server is down. Please try again later.");
            return;
          }
        alert(err.error.message);
        console.log(err);
      },
    }
    )
  }
   confirmExit() {
    this.showBillPopup = false;
    alert('Vehicle exited successfully');
    this.vehicleNumber = '';
  }
  onBack()
  {
    window.location.href="/";
   
  }

   closePopup() {
    this.showBillPopup = false;
  }
}
