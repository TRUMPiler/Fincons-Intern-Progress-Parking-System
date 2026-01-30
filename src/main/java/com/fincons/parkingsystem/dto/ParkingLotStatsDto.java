package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents statistics for a parking lot.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingLotStatsDto {

    /**
     * Unique ID of the parking lot.
     */
    private  Long parkingLotId;
    /**
     * Name of the parking lot.
     */
    private String parkingLotName;
    /**
     * Total number of slots.
     */
    private Integer totalSlots;
    /**
     * Number of currently occupied slots.
     */
    private Long occupiedSlots;
    /**
     * Number of currently active parking sessions.
     */
    private Long activeSessions;
    /**
     * Base price per hour.
     */
    private Double basePricePerHour;
    /**
     * Total revenue from all completed sessions.
     */
    private Double totalRevenue;
    /**
     * Current occupancy percentage.
     */
    private Double occupancyPercentage;
    /**
     * Revenue of the slot based on single day
     */
    private Double  revenueToday;
}
