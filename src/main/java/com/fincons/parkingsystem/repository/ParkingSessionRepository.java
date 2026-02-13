package com.fincons.parkingsystem.repository;

import com.fincons.parkingsystem.entity.ParkingSession;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.entity.Vehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ParkingSession} entities.
 * This interface provides the mechanism for data access and manipulation of the `parking_sessions` table.
 */
@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    /**
     * Checks if a session exists for a given vehicle with a specific status.
     * This is primarily used to determine if a vehicle is already actively parked.
     *
     * @param vehicle The vehicle entity to check.
     * @param parkingSessionStatus The status of the session to check for.
     * @return {@code true} if a matching session exists, {@code false} otherwise.
     */
    boolean existsByVehicleAndStatus(Vehicle vehicle, ParkingSessionStatus parkingSessionStatus);

    /**
     * Finds a session for a given vehicle with a specific status.
     * A pessimistic write lock is applied to prevent race conditions during concurrent exit operations.
     *
     * @param vehicle The vehicle entity to find the session for.
     * @param parkingSessionStatus The status of the session to find.
     * @return An {@link Optional} containing the found session, or empty if not found.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkingSession> findByVehicleAndStatus(Vehicle vehicle, ParkingSessionStatus parkingSessionStatus);

    /**
     * Retrieves a paginated list of sessions with a specific status.
     *
     * @param status The status to filter the sessions by.
     * @param pageable Pagination and sorting information.
     * @return A paginated list of {@link ParkingSession} entities.
     */
    Page<ParkingSession> findByStatus(ParkingSessionStatus status, Pageable pageable);

    /**
     * Calculates the sum of the `totalAmount` for all completed sessions associated with a specific parking lot.
     *
     * @param parkingLotId The unique identifier of the parking lot.
     * @return The total revenue as a {@link Double}.
     */
    @Query(value = "SELECT SUM(total_amount) FROM parking_sessions WHERE status = 'COMPLETED' AND parking_slot_id IN (SELECT id FROM parking_slots WHERE parking_lot_id = :parkingLotId)", nativeQuery = true)
    Double sumOfTotalAmountByParkingLot(@Param("parkingLotId") Long parkingLotId);

    /**
     * Calculates the sum of the `totalAmount` for a specific parking lot within a given time range.
     * This is used to calculate daily revenue.
     *
     * @param parkingLotId The unique identifier of the parking lot.
     * @param startDateNow The start of the time range.
     * @param endDateNow The end of the time range.
     * @return The total revenue for the period as a {@link Double}.
     */
    @Query(value = "SELECT SUM(total_amount) FROM parking_sessions WHERE status = 'COMPLETED' AND parking_slot_id IN (SELECT id FROM parking_slots WHERE parking_lot_id = :parkingLotId) " +
            "AND exit_time BETWEEN :startDateNow AND :endDateNow", nativeQuery = true)
    Double sumOfTotalAmountByParkingLotAndExitTime(@Param("parkingLotId") Long parkingLotId, Instant startDateNow, Instant endDateNow);

    Page<ParkingSession> findAll(Pageable pageable);
}
