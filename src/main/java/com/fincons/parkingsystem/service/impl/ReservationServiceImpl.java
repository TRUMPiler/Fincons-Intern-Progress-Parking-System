package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;
import com.fincons.parkingsystem.entity.*;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ReservationMapper;
import com.fincons.parkingsystem.repository.*;
import com.fincons.parkingsystem.service.ReservationService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PSQLException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing parking reservations.
 * This class contains the business logic for creating, canceling, and managing the lifecycle of reservations.
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService
{

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ReservationMapper reservationMapper;
    private final ParkingSessionRepository parkingSessionRepository;

    // Defines the duration in minutes for which a reservation is held before expiring.
    private static final int RESERVATION_EXPIRATION_MINUTES = 15;

    /**
     * Creates a new reservation. This operation is transactional and uses a high isolation level
     * to prevent race conditions when reserving a slot.
     *
     * @param reservationRequestDto A DTO containing the details for the new reservation.
     * @return The DTO of the newly created reservation.
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
    public ReservationDto createReservation(ReservationRequestDto reservationRequestDto) {

        // Find or create the vehicle
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(reservationRequestDto.getVehicleNumber())
                .orElseGet(() -> {
                    Vehicle newVehicle = Vehicle.builder()
                            .vehicleNumber(reservationRequestDto.getVehicleNumber())
                            .vehicleType(reservationRequestDto.getVehicleType())
                            .build();
                    return vehicleRepository.save(newVehicle);
                });

        // Validate that the vehicle does not have an existing active session or reservation
        if (parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active parking session.");
        }
        if (reservationRepository.existsByVehicleAndStatus(vehicle, ReservationStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active reservation.");
        }

        // Find an available slot and reserve it
        ParkingLot parkingLot = parkingLotRepository.findById(reservationRequestDto.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + reservationRequestDto.getParkingLotId()));

        ParkingSlot availableSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)
                .orElseThrow(() -> new ConflictException("No available parking slots in this lot for reservation."));

        availableSlot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(availableSlot);

        // Create and save the new reservation
        Reservation reservation = Reservation.builder()
                .vehicle(vehicle)
                .parkingLot(parkingLot)
                .reservationTime(LocalDateTime.now())
                .expirationTime(LocalDateTime.now().plusMinutes(RESERVATION_EXPIRATION_MINUTES))
                .status(ReservationStatus.ACTIVE)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // Map to DTO and enrich with parking lot name before returning
        ReservationDto dto = reservationMapper.toDto(savedReservation);
        dto.setParkingLotName(parkingLot.getName());
        return dto;
    }

    /**
     * Cancels an active reservation. This operation is transactional.
     *
     * @param reservationId The unique identifier of the reservation to be canceled.
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
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ConflictException("Only active reservations can be cancelled.");
        }

        // Update reservation status and deallocate the reserved slot
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(reservation.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found for this reservation."));

        ParkingSlot reservedSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.RESERVED)
                .orElseThrow(() -> new ResourceNotFoundException("No reserved slot found for this reservation."));

        reservedSlot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(reservedSlot);
    }

    /**
     * Retrieves a list of all reservations. This operation is read-only.
     *
     * @return A list of DTOs representing all reservations.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ReservationDto> getReservationStatus() {
        List<Reservation> reservations = reservationRepository.findAll();

        if (reservations.isEmpty()) {
            throw new ResourceNotFoundException("No reservations found.");
        }
        // Map entities to DTOs and manually set the parking lot name
        return reservations.stream()
                .map(reservation -> {
                    ReservationDto dto = reservationMapper.toDto(reservation);
                    parkingLotRepository.findByIdWithInactive(reservation.getParkingLotId())
                            .ifPresent(parkingLot -> dto.setParkingLotName(parkingLot.getName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Processes the arrival of a vehicle with a reservation, converting it into an active parking session.
     * This operation is transactional.
     *
     * @param reservationId The unique identifier of the reservation to be processed.
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
    public void processArrival(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ConflictException("Reservation is not active.");
        }
        ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(reservation.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found for this reservation."));

        // Find the reserved slot and mark it as occupied
        ParkingSlot reservedSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.RESERVED)
                .orElseThrow(() -> new ResourceNotFoundException("No reserved slot found for this reservation."));

        reservedSlot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(reservedSlot);

        // Create a new parking session and mark the reservation as completed
        ParkingSession newSession = ParkingSession.builder()
                .vehicle(reservation.getVehicle())
                .parkingSlot(reservedSlot)
                .entryTime(LocalDateTime.now())
                .status(ParkingSessionStatus.ACTIVE)
                .build();
        parkingSessionRepository.save(newSession);

        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
    }

    /**
     * A scheduled task that runs periodically to handle expired reservations.
     * This method is transactional to ensure atomicity when updating reservation and slot statuses.
     */
    @Scheduled(fixedRate = 60000)
    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    PSQLException.class,
                    CannotAcquireLockException.class,
                    DeadlockLoserDataAccessException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )// Runs every minute
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void expireReservations()
    {
        // Find all active reservations that have passed their expiration time
        List<Reservation> expiredReservations = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE && r.getExpirationTime().isBefore(LocalDateTime.now()))
                .toList();

        for (Reservation reservation : expiredReservations) {
            // Mark the reservation as EXPIRED and deallocate the slot
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(reservation.getParkingLotId())
                    .orElse(null);

            if (parkingLot != null) {
                ParkingSlot reservedSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.RESERVED)
                        .orElse(null);

                if (reservedSlot != null) {
                    reservedSlot.setStatus(SlotStatus.AVAILABLE);
                    parkingSlotRepository.save(reservedSlot);
                }
            }
        }
    }
}
