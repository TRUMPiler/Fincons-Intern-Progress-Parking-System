package com.fincons.parkingsystem.repository;

import com.fincons.parkingsystem.entity.ParkingLot;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
     * @return An {@link Optional} containing the found parking lot, or empty if not found.
     */
    Optional<ParkingLot> findByName(String name);

    /**
     * Retrieves a paginated list of all active (not soft-deleted) parking lots.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of active {@link ParkingLot} entities.
     */
    Page<ParkingLot> findAll(Pageable pageable);

    /**
     * Retrieves a paginated list of all parking lots from the database, including those marked as inactive.
     * This custom query is used to bypass the default @Where clause on the entity.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of all {@link ParkingLot} entities.
     */
    @Query(value = "SELECT * FROM parking_lots", countQuery = "SELECT count(*) FROM parking_lots", nativeQuery = true)
    Page<ParkingLot> findAllWithInactive(Pageable pageable);

    /**
     * Finds a single parking lot by its ID, including one that may be inactive (soft-deleted).
     * This is essential for administrative functions like reactivation or viewing historical data.
     *
     * @param id The unique identifier of the parking lot.
     * @return An {@link Optional} containing the found parking lot, or empty if not found.
     */
    @Query(value = "SELECT * FROM parking_lots WHERE id = :id", nativeQuery = true)
    Optional<ParkingLot> findByIdWithInactive(@Param("id") Long id);
}
