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
 * REST controller for managing parking lot resources.
 * Provides endpoints for creating, retrieving, deactivating, and reactivating parking lots.
 */
@Slf4j
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    /**
     * Handles the HTTP POST request to create a new parking lot.
     *
     * @param parkingLotDto A DTO containing the details for the new parking lot.
     * @return A ResponseEntity containing the newly created ParkingLotDto.
     */
    @PostMapping
    public ResponseEntity<Response<ParkingLotDto>> createParkingLot(@Valid @RequestBody ParkingLotDto parkingLotDto) {
        log.info("Creating a new parking lot");
        ParkingLotDto createdParkingLot = parkingLotService.createParkingLot(parkingLotDto);
        Response<ParkingLotDto> response = new Response<>(LocalDateTime.now(), createdParkingLot, "Parking Lot created successfully.", true, HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Handles the HTTP GET request to retrieve a list of all parking lots, including deactivated ones.
     * This endpoint is intended for administrative purposes.
     *
     * @return A ResponseEntity containing a list of all ParkingLotDto objects.
     */
    @GetMapping("/with-inactive")
    public ResponseEntity<Response<List<ParkingLotDto>>> getAllParkingLotsWithInactiveStatus() {
        List<ParkingLotDto> parkingLotDtos = parkingLotService.getAllParkingLotsDeleted();
        Response<List<ParkingLotDto>> response = new Response<>(LocalDateTime.now(), parkingLotDtos, "All data fetched", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Handles the HTTP GET request to retrieve a list of all active parking lots.
     * This endpoint is intended for general user consumption.
     *
     * @return A ResponseEntity containing a list of active ParkingLotDto objects.
     */
    @GetMapping
    public ResponseEntity<Response<List<ParkingLotDto>>> getAllParkingLots() {
        log.info("Retrieving all active parking lots");
        List<ParkingLotDto> parkingLots = parkingLotService.getAllParkingLots();
        Response<List<ParkingLotDto>> response = new Response<>(LocalDateTime.now(), parkingLots, "Successfully retrieved all active parking lots.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP DELETE request to deactivate a parking lot (soft delete).
     *
     * @param id The ID of the parking lot to deactivate.
     * @return A ResponseEntity indicating the outcome of the operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteParkingLot(@PathVariable Long id) {
        parkingLotService.deleteParkingLot(id);
        Response<Void> response = new Response<>(LocalDateTime.now(), null, "Parking Lot deactivated successfully.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP PATCH request to reactivate a previously deactivated parking lot.
     *
     * @param id The ID of the parking lot to reactivate.
     * @return A ResponseEntity indicating the outcome of the operation.
     */
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Response<String>> reactivateParkingLot(@PathVariable Long id) {
        parkingLotService.reactivateParkingLot(id);
        Response<String> response = new Response<>(LocalDateTime.now(), null, "Parking Lot reactivated successfully.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Placeholder for a future implementation of updating parking lot details.
     *
     * @param id The ID of the parking lot to update.
     * @param parkingLotDto A DTO containing the new information for the parking lot.
     * @return A ResponseEntity.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateParkingLot(@PathVariable Long id, @RequestBody ParkingLotDto parkingLotDto) {
        // Implementation for updating parking lot details can be added here.
        return null;
    }
}
