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
 * Repository for ParkingSlot entities.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    /**
     * Counts the number of slots in a lot with a specific status.
     */
    Long countByParkingLotAndStatus(ParkingLot parkingLot, SlotStatus slotStatus);

    /**
     * Finds all slots in a specific parking lot.
     */
    List<ParkingSlot> findByParkingLot(ParkingLot parkingLot);

    /**
     * Finds the first available slot in a lot, ordered by slot number.
     * Uses a pessimistic write lock to prevent race conditions during slot assignment.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkingSlot> findFirstByParkingLotAndStatusOrderBySlotNumberAsc(ParkingLot parkingLot, SlotStatus slotStatus);
}
