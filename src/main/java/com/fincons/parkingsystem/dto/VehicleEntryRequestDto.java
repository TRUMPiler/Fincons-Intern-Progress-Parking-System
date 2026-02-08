package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for handling incoming vehicle entry requests.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class VehicleEntryRequestDto {

    /** The registration number of the vehicle attempting to enter. Cannot be blank. */
    @NotBlank(message = "Vehicle number cannot be empty.")
    private String vehicleNumber;

    /** The type of the vehicle (e.g., CAR, BIKE). Cannot be null. */
    @NotNull(message = "Vehicle type cannot be null.")
    private VehicleType vehicleType;

    /** The unique identifier of the parking lot the vehicle intends to enter. Cannot be null. */
    @NotNull(message = "Parking lot ID cannot be null.")
    private Long parkingLotId;
}
