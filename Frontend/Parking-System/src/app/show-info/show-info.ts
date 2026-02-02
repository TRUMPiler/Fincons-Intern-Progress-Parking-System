import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { ChangeDetectorRef } from '@angular/core';
@Component({
  selector: 'app-show-info',
  imports: [CommonModule],
  standalone:true,
  templateUrl: './show-info.html',
  styleUrl: './show-info.css',
})
export class ShowInfo implements OnInit {
  public parkingLotInfo:any[]=[];
  public parkingSlots:any[]=[];
  constructor(private auth:AuthService,private change:ChangeDetectorRef){
  }
  ngOnInit(): void {
    let id=sessionStorage.getItem("ID");
    this.auth.getParkingLotInfo(id).subscribe(
      {
        next:(response)=>
        {
          this.parkingLotInfo=[response.data];
          console.log(this.parkingLotInfo);
        },error(err) {
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
    this.auth.getParkingSlotsById(id!).subscribe({
      next:(response)=>{
        this.parkingSlots=response.data.parkingSlots;
        console.log(this.parkingSlots);
      },error(err) {
        if(err.status==0)
          {
            alert("Server is down. Please try again later.");
            window.location.href="/";
            return;
          }
        alert(err.error.message);
        console.log(err);
      }
    });
    this.change.detectChanges();
  }
  onDelete(id:number)
  {
    
    this.auth.ParkingLotDelete(id).subscribe(
      {
        next:(response)=>
        {
          if(response.success)
          {
            alert("Parking Lot Deleted Successfully");
                window.location.href="/";
          }

        },
        error(err) {
          if(err.status==0)
          {
            alert("Server is down. Please try again later.");
            window.location.href="/";
            return;

          }
          alert(err.error.message);
          console.log(err);
        },
      }
    );
  }
  onReactivate(id:number)
  {
    this.auth.ParkingLotActivate(id).subscribe(
      {
        next:(response)=>
        {
          if(response.success)
          {
            alert("Parking Lot Reactivated Successfully");
            window.location.href="/";
          }
        },
        error(err) {
          if(err.status==0)
          {
            alert("Server is down. Please try again later.");
            window.location.href="/";
            return;
            
          }
          alert(err.error.message);
          console.log(err);
        },
      }
    );
    
  }
  onChangeSLotStatus(Id:number,status:string)
  {
    let data={
      "status":status,
      "id":Id
    };
    this.auth.ChangeSlotStatus(data).subscribe(
      {

        next:(response)=>
        {
          if(response.success)
          {
            alert("Parking Slot Status Changed Successfully");
            window.location.href="/show-info";
          }
        },
        error(err) {
          if(err.status==0)
          {
            alert("Server is down. Please try again later.");
            window.location.href="/";
            return;
          }
          alert(err.error.message);
          console.log(err);
        }
      }
    );
  }
  onBack()
  { 
    window.location.href='/';
  }
}
