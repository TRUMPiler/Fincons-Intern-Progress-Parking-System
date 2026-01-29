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
 * Handles REST requests for parking session information.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ParkingSessionController {

    private final ParkingSessionService parkingSessionService;

    /**
     * Retrieves a list of all currently active parking sessions.
     *
     * @return A list of active parking sessions.
     */
    @GetMapping("/active")
    public ResponseEntity<Response<List<ParkingSessionDto>>> getActiveSessions() {
        List<ParkingSessionDto> activeSessions = parkingSessionService.getActiveSessions();
        Response<List<ParkingSessionDto>> response = new Response<>(LocalDateTime.now(), activeSessions, "Successfully retrieved all active sessions.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a list of all completed parking sessions.
     *
     * @return A list of completed parking sessions.
     */
    @GetMapping("/history")
    public ResponseEntity<Response<List<ParkingSessionDto>>> getSessionHistory() {
        List<ParkingSessionDto> sessionHistory = parkingSessionService.getSessionHistory();
        Response<List<ParkingSessionDto>> response = new Response<>(LocalDateTime.now(), sessionHistory, "Successfully retrieved session history.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
