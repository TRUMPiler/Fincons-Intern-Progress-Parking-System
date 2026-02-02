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
 * REST controller for handling core parking operations, such as vehicle entry and exit.
 * This controller serves as the API gateway for the primary parking workflow.
 */
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    /**
     * Handles the HTTP POST request for a vehicle entering a parking lot.
     * This endpoint expects a request body containing vehicle and parking lot details,
     * and it initiates a new parking session via the ParkingService.
     *
     * @param entryRequestDto A DTO containing the vehicle's details and the ID of the parking lot.
     * @return A ResponseEntity containing the newly created ParkingSessionDto.
     */
    @PostMapping("/entry")
    public ResponseEntity<Response<ParkingSessionDto>> vehicleEntry(@Valid @RequestBody VehicleEntryRequestDto entryRequestDto) {
        ParkingSessionDto parkingSessionDto = parkingService.enterVehicle(entryRequestDto);
        Response<ParkingSessionDto> response = new Response<>(LocalDateTime.now(), parkingSessionDto, "Parking session initiated for this vehicle.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP POST request for a vehicle exiting a parking lot.
     * This endpoint requires the vehicle's registration number to identify the active session.
     * The service layer is responsible for calculating charges and completing the session.
     *
     * @param vehicleDto A DTO containing the vehicle's registration number.
     * @return A ResponseEntity containing the completed ParkingSessionDto with charge details.
     */
    @PostMapping("/exit")
    public ResponseEntity<Response<ParkingSessionDto>> vehicleExit(@Valid @RequestBody VehicleDto vehicleDto) {
        ParkingSessionDto parkingSessionDto = parkingService.exitVehicle(vehicleDto.getVehicleNumber());
        Response<ParkingSessionDto> response = new Response<>(LocalDateTime.now(), parkingSessionDto, "Parking session completed.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
