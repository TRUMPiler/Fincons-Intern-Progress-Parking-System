package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a parking lot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingLotDto {
    /**
     * Unique ID.
     */
    private Long id;
    /**
     * Name of the parking lot.
     */
    @NotBlank(message = "Parking lot name cannot be empty.")
    private String name;
    /**
     * Physical location.
     */
    private String location;
    /**
     * Total number of slots.
     */
    @NotNull(message = "Total slots cannot be null.")
    @Min(value = 1, message = "Total slots must be at least 1.")
    private Integer totalSlots;
    /**
     * Base price per hour.
     */
    @NotNull(message = "Base price per hour cannot be null.")
    @Min(value = 0, message = "Base price per hour must be a positive value.")
    private Double basePricePerHour;
    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;
    /**
     * List of parking slots.
     */
    private List<ParkingSlotDto> parkingSlots;
}
