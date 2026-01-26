package com.fincons.parkingsystem.repository;


import com.fincons.parkingsystem.entity.ParkingSession;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    // Check if vehicle already has an ACTIVE session
    Optional<ParkingSession> findByVehicleVehicleNumberAndStatus(
            String vehicleNumber,
            ParkingSessionStatus status
    );

    // Get all ACTIVE sessions
    List<ParkingSession> findByStatus(ParkingSessionStatus status);

    // Get session history
    List<ParkingSession> findAllByOrderByEntryTimeDesc();
}

