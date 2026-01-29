package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.entity.ParkingLot;

import java.util.List;

/**
 * Defines the contract for managing parking lots within the parking system.
 * This interface provides methods for creating, retrieving, and querying parking lots.
 */
public interface ParkingLotService {

    /**
     * Creates a new parking lot based on the provided data.
     *
     * @param parkingLotDto A DTO containing the details for the new parking lot.
     * @return A DTO representing the newly created parking lot.
     */
    ParkingLotDto createParkingLot(ParkingLotDto parkingLotDto);

    /**
     * Retrieves a list of all parking lots in the system.
     *
     * @return A list of DTOs, each representing a parking lot.
     */
    List<ParkingLotDto> getAllParkingLots();


}
