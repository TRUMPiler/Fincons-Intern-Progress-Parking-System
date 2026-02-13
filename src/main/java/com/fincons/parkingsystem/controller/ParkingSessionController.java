package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.service.ParkingSessionService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

/**
 * REST controller for retrieving information about parking sessions.
 * This controller provides endpoints for viewing active and historical session data,
 * with support for pagination.
 */
@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ParkingSessionController {

    private final ParkingSessionService parkingSessionService;


    /**
     * Handles the HTTP GET request to retrieve a paginated list of all currently active parking sessions.
     *
     * @param pageable Pagination and sorting information provided by Spring Web.
     * @return A {@link ResponseEntity} containing a paginated list of active {@link ParkingSessionDto} objects.
     */
    @GetMapping("/active")
    public ResponseEntity<Response<Page<ParkingSessionDto>>> getActiveSessions(Pageable pageable) {
        log.info("Received request to retrieve active parking sessions.");
        Page<ParkingSessionDto> activeSessions = parkingSessionService.getActiveSessions(pageable);
        Response<Page<ParkingSessionDto>> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), activeSessions, "Successfully retrieved all active sessions.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP GET request to retrieve a paginated history of all completed parking sessions.
     *
     * @param pageable Pagination and sorting information provided by Spring Web.
     * @return A {@link ResponseEntity} containing a paginated list of completed {@link ParkingSessionDto} objects.
     */
    @GetMapping("/history")
    public ResponseEntity<Response<Page<ParkingSessionDto>>> getSessionHistory(Pageable pageable) {
        log.info("Received request to retrieve parking session history.");
        Page<ParkingSessionDto> sessionHistory = parkingSessionService.getSessionHistory(pageable);
        Response<Page<ParkingSessionDto>> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), sessionHistory, "Successfully retrieved session history.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
