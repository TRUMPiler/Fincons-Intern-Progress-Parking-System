package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;

import java.util.List;

/**
 * This is the contract for my reservation service.
 * It defines all the operations for creating and managing reservations.
 */
public interface ReservationService {

    /**
     * This method creates a new reservation for a parking spot.
     *
     * @param reservationRequestDto The DTO with the details for the new reservation.
     * @return The DTO of the reservation that was just created.
     */
    ReservationDto createReservation(ReservationRequestDto reservationRequestDto);

    /**
     * This method cancels an active reservation.
     *
     * @param reservationId The ID of the reservation to cancel.
     */
    void cancelReservation(Long reservationId);

    /**
     * This method gets the status of all reservations.
     *
     * @return A list of DTOs for all reservations.
     */
    List<ReservationDto> getReservationStatus();

    /**
     * This method processes the arrival of a vehicle with a reservation.
     * It should convert the reservation into an active parking session.
     *
     * @param reservationId The ID of the reservation to process.
     */
    void processArrival(Long reservationId);
}
