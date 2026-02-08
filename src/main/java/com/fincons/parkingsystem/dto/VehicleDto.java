package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for representing vehicle data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDto {

    /** The unique identifier of the vehicle. */
    private Long id;

    /** The registration number of the vehicle. Cannot be blank. */
    @NotBlank(message = "Vehicle number cannot be empty.")
    private String vehicleNumber;

    /** The type of the vehicle (e.g., CAR, BIKE). */
    private VehicleType vehicleType;
}
