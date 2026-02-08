package com.fincons.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an event for when a vehicle exits a parking lot.
 * This event is published to Kafka for asynchronous processing.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleExitedEvent {

    /** The unique identifier of the completed parking session. */
    private Long sessionId;

    /** The registration number of the vehicle that exited. */
    private String vehicleNumber;

    /** The identifier of the parking lot that was exited. */
    private Long parkingLotId;

    /** The name of the parking lot that was exited. */
    private String parkingLotName;

    /** The identifier of the parking slot that was vacated. */
    private Long parkingSlotId;

    /** The timestamp of the vehicle's entry. */
    private LocalDateTime entryTime;

    /** The timestamp of the vehicle's exit. */
    private LocalDateTime exitTime;

    /** The total amount charged for the parking session. */
    private Double totalAmount;
}
