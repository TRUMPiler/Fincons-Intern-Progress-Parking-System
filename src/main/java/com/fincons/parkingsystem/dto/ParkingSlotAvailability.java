package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the availability of parking slots in a lot.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSlotAvailability {
    /**
     * List of all parking slots in the lot.
     */
    private List<ParkingSlotDto> parkingSlots;
    /**
     * Total count of currently available slots.
     */
    private Long availableSlots;
}
