package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a vehicle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDto {
    /**
     * Unique ID.
     */
    private Long id;
    /**
     * Vehicle's registration number.
     */
    @NotBlank(message = "Vehicle number cannot be empty.")
    private String vehicleNumber;
    /**
     * Type of vehicle (e.g., CAR, BIKE).
     */
    private VehicleType vehicleType;

}
