package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingLotStatsDto;
import com.fincons.parkingsystem.service.ParkingLotStatsService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST controller for retrieving statistical data related to parking lots.
 * This controller provides an endpoint for monitoring the performance and status of a specific lot.
 */
@Slf4j
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotStatsController
{
    private final ParkingLotStatsService parkingLotStatsService;

    /**
     * Handles the HTTP GET request to retrieve statistics for a specific parking lot.
     * This endpoint provides key performance indicators such as total revenue, current occupancy,
     * and daily earnings for the specified lot.
     *
     * @param id The unique identifier of the parking lot for which to retrieve statistics.
     * @return A {@link ResponseEntity} containing a DTO with the parking lot's statistics.
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Response<ParkingLotStatsDto>> getStats(@PathVariable Long id)
    {
        log.info("Received request for statistics of parking lot with ID: {}", id);
        ParkingLotStatsDto parkingLotStatsDto = parkingLotStatsService.getParkingLotStats(id);
        Response<ParkingLotStatsDto> response = new Response<>(LocalDateTime.now(), parkingLotStatsDto, "Parking Lot Details Fetched", true, HttpStatus.OK.value());
        log.info("Successfully retrieved statistics for parking lot with ID: {}", id);
        return ResponseEntity.ok(response);
    }
}
