package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.entity.ParkingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ParkingSessionController {

    @GetMapping("/active")
    public ResponseEntity<List<ParkingSession>> getActiveSessions() {

        // service call will be added later
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/history")
    public ResponseEntity<List<ParkingSession>> getSessionHistory() {

        // service call will be added later
        return ResponseEntity.ok(List.of());
    }
}

