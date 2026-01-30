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
 * Implements the service for handling vehicle entry and exit.
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
     * Holds the results of a charge calculation.
     */
    private record ChargeCalculationResult(double totalAmount, double basePricePerHour, long hoursCharged,
                                           double occupancyPercentage, double multiplier) {
    }

    /**
     * Creates a new parking session when a vehicle enters.
     *
     * @param entryRequest DTO with vehicle and parking lot details.
     * @return The created parking session.
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
     * Completes a parking session when a vehicle exits.
     *
     * @param vehicleNumber The vehicle's registration number.
     * @return The completed parking session with charge details.
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
        resultDto.setExitTime(exitTime);
        return resultDto;
    }

    /**
     * Calculates parking charges based on duration and occupancy.
     */
    private ChargeCalculationResult calculateCharges(ParkingSession session) {
        ParkingLot parkingLot = session.getParkingSlot().getParkingLot();
        if (parkingLot == null) {
            throw new ResourceNotFoundException("Parking lot not found for the given slot.");
        }

        long durationMinutes = Duration.between(session.getEntryTime(), LocalDateTime.now()).toMinutes();

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
     * Calculates the current occupancy percentage of a parking lot.
     */
    private double calculateOccupancy(ParkingLot parkingLot) {
        if (parkingLot.getTotalSlots() == null || parkingLot.getTotalSlots() == 0) {
            return 0.0;
        }
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        return (double) occupiedSlots / parkingLot.getTotalSlots() * 100;
    }

    /**
     * Determines the pricing multiplier based on occupancy.
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
