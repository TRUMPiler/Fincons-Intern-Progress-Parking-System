package com.fincons.parkingsystem.repository;

import com.fincons.parkingsystem.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ParkingLot} entities.
 * This interface provides the mechanism for data access and manipulation of the `parking_lots` table.
 */
@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    /**
     * Finds a parking lot by its unique name.
     *
     * @param name The name of the parking lot.
     * @return An {@link Optional<ParkingLot>} containing the found parking lot, or empty if not found.
     */
    Optional<ParkingLot> findByName(String name);
    /**
     * Retrieves all parking lots from the database, including those marked as inactive (soft-deleted).
     * This custom query is used to bypass the default @Where clause on the entity.
     *
     * @return A list of all {@link List<ParkingLot>} entities.
     */
    @Query(value = "SELECT * FROM parking_lots", nativeQuery = true)
    List<ParkingLot> findAllWithInactive();
    /**s
     * Finds a single parking lot by its ID, including one that may be inactive (soft-deleted).
     * This is essential for administrative functions like reactivation or viewing historical data.
     *
     * @param id The unique identifier of the parking lot.
     * @return An {@link Optional<ParkingLot>} containing the found parking lot, or empty if not found.
     */
    @Query(value = "SELECT * FROM parking_lots WHERE id = :id", nativeQuery = true)
    Optional<ParkingLot> findByIdWithInactive(@Param("id") Long id);
}
