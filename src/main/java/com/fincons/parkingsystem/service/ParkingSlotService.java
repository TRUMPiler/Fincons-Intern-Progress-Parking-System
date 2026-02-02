package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.entity.ParkingLot;

/**
 * This is the contract for my service that manages parking slots.
 * It defines how I can create slots and check their availability.
 */
public interface ParkingSlotService {

    /**
     * This method creates all the individual parking slots for a new parking lot.
     *
     * @param parkingLot The parking lot entity that the slots will belong to.
     * @param slots The number of slots I want to create.
     */
    void createParkingSlotsForLot(ParkingLot parkingLot, int slots);

    /**
     * This method gets the current availability of slots for a specific parking lot.
     *
     * @param parkingLotId The ID of the parking lot I want to check.
     * @return A DTO that contains a list of all the slots and a count of how many are available.
     */
    ParkingSlotAvailability getParkingSlotAvailability(Long parkingLotId);
}
