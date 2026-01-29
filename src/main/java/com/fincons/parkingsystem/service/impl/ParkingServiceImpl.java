package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;
import com.fincons.parkingsystem.entity.*;
import com.fincons.parkingsystem.exception.BadRequestException;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSessionMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSessionRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.repository.VehicleRepository;
import com.fincons.parkingsystem.service.ParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Implementation of the ParkingService interface.
 * This service manages the core parking operations, including vehicle entry and exit. It handles the creation
 * and completion of parking sessions, calculates charges based on dynamic pricing rules, and updates the
 * availability of parking slots. The implementation uses both pessimistic and optimistic locking to ensure
 * data consistency in a concurrent environment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSessionMapper parkingSessionMapper;

    /**
     * A private record to hold the results of a charge calculation.
     * This is a temporary, immutable data carrier used to pass charge details
     * between methods without persisting them.
     */
    private record ChargeCalculationResult(double totalAmount, double basePricePerHour, long hoursCharged,
                                           double occupancyPercentage, double multiplier) {
    }

    /**
     * Handles the entry of a vehicle into a parking lot.
     * It finds an existing vehicle or creates a new one, assigns the lowest-numbered available parking slot,
     * and creates a new active parking session. A pessimistic lock is used during slot assignment to prevent
     * race conditions.
     *
     * @param entryRequest DTO containing vehicle number, type, and parking lot ID.
     * @return a DTO representing the newly created and active parking session.
     * @throws ResourceNotFoundException if the specified parking lot is not found.
     * @throws ConflictException if the vehicle already has an active parking session or if no parking slots are available.
     * @throws IllegalArgumentException if the entry request or its essential fields are null.
     */
    @Override
    @Transactional
    public ParkingSessionDto enterVehicle(VehicleEntryRequestDto entryRequest) {
        Assert.notNull(entryRequest, "Entry request cannot be null.");
        Assert.notNull(entryRequest.getParkingLotId(), "Parking lot ID cannot be null.");
        Assert.hasText(entryRequest.getVehicleNumber(), "Vehicle number cannot be empty.");
        Assert.notNull(entryRequest.getVehicleType(), "Vehicle type cannot be null.");

        log.info("Processing entry for vehicle number: {}", entryRequest.getVehicleNumber());

        ParkingLot parkingLot = parkingLotRepository.findById(entryRequest.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Parking lot not found with id: %d", entryRequest.getParkingLotId())));
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(entryRequest.getVehicleNumber())
                .orElseGet(() -> {
                    log.info("Creating new vehicle for");
                    Vehicle newVehicle = Vehicle.builder()
                            .vehicleNumber(entryRequest.getVehicleNumber())
                            .vehicleType(entryRequest.getVehicleType())
                            .build();
                    return vehicleRepository.save(newVehicle);
                });

        if (parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active parking session.");
        }

        ParkingSlot availableSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderBySlotNumberAsc(parkingLot, SlotStatus.AVAILABLE)
                .orElseThrow(() -> new ConflictException("No available parking slots in this lot."));

        availableSlot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(availableSlot);

        ParkingSession newSession = ParkingSession.builder()
                .vehicle(vehicle)
                .parkingSlot(availableSlot)
                .entryTime(LocalDateTime.now())
                .status(ParkingSessionStatus.ACTIVE)
                .build();
        ParkingSession savedSession = parkingSessionRepository.save(newSession);

        return parkingSessionMapper.toDto(savedSession);
    }

    /**
     * Handles the exit of a vehicle from the parking lot.
     * It completes the active parking session, calculates the total charges, and frees up the parking slot.
     * The final amount is persisted, but all intermediate calculation details (base price, hours, occupancy, multiplier)
     * are returned in the DTO without being stored in the database.
     *
     * @param vehicleNumber the registration number of the exiting vehicle.
     * @return a DTO representing the completed parking session with all charge calculation details.
     * @throws ResourceNotFoundException if the vehicle or its active parking session is not found.
     * @throws BadRequestException if the calculated exit time is before the entry time.
     */
    @Override
    @Transactional
    public ParkingSessionDto exitVehicle(String vehicleNumber) {
        log.info("Processing exit for vehicle number: {}", vehicleNumber);

        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Vehicle not found with number: %s", vehicleNumber)));

        ParkingSession activeSession = parkingSessionRepository.findByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active parking session found for this vehicle."));

        LocalDateTime exitTime = LocalDateTime.now();
        if (exitTime.isBefore(activeSession.getEntryTime())) {
            throw new BadRequestException("Exit time cannot be before entry time.");
        }
        activeSession.setExitTime(exitTime);

        ChargeCalculationResult chargeResult = calculateCharges(activeSession);
        activeSession.setTotalAmount(chargeResult.totalAmount());
        activeSession.setStatus(ParkingSessionStatus.COMPLETED);
        ParkingSession savedSession = parkingSessionRepository.save(activeSession);

        ParkingSlot parkingSlot = activeSession.getParkingSlot();
        parkingSlot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(parkingSlot);

        ParkingSessionDto resultDto = parkingSessionMapper.toDto(savedSession);
        resultDto.setBasePricePerHour(chargeResult.basePricePerHour());
        resultDto.setHoursCharged(chargeResult.hoursCharged());
        resultDto.setOccupancyPercentage(chargeResult.occupancyPercentage());
        resultDto.setMultiplier(chargeResult.multiplier());

        return resultDto;
    }

    /**
     * Calculates the parking charges based on the duration of the stay and the current occupancy of the parking lot.
     *
     * @param session the parking session for which to calculate charges.
     * @return a {@link ChargeCalculationResult} containing all charge details.
     * @throws ResourceNotFoundException if the parking lot associated with the session is not found.
     */
    private ChargeCalculationResult calculateCharges(ParkingSession session) {
        ParkingLot parkingLot = session.getParkingSlot().getParkingLot();
        if (parkingLot == null) {
            throw new ResourceNotFoundException("Parking lot not found for the given slot.");
        }

        long durationMinutes = Duration.between(session.getEntryTime(), session.getExitTime()).toMinutes();

        if (durationMinutes <= 30) {
            return new ChargeCalculationResult(0.0, 0.0, 0L, 0.0, 1.0);
        }

        long billableMinutes = durationMinutes - 30;
        double basePrice = parkingLot.getBasePricePerHour();
        long hoursParked = (billableMinutes + 59) / 60;
        double occupancy = calculateOccupancy(parkingLot);
        double multiplier = getOccupancyMultiplier(occupancy);
        double totalAmount = hoursParked * basePrice * multiplier;

        return new ChargeCalculationResult(totalAmount, basePrice, hoursParked, occupancy, multiplier);
    }

    /**
     * Calculates the current occupancy percentage of a given parking lot.
     *
     * @param parkingLot the parking lot for which to calculate occupancy.
     * @return the occupancy percentage.
     */
    private double calculateOccupancy(ParkingLot parkingLot) {
        if (parkingLot.getTotalSlots() == null || parkingLot.getTotalSlots() == 0) {
            return 0.0;
        }
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        return (double) occupiedSlots / parkingLot.getTotalSlots() * 100;
    }

    /**
     * Determines the pricing multiplier based on the parking lot's occupancy percentage.
     * A higher occupancy results in a higher price to encourage parking in less crowded lots.
     *
     * @param occupancy the current occupancy percentage of the parking lot.
     * @return the pricing multiplier.
     */
    private double getOccupancyMultiplier(double occupancy) {
        if (occupancy <= 50) {
            return 1.0;
        } else if (occupancy <= 80) {
            return 1.25;
        } else {
            return 1.5;
        }
    }
}
