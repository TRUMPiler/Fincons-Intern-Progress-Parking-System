package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;

/**
 * Service for handling vehicle entry and exit.
 */
public interface ParkingService {

    /**
     * Creates a new parking session when a vehicle enters.
     *
     * @param entryRequest DTO with vehicle and parking lot details.
     * @return The created parking session.
     */
    ParkingSessionDto enterVehicle(VehicleEntryRequestDto entryRequest);

    /**
     * Completes a parking session when a vehicle exits.
     *
     * @param vehicleNumber The vehicle's registration number.
     * @return The completed parking session with charge details.
     */
    ParkingSessionDto exitVehicle(String vehicleNumber);
}
