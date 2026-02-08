package com.fincons.parkingsystem.controller;
import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.service.ParkingSlotService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for retrieving and managing parking slots.
 */
@RestController
@RequestMapping("/api/parking-slots")
@RequiredArgsConstructor
public class    ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    /**
     * Handles the HTTP GET request to retrieve a paginated list of parking slots for a given parking lot.
     *
     * @param parkingLotId The unique identifier of the parking lot to be checked.
     * @param pageable Pagination and sorting information.
     * @return A ResponseEntity containing a paginated list of ParkingSlotDto objects.
     */
    @GetMapping("/by-lot/{parkingLotId}")
    public ResponseEntity<Response<Page<ParkingSlotDto>>> getSlotsByParkingLot(@PathVariable Long parkingLotId, Pageable pageable) {
        Page<ParkingSlotDto> slots = parkingSlotService.getParkingSlotAvailability(parkingLotId, pageable);
        Response<Page<ParkingSlotDto>> response = new Response<>(LocalDateTime.now(), slots, "Slots fetched successfully", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP PATCH request to update the details of a specific parking slot.
     * This can be used for administrative purposes, such as manually changing a slot's status.
     *
     * @param parkingSlotDto A DTO containing the updated information for the parking slot.
     * @return A ResponseEntity containing the updated ParkingSlotDto.
     */
    @PatchMapping("/update-slot")
    public ResponseEntity<Response<ParkingSlotDto>> updateParkingSlot(@RequestBody ParkingSlotDto parkingSlotDto)
    {
        ParkingSlotDto updatedSlot = parkingSlotService.updateParkingSlotInformation(parkingSlotDto);
        Response<ParkingSlotDto> response = new Response<>(LocalDateTime.now(), updatedSlot, "Parking Slot updated successfully", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
