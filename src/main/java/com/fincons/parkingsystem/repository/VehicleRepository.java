package com.fincons.parkingsystem.repository;
import com.fincons.parkingsystem.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Vehicle} entities.
 * Provides standard CRUD operations and custom queries for accessing vehicle data.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Finds a vehicle by its registration number.
     *
     * @param vehicleNumber The registration number of the vehicle to find.
     * @return An {@link Optional} containing the found vehicle, or an empty optional if no vehicle is found.
     */
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}
