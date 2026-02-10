package com.fincons.parkingsystem.controller;
import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.service.ParkingSlotService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for retrieving and managing parking slots.
 * This controller provides endpoints to view the status of slots within a lot and to update individual slots.
 */
@Slf4j
@RestController
@RequestMapping("/api/parking-slots")
@RequiredArgsConstructor
public class    ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    /**
     * Handles the HTTP GET request to retrieve a paginated list of parking slots for a given parking lot.
     * This allows clients to see the status (e.g., AVAILABLE, OCCUPIED) of all slots in a specific lot.
     *
     * @param parkingLotId The unique identifier of the parking lot to be checked.
     * @param pageable Pagination and sorting information provided by Spring Web.
     * @return A {@link ResponseEntity} containing a paginated list of {@link ParkingSlotDto} objects.
     */
    @GetMapping("/by-lot/{parkingLotId}")
    public ResponseEntity<Response<Page<ParkingSlotDto>>> getSlotsByParkingLot(@PathVariable Long parkingLotId, Pageable pageable) {
        log.info("Received request to get slots for parking lot ID: {}", parkingLotId);
        Page<ParkingSlotDto> slots = parkingSlotService.getParkingSlotAvailability(parkingLotId, pageable);
        Response<Page<ParkingSlotDto>> response = new Response<>(LocalDateTime.now(), slots, "Slots fetched successfully", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP PATCH request to update the details of a specific parking slot.
     * This can be used for administrative purposes, such as manually changing a slot's status
     * (e.g., to 'UNDER_SERVICE').
     *
     * @param parkingSlotDto A DTO containing the ID of the slot to update and the new information.
     * @return A {@link ResponseEntity} containing the updated {@link ParkingSlotDto}.
     */
    @PatchMapping("/update-slot")
    public ResponseEntity<Response<ParkingSlotDto>> updateParkingSlot(@RequestBody ParkingSlotDto parkingSlotDto)
    {
        log.info("Received request to update parking slot with ID: {}", parkingSlotDto.getId());
        ParkingSlotDto updatedSlot = parkingSlotService.updateParkingSlotInformation(parkingSlotDto);
        Response<ParkingSlotDto> response = new Response<>(LocalDateTime.now(), updatedSlot, "Parking Slot updated successfully", true, HttpStatus.OK.value());
        log.info("Successfully updated parking slot with ID: {}", updatedSlot.getId());
        return ResponseEntity.ok(response);
    }
}
