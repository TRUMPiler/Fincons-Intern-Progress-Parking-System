package com.fincons.parkingsystem.repository;

import com.fincons.parkingsystem.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * @param parkingSlotId The ID of the parking slot.
     * @param status The status of the reservation to find.
     * @return An {@link Optional} containing the found reservation, or empty if not found.
     */


    @Query(value="SELECT r FROM Reservation r WHERE r.vehicle = :vehicle AND r.parkingSlot.id = :parkingSlotId AND r.status = :status")
    Optional<Reservation> findByVehicleAndParkingSlotIdAndStatus(Vehicle vehicle, Long parkingSlotId, ReservationStatus status);

//    Page<Reservation> findAll(Pa);
    /**
     * Checks if a parking lot has any reservations with a specific status.
     * This is used to prevent the deactivation of a parking lot with active reservations.
     *
     * @param parkingSlot The parking slot entity to check.
     * @param reservationStatus The status of the reservations to check for.
     * @return {@code true} if a matching reservation exists, {@code false} otherwise.
     */
    boolean existsByParkingSlotAndStatus(ParkingSlot parkingSlot, ReservationStatus reservationStatus);

    @Query(
            value = "SELECT r FROM Reservation r",
            countQuery = "SELECT COUNT(r) FROM Reservation r"
    )
    Page<Reservation> findAllByCustom(Pageable pageable);
}
