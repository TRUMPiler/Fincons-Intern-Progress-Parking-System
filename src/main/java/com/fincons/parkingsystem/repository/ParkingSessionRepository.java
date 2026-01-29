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

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link ParkingSession} entities.
 * Provides standard CRUD operations and custom queries for accessing parking session data.
 */
@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    /**
     * Checks if a parking session exists for a given vehicle with a specific status.
     *
     * @param vehicle The vehicle to check for.
     * @param parkingSessionStatus The status of the parking session to check for.
     * @return {@code true} if a session exists, {@code false} otherwise.
     */
    boolean existsByVehicleAndStatus(Vehicle vehicle, ParkingSessionStatus parkingSessionStatus);

    /**
     * Finds a parking session for a given vehicle with a specific status.
     *
     * @param vehicle The vehicle to find the session for.
     * @param parkingSessionStatus The status of the parking session to find.
     * @return An {@link Optional} containing the found parking session, or an empty optional if no session is found.
     */
    Optional<ParkingSession> findByVehicleAndStatus(Vehicle vehicle, ParkingSessionStatus parkingSessionStatus);

    /**
     * Finds all parking sessions with a specific status.
     *
     * @param status The status of the parking sessions to find.
     * @return A list of all parking sessions with the given status.
     */
    List<ParkingSession> findByStatus(ParkingSessionStatus status);


    @Query(value = "SELECT SUM(total_amount) FROM parking_sessions WHERE status = 'COMPLETED' AND parking_slot_id IN (SELECT id FROM parking_slots WHERE parking_lot_id = :parkingLotId)", nativeQuery = true)
    Double sumOfTotalAmountByParkingLot(@Param("parkingLotId") Long parkingLotId);

    Long countParkingSessionByParkingSlotAndStatus(ParkingSlot slot, ParkingSessionStatus status);
}
