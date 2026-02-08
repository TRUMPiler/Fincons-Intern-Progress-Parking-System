package com.fincons.parkingsystem.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincons.parkingsystem.dto.*;
import com.fincons.parkingsystem.entity.*;
import com.fincons.parkingsystem.exception.BadRequestException;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSessionMapper;
import com.fincons.parkingsystem.repository.*;
import com.fincons.parkingsystem.service.KafkaProducerService;
import com.fincons.parkingsystem.service.ParkingService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSessionMapper parkingSessionMapper;
    private final ReservationRepository parkingReservationRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    private record ChargeCalculationResult(double totalAmount, double basePricePerHour, long hoursCharged,
                                           double occupancyPercentage, double multiplier) {
    }

    @Override
    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    PSQLException.class,
                    CannotAcquireLockException.class,
                    DeadlockLoserDataAccessException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @SneakyThrows
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
        if(parkingReservationRepository.existsByVehicleAndStatus(vehicle,ReservationStatus.ACTIVE))
        {
            throw new ConflictException("This vehicle already has an active reservation for this parking lot.");
        }
        ParkingSlot availableSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)
                .orElseThrow(() -> new ConflictException("No available parking slots in this lot."));

        availableSlot.setStatus(SlotStatus.OCCUPIED);
        ParkingSlot updatedSlot = parkingSlotRepository.save(availableSlot);


        ParkingSession newSession = ParkingSession.builder()
                .vehicle(vehicle)
                .parkingSlot(updatedSlot)
                .entryTime(LocalDateTime.now())
                .status(ParkingSessionStatus.ACTIVE)
                .build();
        ParkingSession savedSession = parkingSessionRepository.save(newSession);

        VehicleEnteredEvent event = new VehicleEnteredEvent(savedSession.getId(), vehicle.getVehicleNumber(), parkingLot.getId(), updatedSlot.getId(),parkingLot.getName(), savedSession.getEntryTime());
        kafkaProducerService.sendVehicleEntry(event);
        kafkaProducerService.sendSlotUpdateProduce(new SlotStatusUpdateDto(parkingLot.getId(),updatedSlot.getId(), updatedSlot.getStatus()));
        return parkingSessionMapper.toDto(savedSession);
    }

    @Override
    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    PSQLException.class,
                    CannotAcquireLockException.class,
                    DeadlockLoserDataAccessException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @SneakyThrows
    public ParkingSessionDto exitVehicle(String vehicleNumber) {
        log.info("Processing exit for vehicle number: {}", vehicleNumber);

        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Vehicle not found with number: %s", vehicleNumber)));

        ParkingSession activeSession = parkingSessionRepository.findByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active parking session found for this vehicle."));

        ParkingSlot parkingSlot = parkingSlotRepository.findByIdWithInactive(activeSession.getParkingSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + activeSession.getParkingSlotId()));


        ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(parkingSlot.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingSlot.getParkingLotId()));

        LocalDateTime exitTime = LocalDateTime.now();
        if (exitTime.isBefore(activeSession.getEntryTime())) {
            throw new BadRequestException("Exit time cannot be before entry time.");
        }

        ChargeCalculationResult chargeResult = calculateCharges(activeSession, parkingLot);
        activeSession.setTotalAmount(chargeResult.totalAmount());
        activeSession.setStatus(ParkingSessionStatus.COMPLETED);
        ParkingSession savedSession = parkingSessionRepository.save(activeSession);

        parkingSlot.setStatus(SlotStatus.AVAILABLE);
        ParkingSlot updatedSlot = parkingSlotRepository.save(parkingSlot);

        kafkaProducerService.sendSlotUpdateProduce(new SlotStatusUpdateDto(parkingSlot.getParkingLotId(),updatedSlot.getId(), updatedSlot.getStatus()));
        VehicleExitedEvent event = new VehicleExitedEvent(savedSession.getId(), vehicle.getVehicleNumber(), parkingSlot.getParkingLotId(), parkingLot.getName(),activeSession.getParkingSlot().getId(),activeSession.getEntryTime() ,LocalDateTime.now(), savedSession.getTotalAmount());
        kafkaProducerService.sendVehicleExit(event);

        ParkingSessionDto resultDto = parkingSessionMapper.toDto(savedSession);
        resultDto.setBasePricePerHour(parkingLot.getBasePricePerHour());
        resultDto.setHoursCharged(chargeResult.hoursCharged());
        resultDto.setOccupancyPercentage(chargeResult.occupancyPercentage());
        resultDto.setMultiplier(chargeResult.multiplier());
        resultDto.setExitTime(exitTime);
        return resultDto;
    }

    private ChargeCalculationResult calculateCharges(ParkingSession session, ParkingLot parkingLot) {
        long durationMinutes = Duration.between(session.getEntryTime(), LocalDateTime.now()).toMinutes();
        log.info("Parking Lot Price"+parkingLot.getBasePricePerHour());
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

    private double calculateOccupancy(ParkingLot parkingLot) {
        if (parkingLot.getTotalSlots() == null || parkingLot.getTotalSlots() == 0) {
            return 0.0;
        }
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        return (double) occupiedSlots / parkingLot.getTotalSlots() * 100;
    }

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
