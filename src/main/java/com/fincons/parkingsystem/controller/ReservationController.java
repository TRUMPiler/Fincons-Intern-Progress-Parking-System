package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;
import com.fincons.parkingsystem.service.ReservationService;
import com.fincons.parkingsystem.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing parking reservations.
 * This controller provides endpoints for creating, canceling, and managing the lifecycle of reservations.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Handles the HTTP POST request to create a new parking reservation.
     *
     * @param reservationRequestDto A DTO containing the details for the new reservation.
     * @return A ResponseEntity containing the newly created ReservationDto.
     */
    @PostMapping
    public ResponseEntity<Response<ReservationDto>> createReservation(@Valid @RequestBody ReservationRequestDto reservationRequestDto) {
        ReservationDto createdReservation = reservationService.createReservation(reservationRequestDto);
        Response<ReservationDto> response = new Response<>(LocalDateTime.now(), createdReservation, "Reservation created successfully.", true, HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Handles the HTTP DELETE request to cancel an active reservation.
     *
     * @param reservationId The unique identifier of the reservation to be canceled.
     * @return A ResponseEntity indicating the outcome of the operation.
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Response<Void>> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        Response<Void> response = new Response<>(LocalDateTime.now(), null, "Reservation cancelled successfully.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP GET request to retrieve a list of all reservations.
     *
     * @return A ResponseEntity containing a list of all ReservationDto objects.
     */
    @GetMapping
    public ResponseEntity<Object> getReservationStatus() {
        List<ReservationDto> reservationDto = reservationService.getReservationStatus();
        Response<List<ReservationDto>> response = new Response<>(LocalDateTime.now(), reservationDto, "Reservations retrieved successfully.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP POST request to process the arrival of a vehicle with a reservation.
     * This endpoint transitions a reservation to an active parking session.
     *
     * @param reservationId The unique identifier of the reservation to be processed.
     * @return A ResponseEntity indicating the outcome of the operation.
     */
    @PostMapping("/{reservationId}/arrival")
    public ResponseEntity<Response<Void>> processArrival(@PathVariable Long reservationId) {
        reservationService.processArrival(reservationId);
        Response<Void> response = new Response<>(LocalDateTime.now(), null, "Vehicle arrival processed successfully.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
