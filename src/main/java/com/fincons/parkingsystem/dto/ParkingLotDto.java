package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for ParkingLot.
 * Used to transfer parking lot details between layers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingLotDto {
    /**
     * The unique identifier of the parking lot.
     */
    private Long id;
    /**
     * The name of the parking lot.
     */

    private String name;
    /**
     * The physical location of the parking lot.
     */
    private String location;
    /**
     * The total number of parking slots available in the lot.
     */
    private Integer totalSlots;
    /**
     * The base hourly price for parking in this lot.
     */
    private Double basePricePerHour;
    /**
     * The timestamp when the parking lot record was created.
     */
    private LocalDateTime createdAt;
    /**
     * A list of parking slots associated with this parking lot.
     */
    private List<ParkingSlotDto> parkingSlots;
}
