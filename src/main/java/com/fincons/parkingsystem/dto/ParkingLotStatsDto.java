package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for parking lot statistics.
 * This class is used to provide a summary of a parking lot's status, including occupancy,
 * revenue, and other relevant metrics. It is designed for reporting and monitoring purposes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingLotStatsDto {

    /**
     * The unique identifier of the parking lot.
     */
    private  Long parkingLotId;
    /**
     * The name of the parking lot.
     */
    private String parkingLotName;
    /**
     * The total number of parking slots in the lot.
     */
    private Integer totalSlots;
    /**
     * The number of slots that are currently occupied.
     */
    private Long occupiedSlots;
    /**
     * The number of parking sessions that are currently active.
     */
    private Long activeSessions;
    /**
     * The base price per hour for parking in this lot.
     */
    private Double basePricePerHour;
    /**
     * The total revenue generated from all completed parking sessions.
     */
    private Double totalRevenue;
    /**
     * The current occupancy percentage of the parking lot.
     * This is calculated as (occupiedSlots / totalSlots) * 100.
     */
    private Double occupancyPercentage;
    /**
     * The total revenue generated on the current day.
     * This provides a daily snapshot of the parking lot's performance.
     */
    private Double  revenueToday;
    /**
     * A flag indicating whether the parking lot has been soft-deleted.
     * Soft-deleted lots are not permanently removed from the database.
     */
    private Boolean deleted;

}
