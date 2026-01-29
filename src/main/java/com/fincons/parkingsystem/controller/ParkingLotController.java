package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.service.ParkingLotService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing parking lots.
 */
@Slf4j
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    /**
     * Creates a new parking lot along with its initial set of parking slots.
     *
     * @param parkingLotDto DTO containing the details of the new parking lot.
     * @return A ResponseEntity containing the details of the created parking lot.
     *         Returns 201 Created on success.
     *         Returns 409 Conflict if a parking lot with the same name already exists.
     */
    @PostMapping
    public ResponseEntity<Response<ParkingLotDto>> createParkingLot(@RequestBody ParkingLotDto parkingLotDto) {
        log.info("Creating a new parking lot");
        ParkingLotDto createdParkingLot = parkingLotService.createParkingLot(parkingLotDto);
        Response<ParkingLotDto> response = new Response<>(LocalDateTime.now(), createdParkingLot, "Parking Lot created successfully.", true, HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of all existing parking lots.
     *
     * @return A ResponseEntity containing a list of all parking lots.
     *         Returns 200 OK on success.
     */
    @GetMapping
    public ResponseEntity<Response<List<ParkingLotDto>>> getAllParkingLots() {

        log.info("Retrieving all parking lots");
        List<ParkingLotDto> parkingLots = parkingLotService.getAllParkingLots();
        Response<List<ParkingLotDto>> response = new Response<>(LocalDateTime.now(), parkingLots, "Successfully retrieved all parking lots.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
