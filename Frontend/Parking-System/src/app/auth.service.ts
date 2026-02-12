/**
 * AuthService
 * Handles all HTTP requests to the backend API for the Parking System
 * Manages parking lot operations, vehicle entry/exit, reservations, and sessions
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // Base API URL used for backend HTTP requests
  private apiUrl = 'https://fincons-intern-progress-parking-system-production.up.railway.app/api';

  constructor(private http: HttpClient) {}

  /**
   * Register a new parking lot
   * @param data - Parking lot registration data (name, location, total slots, base price)
   * @returns Observable with API response
   */
  RegisterParkingLot(data: any): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
    };
    return this.http.post<any>(this.apiUrl + "/parking-lots", data, httpOptions);
  }

  /**
   * Record vehicle entry into a parking lot
   * @param data - Vehicle entry data (vehicle number, vehicle type, parking lot ID)
   * @returns Observable with API response
   */
  vehicleEntry(data: any): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
    };
    return this.http.post<any>(this.apiUrl + "/parking/entry", data, httpOptions);
  }

  /**
   * Record vehicle exit from a parking lot
   * @param vehicleNumber - The vehicle number to exit
   * @returns Observable with exit details and bill information
   */
  vehicleExit(vehicleNumber: string): Observable<any> {
    let data = {
      "vehicleNumber": vehicleNumber
    }
    return this.http.post<any>(this.apiUrl + "/parking/exit", data);
  }

  /**
   * Get all active parking lots
   * @returns Observable with list of parking lots
   */
  getParkingLots(
    
  ) {
    return this.http.get<any>(this.apiUrl + `/parking-lots/all`);
  }


  getALLParkingLots(
     page: number, 
    size: number, 
    sortField: string, 
    sortDirection: 'asc' | 'desc'
  ) {
    return this.http.get<any>(this.apiUrl + `/parking-lots/with-inactive?page=${page}&size=${size}&sort=${sortField},${sortDirection}`);
  }

  /**
   * Get all active parking sessions
   * @returns Observable with list of active sessions
   */
  getActiveSessions() {
    return this.http.get<any>(this.apiUrl + '/sessions/active');
  }

  /**
   * Get parking slots for a specific parking lot
   * @param id - The parking lot ID
   * @returns Observable with list of parking slots
   */
  getParkingSlotsById(id: string) {
    return this.http.get<any>(this.apiUrl + '/parking-slots/by-lot/' + id + "");
  }

  /**
   * Get paginated parking slots for a specific parking lot
   * @param parkingLotId - The parking lot ID
   * @param page - Page number (0-indexed)
   * @param size - Number of records per page
   * @param sortField - Field to sort by (e.g., 'slotNumber')
   * @param sortDirection - Sort direction ('asc' or 'desc')
   * @returns Observable with paginated parking slots
   */
  getParkingSlotsPaginated(
    parkingLotId: string,
    page: number,
    size: number,
    sortField: string,
    sortDirection: 'asc' | 'desc'
  ): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/parking-slots/by-lot/${parkingLotId}?page=${page}&size=${size}&sort=${sortField},${sortDirection}`
    );
  }

  /**
   * Get historical parking sessions with server-side pagination
   * @param page - Page number (0-indexed)
   * @param size - Number of records per page
   * @param sortField - Field to sort by (e.g., 'exitTime', 'entryTime')
   * @param sortDirection - Sort direction ('asc' or 'desc')
   * @returns Observable with paginated session history
   */
  getHistorySessionsPaginated(
    page: number, 
    size: number, 
    sortField: string, 
    sortDirection: 'asc' | 'desc'
  ): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/sessions/history?page=${page}&size=${size}&sort=${sortField},${sortDirection}`
    );
  }
  getPlacesApi(text: string)
  {
      const apiKey = 'aPFf8ok93olEtVMvJ8m5NyHdtEM6oCRgr6LvMaGK'; 
      const url= `https://api.olamaps.io/places/v1/autocomplete?input=${encodeURIComponent(text)}&api_key=${apiKey}`;
      return this.http.get<any>(url);
  }
  /**
   * Get historical parking sessions (non-paginated)
   * @returns Observable with list of past sessions
   */
  getHistorySessions() {
    return this.http.get<any>(this.apiUrl + '/sessions/history');
  }

  /**
   * Get detailed statistics and information for a parking lot
   * @param data - The parking lot ID
   * @returns Observable with parking lot stats and information
   */
  getParkingLotInfo(data: any) {
    return this.http.get<any>(this.apiUrl + '/parking-lots/' + data + "/stats");
  }

  /**
   * Get all parking lots including inactive/deleted ones
   * @returns Observable with list of all parking lots
   */
  getDeletedParkingLot() {
    return this.http.get<any>(this.apiUrl + '/parking-lots/with-inactive');
  }

  /**
   * Delete a parking lot
   * @param id - The parking lot ID to delete
   * @returns Observable with deletion response
   */
  ParkingLotDelete(id: number) {
    return this.http.delete<any>(this.apiUrl + '/parking-lots/' + id);
  }

  /**
   * Reactivate a deleted parking lot
   * @param id - The parking lot ID to reactivate
   * @returns Observable with reactivation response
   */
  ParkingLotActivate(id: number) {
    return this.http.patch<any>(this.apiUrl + '/parking-lots/' + id + '/reactivate', null);
  }

  /**
   * Create a new parking reservation
   * @param data - Reservation data (vehicle number, vehicle type, parking lot ID)
   * @returns Observable with reservation response
   */
  ParkingLotReservation(data: any) {
    return this.http.post<any>(this.apiUrl + '/reservations', data);
  }

  /**
   * Get all parking reservations
   * @returns Observable with list of reservations
   */
  getReservations() {
    return this.http.get<any>(this.apiUrl + '/reservations');
  }

  /**
   * Get paginated reservations (server-side pagination)
   * @param page - Page number (0-indexed)
   * @param size - Number of records per page
   * @param sortField - Field to sort by
   * @param sortDirection - Sort direction ('asc' | 'desc')
   */
  getReservationsPaginated(
    page: number,
    size: number,
    sortField: string,
    sortDirection: 'asc' | 'desc'
  ) {
    return this.http.get<any>(`${this.apiUrl}/reservations?page=${page}&size=${size}&sort=${sortField},${sortDirection}`);
  }

  /**
   * Cancel a parking reservation
   * @param id - The reservation ID to cancel
   * @returns Observable with cancellation response
   */
  CancelReservation(id: number) {
    return this.http.delete<any>(this.apiUrl + '/reservations/' + id);
  }

  /**
   * Mark a reservation as arrived
   * @param id - The reservation ID
   * @returns Observable with arrival confirmation response
   */
  ArrivalReservation(id: number) {
    return this.http.post<any>(this.apiUrl + '/reservations/' + id + '/arrival', null);
  }

  /**
   * Update the status of a parking slot
   * @param data - Slot status update data (ID and new status)
   * @returns Observable with status update response
   */
  ChangeSlotStatus(data: any) {
    return this.http.patch<any>(this.apiUrl + '/parking-slots/update-slot', data);
  }
}
