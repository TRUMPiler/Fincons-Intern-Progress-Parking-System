package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;
import com.fincons.parkingsystem.entity.*;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ReservationMapper;
import com.fincons.parkingsystem.repository.*;
import com.fincons.parkingsystem.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is where the business logic for my reservation service lives.
 * It handles creating, canceling, and managing the lifecycle of reservations.
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

    // I've set the reservation expiration time to 15 minutes.
    private static final int RESERVATION_EXPIRATION_MINUTES = 15;

    /**
     * This method creates a new reservation.
     * It's transactional to ensure all database operations are consistent.
     */
    @Override
    @Transactional
    public ReservationDto createReservation(ReservationRequestDto reservationRequestDto) {

        // I find the vehicle or create a new one if it doesn't exist.
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(reservationRequestDto.getVehicleNumber())
                .orElseGet(() -> {
                    Vehicle newVehicle = Vehicle.builder()
                            .vehicleNumber(reservationRequestDto.getVehicleNumber())
                            .vehicleType(reservationRequestDto.getVehicleType())
                            .build();
                    return vehicleRepository.save(newVehicle);
                });

        // I check if the vehicle already has an active session or reservation.
        if (parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active parking session.");
        }
        if (reservationRepository.existsByVehicleAndStatus(vehicle, ReservationStatus.ACTIVE)) {
            throw new ConflictException("Vehicle already has an active reservation.");
        }

        // I find an available slot and reserve it.
        ParkingLot parkingLot = parkingLotRepository.findById(reservationRequestDto.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + reservationRequestDto.getParkingLotId()));

        ParkingSlot availableSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)
                .orElseThrow(() -> new ConflictException("No available parking slots in this lot for reservation."));

        availableSlot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(availableSlot);

        // I create and save the new reservation.
        Reservation reservation = Reservation.builder()
                .vehicle(vehicle)
                .parkingLot(parkingLot)
                .reservationTime(LocalDateTime.now())
                .expirationTime(LocalDateTime.now().plusMinutes(RESERVATION_EXPIRATION_MINUTES))
                .status(ReservationStatus.ACTIVE)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // I map the entity to a DTO and set the parking lot name before returning it.
        ReservationDto dto = reservationMapper.toDto(savedReservation);
        dto.setParkingLotName(parkingLot.getName());
        return dto;
    }

    /**
     * This method cancels an active reservation.
     */
    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ConflictException("Only active reservations can be cancelled.");
        }

        // I set the reservation status to CANCELLED and make the slot available again.
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
     * This method gets a list of all reservations.
     */
    @Override
    public List<ReservationDto> getReservationStatus() {
        List<Reservation> reservations = reservationRepository.findAll();

        if (reservations.isEmpty()) {
            throw new ResourceNotFoundException("No reservations found.");
        }
        // I map the entities to DTOs and manually set the parking lot name.
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
     * This method processes the arrival of a vehicle with a reservation.
     * It converts the reservation into an active parking session.
     */
    @Override
    @Transactional
    public void processArrival(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ConflictException("Reservation is not active.");
        }
        ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(reservation.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found for this reservation."));

        // I find the reserved slot and mark it as occupied.
        ParkingSlot reservedSlot = parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.RESERVED)
                .orElseThrow(() -> new ResourceNotFoundException("No reserved slot found for this reservation."));

        reservedSlot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(reservedSlot);

        // I create a new parking session and mark the reservation as completed.
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
     * This is a scheduled task that runs every minute to clean up expired reservations.
     */
    @Scheduled(fixedRate = 60000) // Runs every minute
    @Transactional
    public void expireReservations() {
        // I find all active reservations that have passed their expiration time.
        List<Reservation> expiredReservations = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE && r.getExpirationTime().isBefore(LocalDateTime.now()))
                .toList();

        for (Reservation reservation : expiredReservations) {
            // I mark the reservation as EXPIRED and make the slot available again.
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
