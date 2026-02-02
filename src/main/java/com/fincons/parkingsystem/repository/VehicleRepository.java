package com.fincons.parkingsystem.repository;
import com.fincons.parkingsystem.entity.Vehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Vehicle} entities.
 * This interface provides the mechanism for data access and manipulation of the `vehicles` table.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Finds a vehicle by its unique registration number.
     * A pessimistic write lock is applied to prevent race conditions during concurrent vehicle creation.
     *
     * @param vehicleNumber The registration number of the vehicle.
     * @return An {@link Optional<Vehicle>} containing the found vehicle, or empty if not found.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}
