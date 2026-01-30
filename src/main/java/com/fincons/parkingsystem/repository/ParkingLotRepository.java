package com.fincons.parkingsystem.repository;
import com.fincons.parkingsystem.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ParkingLot entities.
 */
@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    /**
     * Finds a parking lot by its name.
     */
    Optional<ParkingLot> findByName(String name);

    /**
     * Gets a reference to a parking lot by its ID.
     */
    @NonNull
    ParkingLot getReferenceById(Long parkingLotId);


}
