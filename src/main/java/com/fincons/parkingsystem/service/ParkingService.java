package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;

/**
 * Service interface for core parking operations.
 * This contract defines the primary business logic for managing the lifecycle of a parking session,
 * from vehicle entry to exit.
 */
public interface ParkingService {

    /**
     * Processes the entry of a vehicle into a parking lot.
     * This method is responsible for creating a new parking session, allocating an available parking slot,
     * and persisting the session details.
     *
     * @param entryRequest A DTO containing the vehicle's details and the target parking lot ID.
     * @return A DTO representing the newly created and active parking session.
     */
    ParkingSessionDto enterVehicle(VehicleEntryRequestDto entryRequest);

    /**
     * Processes the exit of a vehicle from a parking lot.
     * This method handles the completion of the active parking session, including calculating charges,
     * updating the session status, and deallocating the parking slot.
     *
     * @param vehicleNumber The registration number of the exiting vehicle.
     * @return A DTO representing the completed parking session, including all charge details.
     */
    ParkingSessionDto exitVehicle(String vehicleNumber);
}
