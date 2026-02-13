package com.fincons.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * Data Transfer Object representing an event for when a vehicle enters a parking lot.
 * This event is published to Kafka for asynchronous processing.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleEnteredEvent {

    /** The unique identifier of the new parking session. */
    private Long sessionId;

    /** The registration number of the vehicle that entered. */
    private String vehicleNumber;

    /** The identifier of the parking lot that was entered. */
    private Long parkingLotId;

    /** The identifier of the parking slot that was occupied. */
    private Long parkingSlotId;
    /** The identifier of the parking slot that was vacated. */
    private String parkingSlotNumber;
    /** The name of the parking lot that was entered. */
    private String parkingLotName;

    /** The timestamp of the vehicle's entry. */
    private Instant entryTime;
}
