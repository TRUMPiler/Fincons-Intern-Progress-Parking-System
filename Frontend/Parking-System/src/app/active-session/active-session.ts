import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-active-session',
  imports: [CommonModule,FormsModule,DatePipe],
  standalone: true,
  templateUrl: './active-session.html',
  styleUrl: './active-session.css',
})
export class ActiveSession implements OnInit {

  constructor(public authService:AuthService)
  {}
  sessionData:any[]=[];
  sessionHistroy:any[]=[];
  ngOnInit(): void {
    this.authService.getActiveSessions().subscribe(
      {
        next: (response) => {
          if (response.success) {
            this.sessionData = response.data;
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
          console.error('There was an error!', err);
        },
      }
    );
    this.authService.getHistorySessions().subscribe(
      {
        next:(Response)=>
        {
          if(Response.success)
          {
            this.sessionHistroy=Response.data;
            console.log(this.sessionHistroy);
          }
          else
          {
            alert(Response.message);
          }
        },
        error:(err)=>
        {
          if(err.status==0)
          {
           
            return;
          }
          alert(err.error.message);
          console.error('There was an error!', err);  
        }
      }
    );
  }
  onBack()
  {
    window.location.href="/";
  }
}
