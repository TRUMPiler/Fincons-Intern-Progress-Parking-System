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
    this.change.detectChanges();
  }
  onBack()
  { 
    window.location.href='/';

  }
}
