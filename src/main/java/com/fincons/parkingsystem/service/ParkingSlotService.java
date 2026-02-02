package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.entity.ParkingLot;

/**
 * Service interface for managing parking slot resources.
 * This contract defines the business logic for creating, updating, and retrieving information
 * about parking slots.
 */
public interface ParkingSlotService {

    /**
     * Creates the individual parking slots for a newly created parking lot.
     *
     * @param parkingLot The parking lot entity to which the slots will be added.
     * @param slots The total number of slots to create.
     */
    void createParkingSlotsForLot(ParkingLot parkingLot, int slots);

    /**
     * Retrieves the current availability of parking slots for a specific parking lot.
     *
     * @param parkingLotId The unique identifier of the parking lot to check.
     * @return A DTO that encapsulates the list of all parking slots and the count of available ones.
     */
    ParkingSlotAvailability getParkingSlotAvailability(Long parkingLotId);

    /**
     * Updates the information for a specific parking slot.
     *
     * @param parkingSlotDto A DTO containing the updated information for the parking slot.
     * @return The updated {@link ParkingSlotDto}.
     */
    ParkingSlotDto updateParkingSlotInformation(ParkingSlotDto parkingSlotDto);
}
