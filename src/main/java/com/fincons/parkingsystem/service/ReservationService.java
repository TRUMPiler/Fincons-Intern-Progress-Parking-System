package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing parking reservations.
 * This contract defines the business logic for creating, canceling, and managing the lifecycle of reservations.
 */
public interface ReservationService {

    /**
     * Creates a new reservation for a parking spot.
     *
     * @param reservationRequestDto A DTO containing the details for the new reservation.
     * @return The DTO of the newly created reservation.
     */
    ReservationDto createReservation(ReservationRequestDto reservationRequestDto);

    /**
     * Cancels an active reservation.
     *
     * @param reservationId The unique identifier of the reservation to be canceled.
     */
    void cancelReservation(Long reservationId);

    /**
     * Retrieves a list of all reservations.
     *
     * @return A list of DTOs representing all reservations.
     */
    Page<ReservationDto> getReservationStatus(Pageable pageable);

    /**
     * Processes the arrival of a vehicle with a reservation, converting the reservation
     * into an active parking session.
     *
     * @param reservationId The unique identifier of the reservation to be processed.
     */
    void processArrival(Long reservationId);
}
