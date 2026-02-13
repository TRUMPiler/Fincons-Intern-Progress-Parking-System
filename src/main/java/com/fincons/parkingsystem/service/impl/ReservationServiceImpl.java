package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;
import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.entity.*;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ReservationMapper;
import com.fincons.parkingsystem.repository.*;
import com.fincons.parkingsystem.service.KafkaProducerService;
import com.fincons.parkingsystem.service.ReservationService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PSQLException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This service handles all business logic related to parking reservations, including creation,
 * cancellation, processing arrivals, and handling expirations. It ensures data consistency
 * through transactional management and is designed to be resilient in a concurrent environment.
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ReservationMapper reservationMapper;
    private final ParkingSessionRepository parkingSessionRepository;
    private final KafkaProducerService kafkaProducerService;

    // A reservation is held for 15 minutes before it automatically expires.
    private static final int RESERVATION_EXPIRATION_MINUTES = 15;

    /**
     * Creates a new reservation for a vehicle. This method is transactional and retryable to handle
     * potential deadlocks or optimistic locking conflicts in a high-concurrency environment.
     * It finds an available slot, reserves it, and creates a reservation record.
     *
     * @param reservationRequestDto The request DTO containing the vehicle and parking lot details.
     * @return A DTO representing the newly created reservation.
     * @throws ConflictException if the vehicle already has an active session or reservation, or if no slots are available.
     * @throws ResourceNotFoundException if the specified parking lot does not exist.
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

        Vehicle vehicle = vehicleRepository.findByVehicleNumber(reservationRequestDto.getVehicleNumber())
                .orElseGet(() -> {
                    Vehicle newVehicle = Vehicle.builder()
                            .vehicleNumber(reservationRequestDto.getVehicleNumber())
                            .vehicleType(reservationRequestDto.getVehicleType())
                            .build();
                    return vehicleRepository.save(newVehicle);
                });

        if (parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active parking session.");
        }
        if (reservationRepository.existsByVehicleAndStatus(vehicle, ReservationStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active reservation.");
        }

        ParkingLot parkingLot = parkingLotRepository.findById(reservationRequestDto.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + reservationRequestDto.getParkingLotId()));

        ParkingSlot availableSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)
                .orElseThrow(() -> new ConflictException("No available parking slots in this lot for reservation."));

        availableSlot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(availableSlot);

        Reservation reservation = Reservation.builder()
                .vehicle(vehicle)
                .parkingSlot(availableSlot)
                .reservationTime(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant())
                .expirationTime(
                Instant.now().plus(Duration.ofMinutes(RESERVATION_EXPIRATION_MINUTES))
        )
                .status(ReservationStatus.ACTIVE)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        SlotStatusUpdateDto statusUpdateDto = new SlotStatusUpdateDto(parkingLot.getId(), availableSlot.getId(), availableSlot.getSlotNumber(),SlotStatus.RESERVED);
        kafkaProducerService.sendSlotUpdateProduce(statusUpdateDto);

        ReservationDto dto = reservationMapper.toDto(savedReservation);
        dto.setParkingLotName(parkingLot.getName());
        dto.setParkingSlotId(availableSlot.getId());
        return dto;
    }

    /**
     * Cancels an active reservation. This makes the previously reserved slot available again.
     * This operation is transactional and retryable.
     *
     * @param reservationId The ID of the reservation to cancel.
     * @throws ResourceNotFoundException if the reservation or its associated slot is not found.
     * @throws ConflictException if the reservation is not in an active state.
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

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        ParkingSlot reservedSlot = parkingSlotRepository.findById(reservation.getParkingSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("No reserved slot found for this reservation."));
        
        reservedSlot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(reservedSlot);
        
        kafkaProducerService.sendSlotUpdateProduce(new SlotStatusUpdateDto(reservedSlot.getParkingLotId(), reservedSlot.getId(), reservedSlot.getSlotNumber(),SlotStatus.AVAILABLE));
    }

    /**
     * Retrieves a paginated list of all reservations in the system.
     * This operation is read-only and enriches the DTO with the parking lot name,
     * safely handling cases where related entities might be soft-deleted.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs for all reservations.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<ReservationDto> getReservationStatus(Pageable pageable) {

        Page<Reservation> reservations =
                reservationRepository.findAllByCustom(pageable);

        return reservations.map(reservation -> {
            ReservationDto dto = reservationMapper.toDto(reservation);

            parkingSlotRepository.findByIdWithInactive(reservation.getParkingSlotId())
                    .flatMap(slot ->
                            parkingLotRepository.findByIdWithInactive(slot.getParkingLotId())
                    )
                    .ifPresent(parkingLot ->
                            dto.setParkingLotName(parkingLot.getName())
                    );

            return dto;
        });
    }


    /**
     * Processes the arrival of a vehicle with an active reservation. This converts the reservation
     * into an active parking session, marks the slot as occupied, and updates the reservation status to COMPLETED.
     *
     * @param reservationId The ID of the reservation to process.
     * @throws ResourceNotFoundException if the reservation or its slot is not found.
     * @throws ConflictException if the reservation is not active.
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

        ParkingSlot reservedSlot = parkingSlotRepository.findById(reservation.getParkingSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("No reserved slot found for this reservation."));

        reservedSlot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(reservedSlot);

        ParkingSession newSession = ParkingSession.builder()
                .vehicle(reservation.getVehicle())
                .parkingSlot(reservedSlot)
                .entryTime(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant())
                .status(ParkingSessionStatus.ACTIVE)
                .build();
        parkingSessionRepository.save(newSession);

        kafkaProducerService.sendSlotUpdateProduce(new SlotStatusUpdateDto(reservedSlot.getParkingLotId(), reservedSlot.getId(), reservedSlot.getSlotNumber(),SlotStatus.OCCUPIED));
        
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
    }

    /**
     * A scheduled task that runs every minute to find and expire unclaimed reservations.
     * It marks the reservation as EXPIRED and sets the corresponding slot back to AVAILABLE.
     * This is a self-healing mechanism to free up slots that were reserved but never used.
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
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void expireReservations() {
        List<Reservation> expiredReservations = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE && r.getExpirationTime().isBefore(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .toList();

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            parkingSlotRepository.findByIdWithInactive(reservation.getParkingSlotId()).ifPresent(reservedSlot -> {
                reservedSlot.setStatus(SlotStatus.AVAILABLE);
                parkingSlotRepository.save(reservedSlot);
                kafkaProducerService.sendSlotUpdateProduce(new SlotStatusUpdateDto(reservedSlot.getParkingLotId(),reservedSlot.getId(), reservedSlot.getSlotNumber(),SlotStatus.AVAILABLE));
            });
        }
    }
}
