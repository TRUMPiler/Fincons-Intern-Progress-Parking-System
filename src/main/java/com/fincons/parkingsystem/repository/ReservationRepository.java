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
 * This is the repository for my Reservation entities.
 * It's how I interact with the `reservations` table in the database.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * This method checks if a vehicle already has a reservation with a specific status.
     * I use it to see if a car already has an active reservation.
     */
    boolean existsByVehicleAndStatus(Vehicle vehicle, ReservationStatus status);

    /**
     * This method finds a reservation for a specific vehicle and parking lot with a given status.
     */
    Optional<Reservation> findByVehicleAndParkingLotIdAndStatus(Vehicle vehicle, Long parkingLotId, ReservationStatus status);

    /**
     * This method checks if a parking lot has any reservations with a specific status.
     * I use it to prevent deleting a lot that has active reservations.
     */
    boolean existsByParkingLotAndStatus(ParkingLot parkingLot, ReservationStatus reservationStatus);
}
