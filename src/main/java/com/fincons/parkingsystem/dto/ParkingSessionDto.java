package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * Data Transfer Object for representing parking session data.
 * This DTO provides a comprehensive view of a parking session, including charge details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSessionDto {

    /** The unique identifier of the parking session. */
    private Long id;

    /** The registration number of the vehicle associated with the session. */
    private String vehicleNumber;

    /** The identifier of the parking slot occupied during the session. */
    private Long parkingSlotId;
    /** the slot number of the parking slot according to the parking lot **/
    private Long parkingSlotNumber;
    /** The name of the parking lot where the session took place. */
    private String parkingLotName;

    /** The timestamp when the vehicle entered the parking lot. */
    private Instant entryTime;

    /** The timestamp when the vehicle exited the parking lot. Null for active sessions. */
    private Instant exitTime;

    /** The total amount charged for the parking session, calculated upon exit. */
    private Double totalAmount;

    /** The base price per hour at the time of exit, used for charge calculation. */
    private Double basePricePerHour;

    /** The total number of hours for which the user was charged. */
    private Long hoursCharged;

    /** The occupancy percentage of the lot at the time of exit, which may influence pricing. */
    private Double occupancyPercentage;

    /** The pricing multiplier applied to the base price, based on occupancy. */
    private Double multiplier;

    /** The current status of the parking session (e.g., ACTIVE, COMPLETED). */
    private ParkingSessionStatus status;
}
