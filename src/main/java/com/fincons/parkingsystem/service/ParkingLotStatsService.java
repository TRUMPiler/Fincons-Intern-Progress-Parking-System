package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingLotStatsDto;

/**
 * This is the contract for my service that provides statistics about parking lots.
 * It defines how I can get performance data for a specific lot.
 */
public interface ParkingLotStatsService
{
    /**
     * This method retrieves key statistics for a single parking lot.
     *
     * @param id The ID of the parking lot I want to get stats for.
     * @return A DTO containing the statistics, like revenue and occupancy.
     */
    ParkingLotStatsDto getParkingLotStats(Long id);
}
