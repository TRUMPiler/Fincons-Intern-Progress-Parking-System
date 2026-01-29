package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;

/**
 * Defines the contract for parking management operations within the parking system.
 * This interface provides the core functionalities for handling vehicle entry and exit.
 */
public interface ParkingService {

    /**
     * Processes the entry of a vehicle into a parking lot.
     * This method handles the logic for recording a vehicle's entry, assigning a parking slot,
     * and creating a new, active parking session.
     *
     * @param entryRequest A DTO containing the necessary information for vehicle entry,
     *                     such as vehicle number, type, and the parking lot ID.
     * @return A DTO representing the newly created and active parking session.
     */
    ParkingSessionDto enterVehicle(VehicleEntryRequestDto entryRequest);

    /**
     * Processes the exit of a vehicle from the parking lot.
     * This method handles the logic for recording a vehicle's exit, calculating the parking fee,
     * making the parking slot available, and completing the parking session.
     *
     * @param vehicleNumber The registration number of the exiting vehicle.
     * @return A DTO representing the completed parking session, including all charge calculation details.
     */
    ParkingSessionDto exitVehicle(String vehicleNumber);
}
