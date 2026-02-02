package com.fincons.parkingsystem.repository;

import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.Reservation;
import com.fincons.parkingsystem.entity.ReservationStatus;
import com.fincons.parkingsystem.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Reservation} entities.
 * This interface provides the mechanism for data access and manipulation of the `reservations` table.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Checks if a reservation exists for a given vehicle with a specific status.
     * This is used to prevent a vehicle from having multiple active reservations.
     *
     * @param vehicle The vehicle entity to check.
     * @param status The status of the reservation to check for.
     * @return {@code true} if a matching reservation exists, {@code false} otherwise.
     */
    boolean existsByVehicleAndStatus(Vehicle vehicle, ReservationStatus status);

    /**
     * Finds a reservation for a specific vehicle and parking lot with a given status.
     *
     * @param vehicle The vehicle entity to find the reservation for.
     * @param parkingLotId The ID of the parking lot.
     * @param status The status of the reservation to find.
     * @return An {@link Optional} containing the found reservation, or empty if not found.
     */
    Optional<Reservation> findByVehicleAndParkingLotIdAndStatus(Vehicle vehicle, Long parkingLotId, ReservationStatus status);

    /**
     * Checks if a parking lot has any reservations with a specific status.
     * This is used to prevent the deactivation of a parking lot with active reservations.
     *
     * @param parkingLot The parking lot entity to check.
     * @param reservationStatus The status of the reservations to check for.
     * @return {@code true} if a matching reservation exists, {@code false} otherwise.
     */
    boolean existsByParkingLotAndStatus(ParkingLot parkingLot, ReservationStatus reservationStatus);
}
