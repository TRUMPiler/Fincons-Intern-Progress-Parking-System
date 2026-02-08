package com.fincons.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for sending a high occupancy alert.
 * This event is triggered when a parking lot's occupancy exceeds a defined threshold.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HighOccupancyAlertDto {

    /** The unique identifier of the parking lot that has high occupancy. */
    private Long parkingLotId;

    /** A descriptive message for the alert. */
    private String message;

    /** The occupancy percentage that triggered the alert. */
    private double occupancyPercentage;
}
