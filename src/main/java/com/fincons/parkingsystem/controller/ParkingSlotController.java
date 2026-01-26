package com.fincons.parkingsystem.controller;
import com.fincons.parkingsystem.entity.ParkingSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingSlotController {

    @GetMapping("/{parkingLotId}/slots")
    public ResponseEntity<List<ParkingSlot>> getSlotsByParkingLot(
            @PathVariable Long parkingLotId) {

        // service call will be added later
        return ResponseEntity.ok(List.of());
    }
}
