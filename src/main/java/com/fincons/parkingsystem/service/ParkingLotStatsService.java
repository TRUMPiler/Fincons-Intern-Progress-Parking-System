package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingLotStatsDto;

/**
 * Service for retrieving statistical data about parking lots.
 */
public interface ParkingLotStatsService
{
    /**
     * Retrieves statistics for a specific parking lot.
     *
     * @param id The ID of the parking lot.
     * @return Statistics for the parking lot.
     */
    ParkingLotStatsDto getParkingLotStats(Long id);
}
