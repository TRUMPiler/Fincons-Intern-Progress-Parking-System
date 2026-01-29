package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for ParkingSession.
 * Used to transfer parking session details between layers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSessionDto {
    /**
     * The unique identifier of the parking session.
     */
    private Long id;
    /**
     * The registration number of the vehicle involved in the session.
     */
    private String vehicleNumber;
    /**
     * The ID of the parking slot occupied during the session.
     */
    private Long parkingSlotId;
    /**
     * The timestamp when the vehicle entered the parking slot.
     */
    private LocalDateTime entryTime;
    /**
     * The timestamp when the vehicle exited the parking slot.
     */
    private LocalDateTime exitTime;
    /**
     * The total amount charged for the parking session.
     */
    private Double totalAmount;

    /**
     * The base hourly price for parking in this lot.
     */
    private Double basePricePerHour;

    /**
     * The number of hours the vehicle was charged for.
     */
    private Long hoursCharged;

    /**
     * Percentage of Occupancy that the user exited.
     */
    private Double occupancyPercentage;
    /**
     * based on Occupancy amount was multiplied
     */
    private Double multiplier;
    /**
     * The current status of the parking session (e.g., ACTIVE, COMPLETED).
     */
    private ParkingSessionStatus status;
}
