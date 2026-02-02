package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing the availability of parking slots in a lot.
 * This class provides a summary of all parking slots in a lot, along with a count of how many
 * are currently available. It is useful for clients who need to quickly assess parking availability.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSlotAvailability {
    /**
     * A list of Data Transfer Objects for all parking slots in the lot.
     * This provides detailed information about each slot.
     */
    private List<ParkingSlotDto> parkingSlots;
    /**
     * The total number of parking slots that are currently available.
     * This is a convenient summary for quick reference.
     */
    private Long availableSlots;
}
