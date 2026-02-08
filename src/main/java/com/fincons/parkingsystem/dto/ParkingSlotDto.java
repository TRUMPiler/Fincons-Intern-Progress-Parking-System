package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for representing parking slot data.
 * This DTO provides a simplified view of a parking slot, suitable for client-facing responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSlotDto {

    /** The unique identifier of the parking slot. */
    private Long id;

    /** The unique number or identifier for the slot within its parking lot (e.g., "A1"). */
    private String slotNumber;

    /** The current status of the parking slot (e.g., AVAILABLE, OCCUPIED, RESERVED). */
    private SlotStatus status;

    /** The identifier of the parking lot to which this slot belongs. */
    private Long parkingLotId;
}
