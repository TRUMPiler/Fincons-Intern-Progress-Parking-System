package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents the request data for a vehicle entering a parking lot.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class VehicleEntryRequestDto {
    /**
     * Vehicle's registration number.
     */
    @NotBlank(message = "Vehicle number cannot be empty.")
    private String vehicleNumber;
    /**
     * Type of vehicle (e.g., CAR, BIKE).
     */
    @NotNull(message = "Vehicle type cannot be null.")
    private VehicleType vehicleType;
    /**
     * ID of the parking lot being entered.
     */
    @NotNull(message = "Parking lot ID cannot be null.")
    private Long parkingLotId;
}
