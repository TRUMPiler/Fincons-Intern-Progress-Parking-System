import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  RegisterParkingLot(data: any): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
    };
    return this.http.post<any>(this.apiUrl+"/parking-lots",data,httpOptions);
    }

    vehicleEntry(data: any): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
    };
    return this.http.post<any>(this.apiUrl+"/parking/entry",data,httpOptions);
    }
    vehicleExit(vehicleNumber:string): Observable<any> {

      
      let data={
        "vehicleNumber":vehicleNumber
      }
 
    return this.http.post<any>(this.apiUrl+"/parking/exit",data);
    }

 getParkingLots() {
  return this.http.get<any>(this.apiUrl+'/parking-lots');
} 

  getActiveSessions()
  {
    return this.http.get<any>(this.apiUrl+'/sessions/active');
  }
  getParkingSlotsById(id:string)
  {
    return this.http.get<any>(this.apiUrl+'/parking-lots/'+id+"/slots");
  }
  getHistorySessions()
  {
    return this.http.get<any>(this.apiUrl+'/sessions/history');
  }
  getParkingLotInfo(data:any)
  {
      return this.http.get<any>(this.apiUrl+'/parking-lots/'+data+"/stats");
  }
  getDeletedParkingLot()
  {
      return this.http.get<any>(this.apiUrl+'/parking-lots/with-inactive');
  }
  ParkingLotDelete(id:number)
  {
      return this.http.delete<any>(this.apiUrl+'/parking-lots/'+id);
  }
  ParkingLotActivate(id:number)
  {
      return this.http.patch<any>(this.apiUrl+'/parking-lots/'+id+'/reactivate',null);
  }
  ParkingLotReservation(data:any)
  {
      return this.http.post<any>(this.apiUrl+'/reservations',data);
  }
  getReservations()
  {
      return this.http.get<any>(this.apiUrl+'/reservations');
  }
  CancelReservation(id:number)
  {
      return this.http.delete<any>(this.apiUrl+'/reservations/'+id);
  }
  ArrivalReservation(id:number)
  {
      return this.http.post<any>(this.apiUrl+'/reservations/'+id+'/arrival',null);
  }
  ChangeSlotStatus(data:any)
  {
      return this.http.patch<any>(this.apiUrl+'/parking-lots/update-slot',data);
  }
}
