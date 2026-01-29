package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for ParkingSlot.
 * Used to transfer parking slot details between layers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSlotDto {
    /**
     * The unique identifier of the parking slot.
     */

    private Long id;
    /**
     * The unique number or identifier of the slot within its parking lot.
     */
    private String slotNumber;
    /**
     * The current status of the parking slot (e.g., AVAILABLE, OCCUPIED).
     */
    private SlotStatus status;
    /**
     * The ID of the parking lot to which this slot belongs.
     */
    private Long parkingLotId;
    /**
     * The name of the parking lot to which this slot belongs.
     */
}
