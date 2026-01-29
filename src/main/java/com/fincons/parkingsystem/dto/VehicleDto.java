package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Vehicle.
 * Used to transfer vehicle details between layers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDto {
    /**
     * The unique identifier of the vehicle.
     */
    private Long id;
    /**
     * The registration number of the vehicle.
     */
    private String vehicleNumber;
    /**
     * The type of the vehicle (e.g., CAR, BIKE).
     */
    private VehicleType vehicleType;

}
