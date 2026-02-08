package com.fincons.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for broadcasting real-time occupancy updates for a parking lot.
 * This DTO provides a complete snapshot of the lot's current state.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OccupancyUpdateDto {

    /** The unique identifier of the parking lot being updated. */
    private Long parkingLotId;

    /** The current number of occupied slots. */
    private long occupiedSlots;

    /** The current number of available slots. */
    private long availableSlots;


    /** The calculated occupancy percentage. */
    private double occupancyPercentage;
}
