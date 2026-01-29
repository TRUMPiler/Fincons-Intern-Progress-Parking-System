package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.dto.VehicleDto;
import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;
import com.fincons.parkingsystem.service.ParkingService;
import com.fincons.parkingsystem.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Handles REST requests for vehicle entry and exit.
 */
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    /**
     * Creates a new parking session when a vehicle enters.
     *
     * @param entryRequestDto DTO with vehicle and parking lot details.
     * @return The created parking session.
     */
    @PostMapping("/entry")
    public ResponseEntity<Response<ParkingSessionDto>> vehicleEntry(@Valid @RequestBody VehicleEntryRequestDto entryRequestDto) {
        ParkingSessionDto parkingSessionDto = parkingService.enterVehicle(entryRequestDto);
        Response<ParkingSessionDto> response = new Response<>(LocalDateTime.now(), parkingSessionDto, "Parking session initiated for this vehicle.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Completes a parking session when a vehicle exits.
     *
     * @param vehicleDto DTO with the vehicle's registration number.
     * @return The completed parking session with charge details.
     */
    @PostMapping("/exit")
    public ResponseEntity<Response<ParkingSessionDto>> vehicleExit(@Valid @RequestBody VehicleDto vehicleDto) {
        ParkingSessionDto parkingSessionDto = parkingService.exitVehicle(vehicleDto.getVehicleNumber());
        Response<ParkingSessionDto> response = new Response<>(LocalDateTime.now(), parkingSessionDto, "Parking session completed.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
