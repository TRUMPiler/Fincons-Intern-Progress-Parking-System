package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.entity.ParkingLot;

/**
 * Service for managing parking slots.
 */
public interface ParkingSlotService {

    /**
     * Creates parking slots for a new parking lot.
     *
     * @param parkingLot The new parking lot.
     * @param slots The number of slots to create.
     */
    void createParkingSlotsForLot(ParkingLot parkingLot, int slots);

    /**
     * Retrieves the availability of slots for a parking lot.
     *
     * @param parkingLotId The ID of the parking lot.
     * @return A DTO with the list of slots and the count of available slots.
     */
    ParkingSlotAvailability getParkingSlotAvailability(Long parkingLotId);
}
