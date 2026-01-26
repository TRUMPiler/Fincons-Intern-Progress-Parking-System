package com.fincons.parkingsystem.repository;


import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    // Find first AVAILABLE slot in a parking lot
    Optional<ParkingSlot> findFirstByParkingLotIdAndStatus(
            Long parkingLotId,
            SlotStatus status
    );

    // Get all slots of a parking lot
    List<ParkingSlot> findByParkingLotId(Long parkingLotId);

    // Count slots by status (used for occupancy calculation)
    long countByParkingLotIdAndStatus(
            Long parkingLotId,
            SlotStatus status
    );
}
