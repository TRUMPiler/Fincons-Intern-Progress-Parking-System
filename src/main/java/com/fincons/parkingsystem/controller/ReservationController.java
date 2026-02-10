package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;
import com.fincons.parkingsystem.service.ReservationService;
import com.fincons.parkingsystem.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing parking reservations.
 * This controller provides endpoints for creating, canceling, and managing the lifecycle of reservations,
 * such as processing a vehicle's arrival.
 */
@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Handles the HTTP POST request to create a new parking reservation.
     * The request body is validated to ensure all required fields are present.
     *
     * @param reservationRequestDto A DTO containing the details for the new reservation.
     * @return A {@link ResponseEntity} with a status of 201 (Created) and a {@link Response} object
     *         containing the newly created {@link ReservationDto}.
     */
    @PostMapping
    public ResponseEntity<Response<ReservationDto>> createReservation(@Valid @RequestBody ReservationRequestDto reservationRequestDto) {
        log.info("Received request to create a reservation for vehicle: {}", reservationRequestDto.getVehicleNumber());
        ReservationDto createdReservation = reservationService.createReservation(reservationRequestDto);
        Response<ReservationDto> response = new Response<>(LocalDateTime.now(), createdReservation, "Reservation created successfully.", true, HttpStatus.CREATED.value());
        log.info("Successfully created reservation with ID: {}", createdReservation.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Handles the HTTP DELETE request to cancel an active reservation.
     *
     * @param reservationId The unique identifier of the reservation to be canceled.
     * @return A {@link ResponseEntity} indicating the outcome of the operation.
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Response<Void>> cancelReservation(@PathVariable Long reservationId) {
        log.info("Received request to cancel reservation with ID: {}", reservationId);
        reservationService.cancelReservation(reservationId);
        Response<Void> response = new Response<>(LocalDateTime.now(), null, "Reservation cancelled successfully.", true, HttpStatus.OK.value());
        log.info("Successfully cancelled reservation with ID: {}", reservationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP GET request to retrieve a paginated list of all reservations.
     *
     * @param pageable Pagination and sorting information.
     * @return A {@link ResponseEntity} containing a paginated list of all {@link ReservationDto} objects.
     */
    @GetMapping
    public ResponseEntity<Response<Page<ReservationDto>>> getReservationStatus(Pageable pageable) {
        log.info("Received request to retrieve reservation statuses with pagination.");
        Page<ReservationDto> reservationDtoPage = reservationService.getReservationStatus(pageable);
        Response<Page<ReservationDto>> response = new Response<>(LocalDateTime.now(), reservationDtoPage, "Reservations retrieved successfully.", true, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the HTTP POST request to process the arrival of a vehicle with a reservation.
     * This endpoint transitions a reservation to an active parking session.
     *
     * @param reservationId The unique identifier of the reservation to be processed.
     * @return A {@link ResponseEntity} indicating the outcome of the operation.
     */
    @PostMapping("/{reservationId}/arrival")
    public ResponseEntity<Response<Void>> processArrival(@PathVariable Long reservationId) {
        log.info("Received request to process arrival for reservation with ID: {}", reservationId);
        reservationService.processArrival(reservationId);
        Response<Void> response = new Response<>(LocalDateTime.now(), null, "Vehicle arrival processed successfully.", true, HttpStatus.OK.value());
        log.info("Successfully processed arrival for reservation with ID: {}", reservationId);
        return ResponseEntity.ok(response);
    }
}
