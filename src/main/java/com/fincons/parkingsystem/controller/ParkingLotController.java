package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.service.ParkingLotService;
import com.fincons.parkingsystem.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles REST requests for managing parking lots.
 */
@Slf4j
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    /**
     * Creates a new parking lot.
     *
     * @param parkingLotDto DTO with the new parking lot's details.
     * @return The newly created parking lot.
     */
    @PostMapping
    public ResponseEntity<Response<ParkingLotDto>> createParkingLot(@Valid @RequestBody ParkingLotDto parkingLotDto) {
        log.info("Creating a new parking lot");
        ParkingLotDto createdParkingLot = parkingLotService.createParkingLot(parkingLotDto);
        Response<ParkingLotDto> response = new Response<>(LocalDateTime.now(), createdParkingLot, "Parking Lot created successfully.", true, HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of all parking lots.
     *
     * @return A list of all parking lots.
     */
    @GetMapping
    public ResponseEntity<Response<List<ParkingLotDto>>> getAllParkingLots() {

        log.info("Retrieving all parking lots");
        List<ParkingLotDto> parkingLots = parkingLotService.getAllParkingLots();
        Response<List<ParkingLotDto>> response = new Response<>(LocalDateTime.now(), parkingLots, "Successfully retrieved all parking lots.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
