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
 * This controller serves as the API gateway for the primary parking workflow, delegating
 * business logic to the ParkingService.
 */
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    /**
     * Handles the HTTP POST request to record a vehicle's entry into a parking lot.
     * This endpoint is responsible for initiating a new parking session. It validates the incoming
     * request and delegates the core logic of session creation to the ParkingService.
     *
     * @param entryRequestDto A data transfer object containing the vehicle's registration number,
     *                        type, and the ID of the target parking lot.
     * @return A ResponseEntity wrapping a standardized Response object, which contains the
     *         newly created ParkingSessionDto upon success.
     */
    @PostMapping("/entry")
    public ResponseEntity<Response<ParkingSessionDto>> vehicleEntry(@Valid @RequestBody VehicleEntryRequestDto entryRequestDto) {
        ParkingSessionDto parkingSessionDto = parkingService.enterVehicle(entryRequestDto);
        Response<ParkingSessionDto> response = new Response<>(LocalDateTime.now(), parkingSessionDto, "Parking session initiated for this vehicle.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP POST request to record a vehicle's exit from a parking lot.
     * This endpoint completes an active parking session. It identifies the session based on the
     * vehicle's registration number and delegates charge calculation and session completion
     * to the ParkingService.
     *
     * @param vehicleDto A data transfer object containing the vehicle's registration number.
     * @return A ResponseEntity wrapping a standardized Response object, which contains the
     *         completed ParkingSessionDto, including charge details.
     */
    @PostMapping("/exit")
    public ResponseEntity<Response<ParkingSessionDto>> vehicleExit(@Valid @RequestBody VehicleDto vehicleDto) {
        ParkingSessionDto parkingSessionDto = parkingService.exitVehicle(vehicleDto.getVehicleNumber());
        Response<ParkingSessionDto> response = new Response<>(LocalDateTime.now(), parkingSessionDto, "Parking session completed.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
