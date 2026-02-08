package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * Retrieves a paginated list of parking slots for a specific parking lot.
     *
     * @param parkingLotId The unique identifier of the parking lot to check.
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs representing the parking slots.
     */
    Page<ParkingSlotDto> getParkingSlotAvailability(Long parkingLotId, Pageable pageable);

    /**
     * Updates the information for a specific parking slot.
     *
     * @param parkingSlotDto A DTO containing the updated information for the parking slot.
     * @return The updated {@link ParkingSlotDto}.
     */
    ParkingSlotDto updateParkingSlotInformation(ParkingSlotDto parkingSlotDto);
}
