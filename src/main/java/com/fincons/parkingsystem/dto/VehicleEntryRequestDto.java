package com.fincons.parkingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fincons.parkingsystem.entity.VehicleType;
import lombok.Data;


/**
 * Data Transfer Object for a vehicle entry request.
 * Used to capture details required when a vehicle enters a parking lot.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleEntryRequestDto {
    /**
     * The registration number of the vehicle entering the parking lot.
     */
    private String vehicleNumber;
    /**
     * The type of the vehicle (e.g., CAR, BIKE).
     */
    private VehicleType vehicleType;
    /**
     * The ID of the parking lot the vehicle is entering.
     */
    private Long parkingLotId;
}
