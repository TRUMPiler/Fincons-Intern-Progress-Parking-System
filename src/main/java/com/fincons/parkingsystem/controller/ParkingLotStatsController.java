package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingLotStatsDto;
import com.fincons.parkingsystem.service.ParkingLotStatsService;
import com.fincons.parkingsystem.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Handles REST requests for parking lot statistics.
 */
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotStatsController
{
    private final ParkingLotStatsService parkingLotStatsService;

    /**
     * Retrieves statistics for a specific parking lot.
     *
     * @param id The ID of the parking lot.
     * @return Statistics for the parking lot, such as revenue and occupancy.
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Response<ParkingLotStatsDto>> getStats(@PathVariable Long id)
    {
        ParkingLotStatsDto parkingLotStatsDto = parkingLotStatsService.getParkingLotStats(id);
        Response<ParkingLotStatsDto> response = new Response<>(LocalDateTime.now(), parkingLotStatsDto, "Parking Lot Details Fetched", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
