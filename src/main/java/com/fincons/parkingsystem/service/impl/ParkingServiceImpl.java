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
import org.springframework.context.annotation.Lazy;
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

/**
 * This is the core service for handling the main parking workflow. It manages the business logic
 * for when a vehicle enters and exits a parking lot, including validations, state changes,
 * charge calculations, and publishing events to Kafka. It employs transactional integrity
 * and retry mechanisms to ensure robustness in a concurrent environment.
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
    private final ReservationRepository parkingReservationRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper; // Injected but not used in the provided methods, might be for other methods.

    /**
     * A private record to neatly package the results of a charge calculation.
     * This improves readability and type safety for returning multiple related values.
     *
     * @param totalAmount The final calculated parking fee.
     * @param basePricePerHour The base hourly rate used for calculation.
     * @param hoursCharged The number of hours for which the vehicle was charged.
     * @param occupancyPercentage The occupancy percentage of the lot at the time of exit.
     * @param multiplier The pricing multiplier applied based on occupancy.
     */
    private record ChargeCalculationResult(double totalAmount, double basePricePerHour, long hoursCharged,
                                           double occupancyPercentage, double multiplier) {
    }

    /**
     * Processes a vehicle's entry into a parking lot.
     * This method is transactional and set to the highest isolation level (SERIALIZABLE)
     * to prevent race conditions, such as two vehicles trying to claim the same last spot.
     * It's also retryable for transient database issues like optimistic locking failures or deadlocks.
     *
     * @param entryRequest The request DTO containing the vehicle's details and the target parking lot ID.
     * @return A DTO representing the newly created, active parking session.
     * @throws ResourceNotFoundException if the specified parking lot does not exist.
     * @throws ConflictException if the vehicle already has an active parking session or reservation,
     *                           or if no available parking slots are found.
     * @throws IllegalArgumentException if the entry request is null or contains invalid data.
     */
    @Override
    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    PSQLException.class, // Specific to PostgreSQL, indicating a database error
                    CannotAcquireLockException.class, // Indicates a failure to acquire a lock
                    DeadlockLoserDataAccessException.class // Indicates a transaction was chosen as a deadlock victim
            },
            maxAttempts = 3, // Maximum number of retry attempts
            backoff = @Backoff(delay = 100) // Delay in milliseconds before retrying
    )
    @Transactional(isolation = Isolation.SERIALIZABLE) // Ensures highest level of transaction isolation
    @SneakyThrows // Handles checked exceptions by rethrowing them as unchecked
    public ParkingSessionDto enterVehicle(VehicleEntryRequestDto entryRequest) {
        // Validate incoming request to ensure essential data is present
        Assert.notNull(entryRequest, "Entry request cannot be null.");
        Assert.notNull(entryRequest.getParkingLotId(), "Parking lot ID cannot be null.");
        Assert.hasText(entryRequest.getVehicleNumber(), "Vehicle number cannot be empty.");
        Assert.notNull(entryRequest.getVehicleType(), "Vehicle type cannot be null.");

        log.info("Processing entry for vehicle number: {}", entryRequest.getVehicleNumber());

        // Retrieve the parking lot, throwing an exception if not found
        ParkingLot parkingLot = parkingLotRepository.findById(entryRequest.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Parking lot not found with id: %d", entryRequest.getParkingLotId())));

        // Find the vehicle by its number; if it doesn't exist, create and save a new one
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(entryRequest.getVehicleNumber())
                .orElseGet(() -> {
                    log.info("Creating new vehicle for number: {}", entryRequest.getVehicleNumber());
                    Vehicle newVehicle = Vehicle.builder()
                            .vehicleNumber(entryRequest.getVehicleNumber())
                            .vehicleType(entryRequest.getVehicleType())
                            .build();
                    return vehicleRepository.save(newVehicle);
                });

        // Check for existing active parking sessions or reservations for the vehicle to prevent conflicts
        if (parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active parking session.");
        }
        if(parkingReservationRepository.existsByVehicleAndStatus(vehicle,ReservationStatus.ACTIVE))
        {
            throw new ConflictException("This vehicle already has an active reservation for this parking lot.");
        }

        // Find the first available parking slot in the specified lot.
        // A pessimistic write lock is applied by the repository method to prevent concurrent claims on the same slot.
        ParkingSlot availableSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)
                .orElseThrow(() -> new ConflictException("No available parking slots in this lot."));

        // Update the status of the chosen slot to OCCUPIED and persist the change
        availableSlot.setStatus(SlotStatus.OCCUPIED);
        ParkingSlot updatedSlot = parkingSlotRepository.save(availableSlot);

        // Create a new parking session record for the vehicle
        ParkingSession newSession = ParkingSession.builder()
                .vehicle(vehicle)
                .parkingSlot(updatedSlot)
                .entryTime(LocalDateTime.now())
                .status(ParkingSessionStatus.ACTIVE)
                .build();
        ParkingSession savedSession = parkingSessionRepository.save(newSession);

        // Publish events to Kafka to notify other services (e.g., WebSocket dashboard)
        VehicleEnteredEvent event = new VehicleEnteredEvent(savedSession.getId(), vehicle.getVehicleNumber(), parkingLot.getId(),updatedSlot.getId(), updatedSlot.getSlotNumber(), parkingLot.getName(), savedSession.getEntryTime());
        kafkaProducerService.sendVehicleEntry(event);
        kafkaProducerService.sendSlotUpdateProduce(new SlotStatusUpdateDto(parkingLot.getId(),updatedSlot.getId(), updatedSlot.getSlotNumber(), updatedSlot.getStatus()));

        // Map the saved session entity to a DTO and return it
        return parkingSessionMapper.toDto(savedSession);
    }

    /**
     * Processes a vehicle's exit from a parking lot.
     * This method handles the completion of the active parking session, including calculating charges,
     * updating the session status, and deallocating the parking slot.
     * It is transactional and retryable for robustness.
     *
     * @param vehicleNumber The registration number of the exiting vehicle.
     * @return A DTO of the completed session, including all charge details.
     * @throws ResourceNotFoundException if the vehicle or its active session is not found,
     *                                   or if the associated parking slot/lot cannot be found.
     * @throws BadRequestException if the calculated exit time is before the entry time.
     */
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

        // Find the vehicle by its number
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Vehicle not found with number: %s", vehicleNumber)));

        // Find the active parking session for the vehicle
        ParkingSession activeSession = parkingSessionRepository.findByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active parking session found for this vehicle."));

        // Eagerly fetch the associated parking slot and lot to avoid lazy-loading issues
        // and ensure all necessary data is available for charge calculation and updates.
        ParkingSlot parkingSlot = parkingSlotRepository.findByIdWithInactive(activeSession.getParkingSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + activeSession.getParkingSlotId()));
        ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(parkingSlot.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingSlot.getParkingLotId()));

        LocalDateTime exitTime = LocalDateTime.now();

        // Validate that the exit time is not before the entry time
        if (exitTime.isBefore(activeSession.getEntryTime())) {
            throw new BadRequestException("Exit time cannot be before entry time.");
        }

        // Calculate the parking charges based on session duration and lot occupancy

        ChargeCalculationResult chargeResult = calculateCharges(activeSession, parkingLot);
        activeSession.setTotalAmount(chargeResult.totalAmount());
        activeSession.setStatus(ParkingSessionStatus.COMPLETED); // Mark session as completed
        ParkingSession savedSession = parkingSessionRepository.save(activeSession); // Persist session updates

        parkingSlot.setStatus(SlotStatus.AVAILABLE);
        ParkingSlot updatedSlot = parkingSlotRepository.save(parkingSlot); // Persist slot updates
        // Publish events to Kafka to notify other services
        VehicleExitedEvent event = new VehicleExitedEvent(savedSession.getId(), vehicle.getVehicleNumber(), parkingSlot.getParkingLotId(), parkingLot.getName(),activeSession.getParkingSlot().getId(),activeSession.getParkingSlot().getSlotNumber(),activeSession.getEntryTime() ,LocalDateTime.now(), savedSession.getTotalAmount());


        log.info("updated status is:"+updatedSlot.getStatus());
        // Build the final DTO, including all calculated charge details for the client
        ParkingSessionDto resultDto = parkingSessionMapper.toDto(savedSession);
        resultDto.setBasePricePerHour(parkingLot.getBasePricePerHour());
        resultDto.setHoursCharged(chargeResult.hoursCharged());
        resultDto.setOccupancyPercentage(chargeResult.occupancyPercentage());
        resultDto.setMultiplier(chargeResult.multiplier());
        resultDto.setExitTime(exitTime);
        kafkaProducerService.sendSlotUpdateProduce(new SlotStatusUpdateDto(parkingSlot.getParkingLotId(),updatedSlot.getId(), updatedSlot.getSlotNumber(),updatedSlot.getStatus()));
        kafkaProducerService.sendVehicleExit(event);
        return resultDto;

    }

    /**
     * A private helper method to calculate the parking fee based on the duration of the stay
     * and the current occupancy of the lot. It applies a grace period and dynamic pricing.
     *
     * @param session The parking session for which to calculate charges.
     * @param parkingLot The fully loaded ParkingLot entity, containing base pricing information.
     * @return A record containing the detailed charge information.
     */
    private ChargeCalculationResult calculateCharges(ParkingSession session, ParkingLot parkingLot) {
        // Calculate duration in minutes from entry time to current time
        long durationMinutes = Duration.between(session.getEntryTime(), LocalDateTime.now()).toMinutes();
        log.info("Parking Lot Price: {}", parkingLot.getBasePricePerHour());

        if(parkingLot==null)
        {
            log.info("parkinglot is null");
            throw new ResourceNotFoundException("Parking lot not found during vehicle exit");
        }
        double occupancy = calculateOccupancy(parkingLot); // Get current occupancy percentage
        // Apply a 30-minute grace period; if duration is within this, no charge
        if (durationMinutes <= 30) {
            return new ChargeCalculationResult(0.0, 0.0, 0L, occupancy, 1.0);
        }

        // Calculate billable minutes after the grace period
        long billableMinutes = durationMinutes - 30;
        double basePrice = parkingLot.getBasePricePerHour();
        // Calculate hours charged, rounding up to the nearest hour for any fraction of an hour
        long hoursParked = (billableMinutes + 59) / 60;

        double multiplier = getOccupancyMultiplier(occupancy); // Determine pricing multiplier based on occupancy
        double totalAmount = hoursParked * basePrice * multiplier; // Calculate final total amount
        log.info("Total Amount: " + totalAmount+" Multiplier: "+multiplier+" Base Price: "+basePrice+" Hours Parked: "+hoursParked+" Occupancy: "+occupancy);
        return new ChargeCalculationResult(totalAmount, basePrice, hoursParked, occupancy, multiplier);
    }

    /**
     * A private helper method to calculate the current occupancy percentage of a parking lot.
     *
     * @param parkingLot The parking lot to analyze.
     * @return The occupancy percentage (e.g., 75.5 for 75.5%). Returns 0.0 if the lot has no slots.
     */
    private double calculateOccupancy(ParkingLot parkingLot) {
        if (parkingLot.getTotalSlots() == null || parkingLot.getTotalSlots() == 0) {
            throw new BadRequestException("Parking Lot not found during vehicle exit");
//            return 0.0; // Avoid division by zero if no slots are defined
        }
        // Count currently occupied and reserved slots for the given parking lot
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        long reservedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.RESERVED);
        long totalTakenSlots = occupiedSlots + reservedSlots;

        log.info(reservedSlots+" "+occupiedSlots+" "+totalTakenSlots);
        // Calculate percentage based on total slots, performing floating-point division
        return ((double) totalTakenSlots / parkingLot.getTotalSlots()) * 100.0;
    }

    /**
     * A private helper method to determine the pricing multiplier based on lot occupancy.
     * This implements our dynamic pricing rule:
     * - <= 50% occupancy: 1.0 (standard price)
     * - > 50% and <= 80% occupancy: 1.25 (25% surcharge)
     * - > 80% occupancy: 1.5 (50% surcharge)
     *
     * @param occupancy The current occupancy percentage.
     * @return The pricing multiplier to be applied.
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
