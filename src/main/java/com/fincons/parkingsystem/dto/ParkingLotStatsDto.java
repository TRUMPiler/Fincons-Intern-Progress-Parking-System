package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for representing parking lot statistics.
 * This DTO provides a summary of a parking lot's performance and status.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingLotStatsDto {

    /** The unique identifier of the parking lot. */
    private Long parkingLotId;

    /** The name of the parking lot. */
    private String parkingLotName;

    /** The total number of parking slots in the lot. */
    private Integer totalSlots;

    /** The number of slots that are currently occupied. */
    private Long occupiedSlots;

    /** The number of parking sessions that are currently active. */
    private Long availableSlots;

    /** The base price per hour for parking in this lot. */
    private Double basePricePerHour;

    /** The total revenue generated from all completed parking sessions. */
    private Double totalRevenue;

    /** The current occupancy percentage of the parking lot. */
    private Double occupancyPercentage;

    /** The total revenue generated on the current day. */
    private Double revenueToday;

    /** A flag indicating whether the parking lot has been soft-deleted. */
    private Boolean deleted;
}
