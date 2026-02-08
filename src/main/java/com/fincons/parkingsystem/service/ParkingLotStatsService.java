package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingLotStatsDto;

/**
 * Service interface for retrieving statistical data about parking lots.
 * This contract defines how to fetch aggregated statistics for a specific parking lot.
 */
public interface ParkingLotStatsService {

    /**
     * Retrieves a set of statistics for a specific parking lot, identified by its ID.
     * This includes data like total slots, occupied slots, total revenue, and daily revenue.
     *
     * @param id The unique identifier of the parking lot for which to retrieve statistics.
     * @return A DTO containing the various statistics for the specified parking lot.
     */
    ParkingLotStatsDto getParkingLotStats(Long id);
}
