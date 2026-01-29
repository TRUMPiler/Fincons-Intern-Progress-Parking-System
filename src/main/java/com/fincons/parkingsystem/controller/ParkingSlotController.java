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
 * Handles REST requests for parking slot information.
 */
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    /**
     * Retrieves the availability of parking slots for a specific parking lot.
     *
     * @param parkingLotId The ID of the parking lot.
     * @return A DTO containing the list of slots and the count of available slots.
     */
    @GetMapping("/{parkingLotId}/slots")
    public ResponseEntity<Response<ParkingSlotAvailability>> getSlotsByParkingLot(@PathVariable Long parkingLotId) {
        ParkingSlotAvailability getSlots = parkingSlotService.getParkingSlotAvailability(parkingLotId);
        Response<ParkingSlotAvailability> response = new Response<>(LocalDateTime.now(), getSlots, "Slots fetched successfully", true, 200);
        return ResponseEntity.ok(response);
    }
}
