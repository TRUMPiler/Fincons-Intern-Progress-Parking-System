package com.fincons.parkingsystem.dto;

import com.fincons.parkingsystem.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for handling incoming parking reservation requests.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationRequestDto {

    /** The registration number of the vehicle for which the reservation is being made. */
    @NotBlank(message = "Vehicle number cannot be empty.")
    private String vehicleNumber;

    /** The type of the vehicle (e.g., CAR, BIKE). */
    @NotNull(message = "Vehicle type cannot be null.")
    private VehicleType vehicleType;

    /** The unique identifier of the parking lot where the reservation is requested. */
    @NotNull(message = "Parking lot ID cannot be null.")
    private Long parkingLotId;
}
