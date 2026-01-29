package com.fincons.parkingsystem.controller;
import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.service.ParkingSlotService;
import com.fincons.parkingsystem.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for retrieving information about parking slots.
 */
@RestController
@RequestMapping("/api/parking-lots")

public class ParkingSlotController {

    ParkingSlotService parkingSlotService;

    @Autowired
    ParkingSlotController(ParkingSlotService parkingSlotService)
    {
        this.parkingSlotService=parkingSlotService;
    }
    /**
     * Retrieves all parking slots for a specific parking lot.
     *
     * @param parkingLotId The ID of the parking lot.
     * @return A ResponseEntity containing a list of parking slots for the given lot.
     *         Returns 200 OK on success.
     *         Returns 404 Not Found if the parking lot does not exist.
     */
    @GetMapping("/{parkingLotId}/slots")
    public ResponseEntity<Object> getSlotsByParkingLot(
            @PathVariable Long parkingLotId) {

        ParkingSlotAvailability getSlots=parkingSlotService.GetParkingSlot(parkingLotId);
        Response<ParkingSlotAvailability> getSlotsResponse=new Response<>(LocalDateTime.now(),getSlots,"Slots fetched successfully",true,200);
        return ResponseEntity.ok(getSlotsResponse);
    }
}
