package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.service.ParkingLotService;
import com.fincons.parkingsystem.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

/**
 * REST controller for managing parking lot resources.
 * Provides endpoints for creating, retrieving, deactivating, and reactivating parking lots,
 * wrapping all responses in a standardized {@link Response} object.
 */
@Slf4j
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    /**
     * Handles the HTTP POST request to create a new parking lot.
     * The request body is validated to ensure it meets the defined constraints.
     *
     * @param parkingLotDto A data transfer object containing the details for the new parking lot.
     * @return A {@link ResponseEntity} with a status of 201 (Created) and a {@link Response} object
     *         containing the newly created {@link ParkingLotDto}.
     */
    @PostMapping
    public ResponseEntity<Response<ParkingLotDto>> createParkingLot(@Valid @RequestBody ParkingLotDto parkingLotDto) {
        log.info("Received request to create a new parking lot with name: {}", parkingLotDto.getName());
        ParkingLotDto createdParkingLot = parkingLotService.createParkingLot(parkingLotDto);
        Response<ParkingLotDto> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), createdParkingLot, "Parking Lot created successfully.", true, HttpStatus.CREATED.value());
        log.info("Successfully created parking lot with ID: {}", createdParkingLot.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Handles the HTTP GET request to retrieve a paginated list of all parking lots, including deactivated (soft-deleted) ones.
     * This endpoint is intended for administrative purposes, allowing for the management of all records.
     *
     * @param pageable Pagination and sorting information provided by Spring Web.
     * @return A {@link ResponseEntity} containing a paginated list of all {@link ParkingLotDto} objects.
     */
    @GetMapping("/with-inactive")
    public ResponseEntity<Response<Page<ParkingLotDto>>> getAllParkingLotsWithInactiveStatus(Pageable pageable) {
        log.info("Received request to retrieve all parking lots, including inactive ones.");
        Page<ParkingLotDto> parkingLotDtos = parkingLotService.getAllParkingLotsDeleted(pageable);
        Response<Page<ParkingLotDto>> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), parkingLotDtos, "All parking lots, including inactive, fetched successfully.", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Handles the HTTP GET request to retrieve a non-paginated list of all active (non-deleted) parking lots.
     * This is useful for populating UI elements like dropdowns where all options are needed at once.
     *
     * @return A {@link ResponseEntity} containing a list of all active {@link ParkingLotDto} objects.
     */
    @GetMapping("/all")
    public ResponseEntity<Response<List<ParkingLotDto>>> getAllParkingLots() {
        log.info("Received request to retrieve all active parking lots (non-paginated).");
        List<ParkingLotDto> parkingLots = parkingLotService.findAllParkingLotsNonDeleted();
        Response<List<ParkingLotDto>> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), parkingLots, "All active parking lots fetched successfully.", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Handles the HTTP GET request to retrieve a paginated list of all active parking lots.
     * This endpoint is intended for general user consumption, showing only available lots.
     *
     * @param pageable Pagination and sorting information provided by Spring Web.
     * @return A {@link ResponseEntity} containing a paginated list of active {@link ParkingLotDto} objects.
     */
    @GetMapping
    public ResponseEntity<Response<Page<ParkingLotDto>>> getAllActiveParkingLots(Pageable pageable) {
        log.info("Received request to retrieve active parking lots with pagination.");
        Page<ParkingLotDto> parkingLots = parkingLotService.getAllParkingLots(pageable);
        Response<Page<ParkingLotDto>> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), parkingLots, "Successfully retrieved active parking lots.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP DELETE request to deactivate a parking lot via a soft-delete mechanism.
     *
     * @param id The unique identifier of the parking lot to be deactivated.
     * @return A {@link ResponseEntity} indicating the outcome of the operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteParkingLot(@PathVariable Long id) {
        log.info("Received request to deactivate parking lot with ID: {}", id);
        parkingLotService.deleteParkingLot(id);
        Response<Void> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, "Parking Lot deactivated successfully.", true, HttpStatus.OK.value());
        log.info("Successfully deactivated parking lot with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP PATCH request to reactivate a previously soft-deleted parking lot.
     *
     * @param id The unique identifier of the parking lot to be reactivated.
     * @return A {@link ResponseEntity} indicating the outcome of the operation.
     */
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Response<String>> reactivateParkingLot(@PathVariable Long id) {
        log.info("Received request to reactivate parking lot with ID: {}", id);
        parkingLotService.reactivateParkingLot(id);
        Response<String> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, "Parking Lot reactivated successfully.", true, HttpStatus.OK.value());
        log.info("Successfully reactivated parking lot with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Placeholder for a future implementation of updating parking lot details.
     *
     * @param id The unique identifier of the parking lot to update.
     * @param parkingLotDto A DTO containing the new information for the parking lot.
     * @return A ResponseEntity.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateParkingLot(@PathVariable Long id, @RequestBody ParkingLotDto parkingLotDto) {
        log.warn("Received request to update parking lot with ID: {}. This endpoint is not yet implemented.", id);
        // Implementation for updating parking lot details can be added here.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
