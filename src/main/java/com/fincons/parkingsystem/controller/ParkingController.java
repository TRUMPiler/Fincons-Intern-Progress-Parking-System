package com.fincons.parkingsystem.controller;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    @PostMapping("/entry")
    public ResponseEntity<Map<String, String>> vehicleEntry(
            @RequestBody Map<String, String> request) {

        // service call will be added later
        return ResponseEntity.ok(
                Map.of("message", "Vehicle entry endpoint")
        );
    }

    @PostMapping("/exit")
    public ResponseEntity<Map<String, String>> vehicleExit(
            @RequestBody Map<String, String> request) {

        // service call will be added later
        return ResponseEntity.ok(
                Map.of("message", "Vehicle exit endpoint")
        );
    }
}
