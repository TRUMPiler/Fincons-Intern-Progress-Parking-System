package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.dto.VehicleDto;
import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;
import com.fincons.parkingsystem.service.ParkingService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for handling core parking operations, such as vehicle entry and exit.
 */
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    /**
     * Handles the entry of a vehicle into a specified parking lot.
     *
     * @param entryRequestDto DTO containing the vehicle number, type, and parking lot ID.
     * @return A ResponseEntity containing the details of the newly created active parking session.
     *         Returns 200 OK on success.
     *         Returns 404 Not Found if the parking lot does not exist.
     *         Returns 409 Conflict if the vehicle is already parked or the lot is full.
     */
    @PostMapping("/entry")
    public ResponseEntity<Response<ParkingSessionDto>> vehicleEntry(@RequestBody VehicleEntryRequestDto entryRequestDto) {
        ParkingSessionDto parkingSessionDto = parkingService.enterVehicle(entryRequestDto);
        Response<ParkingSessionDto> response = new Response<>(LocalDateTime.now(), parkingSessionDto, "Parking session initiated for this vehicle.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the exit of a vehicle from the parking system.
     *
     * @param vehicleDto DTO containing the vehicle number of the exiting vehicle.
     * @return A ResponseEntity containing the details of the completed parking session, including the total amount.
     *         Returns 200 OK on success.
     *         Returns 404 Not Found if the vehicle or its active session does not exist.
     */
    @PostMapping("/exit")
    public ResponseEntity<Response<ParkingSessionDto>> vehicleExit(@RequestBody VehicleDto vehicleDto) {
        ParkingSessionDto parkingSessionDto = parkingService.exitVehicle(vehicleDto.getVehicleNumber());
        Response<ParkingSessionDto> response = new Response<>(LocalDateTime.now(), parkingSessionDto, "Parking session completed.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
