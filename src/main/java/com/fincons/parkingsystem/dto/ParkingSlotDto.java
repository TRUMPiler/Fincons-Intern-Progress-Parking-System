package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a parking slot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSlotDto {
    /**
     * Unique ID.
     */
    private Long id;
    /**
     * Unique number within the parking lot.
     */
    private String slotNumber;
    /**
     * Current status (e.g., AVAILABLE, OCCUPIED).
     */
    private SlotStatus status;
    /**
     * ID of the parking lot this slot belongs to.
     */
    private Long parkingLotId;
}
