package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a parking session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSessionDto
{
    /**
     * Unique ID.
     */
    private Long id;
    /**
     * Vehicle's registration number.
     */
    private String vehicleNumber;
    /**
     * ID of the occupied parking slot.
     */
    private Long parkingSlotId;

    /**
     * Name of the parking Lot for the slot that belongs
     */
    private String parkingLotName;
    /**
     * Entry timestamp.
     */
    private LocalDateTime entryTime;
    /**
     * Exit timestamp.
     */
    private LocalDateTime exitTime;
    /**
     * Total charge for the session.
     */
    private Double totalAmount;

    /**
     * Base price per hour at the time of exit.
     */
    private Double basePricePerHour;

    /**
     * Total number of billable hours.
     */
    private Long hoursCharged;

    /**
     * Occupancy percentage at the time of exit.
     */
    private Double occupancyPercentage;
    /**
     * Pricing multiplier applied based on occupancy.
     */
    private Double multiplier;
    /**
     * Current status (e.g., ACTIVE, COMPLETED).
     */
    private ParkingSessionStatus status;
}
