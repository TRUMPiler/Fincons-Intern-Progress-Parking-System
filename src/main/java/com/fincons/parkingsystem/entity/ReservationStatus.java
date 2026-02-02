package com.fincons.parkingsystem.entity;

/**
 * An enumeration representing the possible statuses of a parking reservation.
 * This is used to track the lifecycle of a reservation from creation to completion or cancellation.
 */
public enum ReservationStatus {
    /**
     * The reservation is active and waiting for the vehicle to arrive.
     */
    ACTIVE,
    /**
     * The vehicle has arrived, and the reservation has been converted into a parking session.
     */
    COMPLETED,
    /**
     * The reservation has been cancelled by the user.
     */
    CANCELLED,
    /**
     * The reservation has expired because the vehicle did not arrive within the specified time.
     */
    EXPIRED
}
