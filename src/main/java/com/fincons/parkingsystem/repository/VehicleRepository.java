package com.fincons.parkingsystem.repository;
import com.fincons.parkingsystem.entity.Vehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * This is the repository for my Vehicle entities.
 * It's how I interact with the `vehicles` table in the database.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * This method finds a vehicle by its registration number.
     * I've added a pessimistic lock to prevent issues if two requests try to create the same vehicle at once.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}
