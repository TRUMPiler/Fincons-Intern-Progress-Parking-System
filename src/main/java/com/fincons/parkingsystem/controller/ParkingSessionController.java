package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingSessionDto;

import com.fincons.parkingsystem.service.ParkingSessionService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for retrieving information about parking sessions.
 * This controller provides endpoints for viewing active and historical session data.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ParkingSessionController {

    private final ParkingSessionService parkingSessionService;

    /**
     * Handles the HTTP GET request to retrieve a list of all currently active parking sessions.
     * This is useful for real-time monitoring of parked vehicles.
     *
     * @return A ResponseEntity containing a list of active ParkingSessionDto objects.
     */
    @GetMapping("/active")
    public ResponseEntity<Response<List<ParkingSessionDto>>> getActiveSessions() {
        List<ParkingSessionDto> activeSessions = parkingSessionService.getActiveSessions();
        Response<List<ParkingSessionDto>> response = new Response<>(LocalDateTime.now(), activeSessions, "Successfully retrieved all active sessions.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP GET request to retrieve a history of all completed parking sessions.
     * This can be used for auditing, reporting, and analyzing past activity.
     *
     * @return A ResponseEntity containing a list of completed ParkingSessionDto objects.
     */
    @GetMapping("/history")
    public ResponseEntity<Response<List<ParkingSessionDto>>> getSessionHistory() {
        List<ParkingSessionDto> sessionHistory = parkingSessionService.getSessionHistory();
        Response<List<ParkingSessionDto>> response = new Response<>(LocalDateTime.now(), sessionHistory, "Successfully retrieved session history.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
