package com.fincons.parkingsystem.controller;
import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.service.ParkingSlotService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for retrieving and managing parking slots.
 */
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    /**
     * Handles the HTTP GET request to retrieve the status of all parking slots for a given parking lot.
     * This endpoint is used to get a real-time view of slot availability (e.g., AVAILABLE, OCCUPIED, RESERVED).
     *
     * @param parkingLotId The unique identifier of the parking lot to be checked.
     * @return A ResponseEntity containing a DTO with a list of all slots and a count of available ones.
     */
    @GetMapping("/{parkingLotId}/slots")
    public ResponseEntity<Response<ParkingSlotAvailability>> getSlotsByParkingLot(@PathVariable Long parkingLotId) {
        ParkingSlotAvailability getSlots = parkingSlotService.getParkingSlotAvailability(parkingLotId);
        Response<ParkingSlotAvailability> response = new Response<>(LocalDateTime.now(), getSlots, "Slots fetched successfully", true, 200);
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
    public ResponseEntity<Object> updateParkingSlot(@RequestBody ParkingSlotDto parkingSlotDto)
    {
        ParkingSlotDto parkingSlotDto1=parkingSlotService.updateParkingSlotInformation(parkingSlotDto);
        Response<ParkingSlotDto> response=new Response<>(LocalDateTime.now(),parkingSlotDto1,"Parking Slot updated successfully",true,200);
        return ResponseEntity.ok(response);
    }
}
