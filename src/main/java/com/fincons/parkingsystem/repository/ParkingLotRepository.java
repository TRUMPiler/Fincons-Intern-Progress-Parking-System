package com.fincons.parkingsystem.repository;
import com.fincons.parkingsystem.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link ParkingLot} entities.
 * Provides standard CRUD operations and custom queries for accessing parking lot data.
 */
@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    /**
     * Finds a parking lot by its name.
     *
     * @param name The name of the parking lot to find.
     * @return An {@link Optional} containing the found parking lot, or an empty optional if no parking lot is found.
     */
    Optional<ParkingLot> findByName(String name);

    ParkingLot getReferenceById(Long parkingLotId);
}
