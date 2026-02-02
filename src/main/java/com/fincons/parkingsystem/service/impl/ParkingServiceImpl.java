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
 * This is where the main business logic for my parking service lives.
 * It handles what happens when a vehicle enters and exits.
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
     * I created this private record to easily pass around the results of my charge calculation.
     */
    private record ChargeCalculationResult(double totalAmount, double basePricePerHour, long hoursCharged,
                                           double occupancyPercentage, double multiplier) {
    }

    /**
     * This method handles the logic for a vehicle entering the lot.
     * It's transactional to make sure all database operations succeed or fail together.
     */
    @Override
    @Transactional
    public ParkingSessionDto enterVehicle(VehicleEntryRequestDto entryRequest) {
        // First, I make sure the request has all the necessary information.
        Assert.notNull(entryRequest, "Entry request cannot be null.");
        Assert.notNull(entryRequest.getParkingLotId(), "Parking lot ID cannot be null.");
        Assert.hasText(entryRequest.getVehicleNumber(), "Vehicle number cannot be empty.");
        Assert.notNull(entryRequest.getVehicleType(), "Vehicle type cannot be null.");

        log.info("Processing entry for vehicle number: {}", entryRequest.getVehicleNumber());

        // I find the parking lot and the vehicle. If the vehicle is new, I create it.
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

        // I check if the vehicle is already parked.
        if (parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active parking session.");
        }

        // I find an available slot and mark it as occupied.
        ParkingSlot availableSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)
                .orElseThrow(() -> new ConflictException("No available parking slots in this lot."));

        availableSlot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(availableSlot);

        // Finally, I create and save the new parking session.
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
     * This method handles the logic for a vehicle exiting the lot.
     * It's also transactional to ensure data consistency.
     */
    @Override
    @Transactional
    public ParkingSessionDto exitVehicle(String vehicleNumber) {
        log.info("Processing exit for vehicle number: {}", vehicleNumber);

        // I find the vehicle and its active parking session.
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Vehicle not found with number: %s", vehicleNumber)));

        ParkingSession activeSession = parkingSessionRepository.findByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active parking session found for this vehicle."));

        LocalDateTime exitTime = LocalDateTime.now();
        if (exitTime.isBefore(activeSession.getEntryTime())) {
            throw new BadRequestException("Exit time cannot be before entry time.");
        }

        // I calculate the charges and update the session.
        ChargeCalculationResult chargeResult = calculateCharges(activeSession);
        activeSession.setTotalAmount(chargeResult.totalAmount());
        activeSession.setStatus(ParkingSessionStatus.COMPLETED);
        ParkingSession savedSession = parkingSessionRepository.save(activeSession);

        // I free up the parking slot.
        ParkingSlot parkingSlot = activeSession.getParkingSlot();
        parkingSlot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(parkingSlot);

        // I prepare the final DTO with all the charge details to send back to the user.
        ParkingSessionDto resultDto = parkingSessionMapper.toDto(savedSession);
        resultDto.setBasePricePerHour(chargeResult.basePricePerHour());
        resultDto.setHoursCharged(chargeResult.hoursCharged());
        resultDto.setOccupancyPercentage(chargeResult.occupancyPercentage());
        resultDto.setMultiplier(chargeResult.multiplier());
        resultDto.setExitTime(exitTime);
        return resultDto;
    }

    /**
     * This method calculates the parking fee.
     * The first 30 minutes are free, and after that, the price is adjusted based on how full the lot is.
     */
    private ChargeCalculationResult calculateCharges(ParkingSession session) {
        ParkingLot parkingLot = session.getParkingSlot().getParkingLot();
        if (parkingLot == null) {
            throw new ResourceNotFoundException("Parking lot not found for the given slot.");
        }

        long durationMinutes = Duration.between(session.getEntryTime(), LocalDateTime.now()).toMinutes();

        // If the car was parked for 30 minutes or less, it's free.
        if (durationMinutes <= 30) {
            return new ChargeCalculationResult(0.0, 0.0, 0L, 0.0, 1.0);
        }

        // Otherwise, I calculate the billable hours and apply the occupancy multiplier.
        long billableMinutes = durationMinutes - 30;
        double basePrice = parkingLot.getBasePricePerHour();
        long hoursParked = (billableMinutes + 59) / 60; // This rounds up to the nearest hour.
        double occupancy = calculateOccupancy(parkingLot);
        double multiplier = getOccupancyMultiplier(occupancy);
        double totalAmount = hoursParked * basePrice * multiplier;

        return new ChargeCalculationResult(totalAmount, basePrice, hoursParked, occupancy, multiplier);
    }

    /**
     * This method calculates the current occupancy percentage of a parking lot.
     */
    private double calculateOccupancy(ParkingLot parkingLot) {
        if (parkingLot.getTotalSlots() == null || parkingLot.getTotalSlots() == 0) {
            return 0.0;
        }
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        return (double) occupiedSlots / parkingLot.getTotalSlots() * 100;
    }

    /**
     * This method determines the pricing multiplier based on how full the lot is.
     * The fuller the lot, the higher the price.
     */
    private double getOccupancyMultiplier(double occupancy) {
        if (occupancy <= 50) {
            return 1.0; // Standard price
        } else if (occupancy <= 80) {
            return 1.25; // 25% surcharge
        } else {
            return 1.5; // 50% surcharge for a very full lot
        }
    }
}
