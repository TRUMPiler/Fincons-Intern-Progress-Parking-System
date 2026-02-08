package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for representing parking lot data.
 * This DTO is used for creating and retrieving parking lot information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingLotDto {

    /** The unique identifier of the parking lot. */
    private Long id;

    /** The name of the parking lot. Cannot be blank. */
    @NotBlank(message = "Parking lot name cannot be empty.")
    private String name;

    /** The physical location or address of the parking lot. */
    private String location;

    /** The total number of parking slots in the lot. Must be between 1 and 50. */
    @NotNull(message = "Total slots cannot be null.")
    @Min(value = 1, message = "Total slots must be at least 1.")
    @Max(value = 50, message = "Total slots cannot be more than 50")
    private Integer totalSlots;

    /** The base price per hour for parking. Must be between 0 and 1000. */
    @NotNull(message = "Base price per hour cannot be null.")
    @Min(value = 0, message = "Base price per hour must be a positive value.")
    @Max(value = 1000, message = "Base price per hour cannot be more than 1000")
    private Double basePricePerHour;

    /** The timestamp when the parking lot was created. */
    private LocalDateTime createdAt;

    /** A list of DTOs for the parking slots within this lot. */
    private List<ParkingSlotDto> parkingSlots;
}
