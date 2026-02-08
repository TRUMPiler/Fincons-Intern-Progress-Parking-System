package com.fincons.parkingsystem.repository;


import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ParkingSlot} entities.
 * This interface provides the mechanism for data access and manipulation of the `parking_slots` table.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    /**
     * Counts the number of slots in a given parking lot with a specific status.
     *
     * @param parkingLot The parking lot entity to query against.
     * @param slotStatus The status of the slots to count.
     * @return The total count of matching slots.
     */
    Long countByParkingLotAndStatus(ParkingLot parkingLot, SlotStatus slotStatus);

    /**
     * Retrieves a paginated list of active slots associated with a specific parking lot.
     *
     * @param parkingLot The parking lot entity to find slots for.
     * @param pageable Pagination and sorting information.
     * @return A paginated list of active {@link ParkingSlot} entities for the given lot.
     */
    Page<ParkingSlot> findByParkingLot(ParkingLot parkingLot, Pageable pageable);

    /**
     * Finds the first available slot in a given parking lot, ordered by ID.
     * A pessimistic write lock is applied to prevent race conditions during concurrent slot assignments.
     *
     * @param parkingLot The parking lot to search within.
     * @param slotStatus The desired status of the slot (e.g., AVAILABLE).
     * @return An {@link Optional} containing the first available slot, or empty if none are found.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkingSlot> findFirstByParkingLotAndStatusOrderByIdAsc(ParkingLot parkingLot, SlotStatus slotStatus);

    /**
     * Finds a parking slot by its unique identifier.
     *
     * @param parkingSlotId The ID of the parking slot.
     * @return An {@link Optional} containing the found slot, or empty if not found.
     */
    Optional<ParkingSlot> findParkingSlotById(Long parkingSlotId);

    /**
     * Retrieves all parking slots for a given lot, including those marked as inactive (soft-deleted).
     * This custom query is used to bypass the default @Where clause on the entity.
     *
     * @param parkingLotId The unique identifier of the parking lot.
     * @return A list of all {@link ParkingSlot} entities for the given lot.
     */
    @Query(value = "SELECT * FROM parking_slots WHERE parking_lot_id = :parkingLotId", nativeQuery = true)
    List<ParkingSlot> findAllByParkingLotIdWithInactive(@Param("parkingLotId") Long parkingLotId);

    /**
     * Finds a single parking slot by its ID, including one that may be inactive (soft-deleted).
     * This is essential for administrative functions and for viewing historical data.
     *
     * @param id The unique identifier of the parking slot.
     * @return An {@link Optional} containing the found slot, or empty if not found.
     */
    @Query(value = "SELECT * FROM parking_slots WHERE id = :id", nativeQuery = true)
    Optional<ParkingSlot> findByIdWithInactive(@Param("id") Long id);

    long countByParkingLot(ParkingLot parkingLot);
}
