package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for representing parking reservation data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationDto {

    /** The unique identifier of the reservation. */
    private Long id;

    /** The registration number of the vehicle for which the reservation was made. */
    private String vehicleNumber;

    /** The identifier of the parking slot where the reservation is valid. */
    private Long parkingSlotId;

    /** The identifier of the parking lot where the reservation is valid. */
    private Long parkingLotId;

    /** The name of the parking lot where the reservation is valid. */
    private String parkingLotName;

    /** The timestamp when the reservation was created. */
    private LocalDateTime reservationTime;

    /** The timestamp when the reservation will expire if not claimed. */
    private LocalDateTime expirationTime;

    /** The current status of the reservation (e.g., ACTIVE, COMPLETED, CANCELLED). */
    private ReservationStatus status;
}
