package com.fincons.parkingsystem.repository;


import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * This is the repository for my ParkingSlot entities.
 * It's how I interact with the `parking_slots` table in the database.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    /**
     * This method counts the number of slots in a lot that have a specific status (e.g., AVAILABLE).
     */
    Long countByParkingLotAndStatus(ParkingLot parkingLot, SlotStatus slotStatus);

    /**
     * This method finds all the active slots in a specific parking lot.
     */
    List<ParkingSlot> findByParkingLot(ParkingLot parkingLot);

    /**
     * This method finds the first available slot in a lot.
     * I've added a pessimistic lock to prevent two cars from being assigned the same slot at the same time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkingSlot> findFirstByParkingLotAndStatusOrderByIdAsc(ParkingLot parkingLot, SlotStatus slotStatus);

    /**
     * This method finds a slot by its ID.
     */
    Optional<ParkingSlot> findParkingSlotById(Long parkingSlotId);

    /**
     * This method finds all slots for a parking lot, including the ones I've deactivated.
     * I'm using a native query here to bypass the soft-delete filter.
     */
    @Query(value = "SELECT * FROM parking_slots WHERE parking_lot_id = :parkingLotId", nativeQuery = true)
    List<ParkingSlot> findAllByParkingLotIdWithInactive(@Param("parkingLotId") Long parkingLotId);

    /**
     * This method finds a slot by its ID, including inactive ones.
     * This is useful for getting details of a slot that has been soft-deleted.
     */
    @Query(value = "SELECT * FROM parking_slots WHERE id = :id", nativeQuery = true)
    Optional<ParkingSlot> findByIdWithInactive(@Param("id") Long id);
}
