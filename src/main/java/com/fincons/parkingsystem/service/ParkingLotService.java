package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingLotDto;

import java.util.List;

/**
 * Service for managing parking lots.
 */
public interface ParkingLotService {

    /**
     * Creates a new parking lot.
     *
     * @param parkingLotDto DTO with the new parking lot's details.
     * @return The newly created parking lot.
     */
    ParkingLotDto createParkingLot(ParkingLotDto parkingLotDto);

    /**
     * Retrieves a list of all parking lots.
     *
     * @return A list of all parking lots.
     */
    List<ParkingLotDto> getAllParkingLots();


}
