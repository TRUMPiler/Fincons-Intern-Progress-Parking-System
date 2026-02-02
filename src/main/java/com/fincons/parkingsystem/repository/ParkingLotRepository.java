package com.fincons.parkingsystem.repository;

import com.fincons.parkingsystem.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * This is the repository for my ParkingLot entities.
 * It's how I interact with the `parking_lots` table in the database.
 */
@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    /**
     * This method finds a parking lot by its name.
     */
    Optional<ParkingLot> findByName(String name);

    /**
     * This method finds all parking lots, including the ones I've deactivated.
     * I'm using a native query here to bypass the soft-delete filter.
     */
    @Query(value = "SELECT * FROM parking_lots", nativeQuery = true)
    List<ParkingLot> findAllWithInactive();

    /**
     * This method finds a parking lot by its ID, including inactive ones.
     * This is useful for getting details of a lot that has been soft-deleted.
     */
    @Query(value = "SELECT * FROM parking_lots WHERE id = :id", nativeQuery = true)
    Optional<ParkingLot> findByIdWithInactive(@Param("id") Long id);
}
