package com.fincons.parkingsystem.repository;

import com.fincons.parkingsystem.entity.ParkingSession;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.Vehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This is the repository for my ParkingSession entities.
 * It's how I interact with the `parking_sessions` table in the database.
 */
@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    /**
     * This method checks if a vehicle already has a session with a specific status.
     * I use it to see if a car is already parked.
     */
    boolean existsByVehicleAndStatus(Vehicle vehicle, ParkingSessionStatus parkingSessionStatus);

    /**
     * This method finds a session for a vehicle with a specific status.
     * I've added a pessimistic lock to prevent issues if two requests try to modify the same session at once.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkingSession> findByVehicleAndStatus(Vehicle vehicle, ParkingSessionStatus parkingSessionStatus);

    /**
     * This method finds all sessions that have a specific status (e.g., all ACTIVE sessions).
     */
    @Query(value = "SELECT p FROM ParkingSession p WHERE p.status =:status", nativeQuery = false)
    List<ParkingSession> findByStatus(@Param("status") ParkingSessionStatus status);

    /**
     * This method calculates the total revenue from completed sessions for a specific parking lot.
     */
    @Query(value = "SELECT SUM(total_amount) FROM parking_sessions WHERE status = 'COMPLETED' AND parking_slot_id IN (SELECT id FROM parking_slots WHERE parking_lot_id = :parkingLotId)", nativeQuery = true)
    Double sumOfTotalAmountByParkingLot(@Param("parkingLotId") Long parkingLotId);


    /**
     * This method calculates the total revenue for a specific parking lot for the current day.
     */
    @Query(value = "SELECT SUM(total_amount) FROM parking_sessions WHERE status = 'COMPLETED' AND parking_slot_id IN (SELECT id FROM parking_slots WHERE parking_lot_id = :parkingLotId) " +
            "AND exit_time BETWEEN :startDateNow AND :endDateNow", nativeQuery = true)
    Double sumOfTotalAmountByParkingLotAndExitTime(@Param("parkingLotId") Long parkingLotId, LocalDateTime startDateNow, LocalDateTime endDateNow);

}
