package com.fincons.parkingsystem.repository;


import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link ParkingSlot} entities.
 * Provides standard CRUD operations and custom queries for accessing parking slot data.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {



    /**
     * Counts the number of parking slots in a given parking lot with a specific status.
     *
     * @param parkingLot The parking lot to search in.
     * @param slotStatus The status of the slots to count.
     * @return The number of slots with the given status in the specified parking lot.
     */
    @Lock(LockModeType.OPTIMISTIC)
    Long countByParkingLotAndStatus(ParkingLot parkingLot, SlotStatus slotStatus);

    /**
     * Finds all parking slots in a given parking lot.
     *
     * @param parkingLot The parking lot to search in.
     * @return A list of all parking slots in the specified parking lot.
     */
    @Lock(LockModeType.OPTIMISTIC)
    List<ParkingSlot> findByParkingLot(ParkingLot parkingLot);

    /**
     * Finds the first available parking slot in a given parking lot with a specific status, ordered by slot number.
     * This method uses a pessimistic write lock to prevent race conditions during slot assignment.
     *
     * @param parkingLot The parking lot to search in.
     * @param slotStatus The status of the slot to find.
     * @return An {@link Optional} containing the first available slot, or an empty optional if no slot is found.
     */
    @Lock(LockModeType.OPTIMISTIC)
    Optional<ParkingSlot> findFirstByParkingLotAndStatusOrderBySlotNumberAsc(ParkingLot parkingLot, SlotStatus slotStatus);
}
