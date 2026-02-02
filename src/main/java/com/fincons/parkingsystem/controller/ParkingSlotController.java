package com.fincons.parkingsystem.controller;
import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.service.ParkingSlotService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for retrieving information about parking slots within a specific lot.
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
}
