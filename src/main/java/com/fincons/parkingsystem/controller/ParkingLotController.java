package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.entity.ParkingLot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    @PostMapping
    public ResponseEntity<ParkingLot> createParkingLot(
            @RequestBody ParkingLot parkingLot) {

        // service call will be added later
        return ResponseEntity.ok(parkingLot);
    }

    @GetMapping
    public ResponseEntity<List<ParkingLot>> getAllParkingLots() {

        // service call will be added later
        return ResponseEntity.ok(List.of());
    }
}
