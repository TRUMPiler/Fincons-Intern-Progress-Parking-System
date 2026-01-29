package com.fincons.parkingsystem.repository;

import com.fincons.parkingsystem.entity.ParkingSession;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ParkingSession entities.
 */
@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    /**
     * Checks if a vehicle has a session with a specific status.
     */
    boolean existsByVehicleAndStatus(Vehicle vehicle, ParkingSessionStatus parkingSessionStatus);

    /**
     * Finds a session for a vehicle with a specific status.
     */
    Optional<ParkingSession> findByVehicleAndStatus(Vehicle vehicle, ParkingSessionStatus parkingSessionStatus);

    /**
     * Finds all sessions with a specific status.
     */
    List<ParkingSession> findByStatus(ParkingSessionStatus status);

    /**
     * Calculates the total revenue from completed sessions for a specific parking lot.
     */
    @Query(value = "SELECT SUM(total_amount) FROM parking_sessions WHERE status = 'COMPLETED' AND parking_slot_id IN (SELECT id FROM parking_slots WHERE parking_lot_id = :parkingLotId)", nativeQuery = true)
    Double sumOfTotalAmountByParkingLot(@Param("parkingLotId") Long parkingLotId);

    /**
     * Counts the number of sessions for a slot with a specific status.
     */
    Long countParkingSessionByParkingSlotAndStatus(ParkingSlot slot, ParkingSessionStatus status);
}
