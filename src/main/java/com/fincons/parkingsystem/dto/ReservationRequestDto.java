package com.fincons.parkingsystem.dto;

import com.fincons.parkingsystem.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationRequestDto
{
    @NotBlank(message = "Vehicle number cannot be empty.")
    private String vehicleNumber;
    @NotNull(message = "Vehicle type cannot be null.")
    private VehicleType vehicleType;
    @NotNull(message = "Parking lot ID cannot be null.")
    private Long parkingLotId;
}
