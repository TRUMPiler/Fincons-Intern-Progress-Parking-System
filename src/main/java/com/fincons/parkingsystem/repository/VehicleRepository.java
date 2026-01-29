package com.fincons.parkingsystem.repository;
import com.fincons.parkingsystem.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Vehicle entities.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Finds a vehicle by its registration number.
     */
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}
