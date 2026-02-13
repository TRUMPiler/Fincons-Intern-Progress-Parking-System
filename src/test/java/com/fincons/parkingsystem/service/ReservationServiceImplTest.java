package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;
import com.fincons.parkingsystem.entity.*;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ReservationMapper;
import com.fincons.parkingsystem.repository.*;
import com.fincons.parkingsystem.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReservationServiceImpl}.
 * This class tests the logic for creating, canceling, and processing reservations,
 * as well as the scheduled expiration task.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ParkingLotRepository parkingLotRepository;
    @Mock
    private ParkingSlotRepository parkingSlotRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Vehicle vehicle;
    private ParkingLot parkingLot;
    private ParkingSlot parkingSlot;
    private Reservation reservation;
    private ReservationRequestDto reservationRequestDto;

    /**
     * Initializes mock objects and test data before each test.
     */
    @BeforeEach
    void setUp() {
        vehicle = new Vehicle(1L, "TEST1234", VehicleType.CAR, false);
        parkingLot = new ParkingLot(1L, "Test Lot", "Location", 10, 10.0, Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, false, 0);
        parkingSlot = new ParkingSlot(101L, "A1", SlotStatus.AVAILABLE, parkingLot, 1L, false, 0);
        reservation = new Reservation(1L, vehicle, parkingSlot, 101L, Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant().plus(Duration.ofMinutes(15)), ReservationStatus.ACTIVE, false, 0);
        reservationRequestDto = new ReservationRequestDto("TEST1234", VehicleType.CAR, 1L);
    }

    /**
     * Tests the successful creation of a reservation.
     */
    @Test
    void createReservation_success() {
        // Arrange
        when(vehicleRepository.findByVehicleNumber(anyString())).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)).thenReturn(false);
        when(reservationRepository.existsByVehicleAndStatus(vehicle, ReservationStatus.ACTIVE)).thenReturn(false);
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(parkingLot));
        when(parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)).thenReturn(Optional.of(parkingSlot));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationMapper.toDto(any(Reservation.class))).thenReturn(new ReservationDto());

        // Act
        ReservationDto result = reservationService.createReservation(reservationRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(SlotStatus.RESERVED, parkingSlot.getStatus());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    /**
     * Tests the successful cancellation of an active reservation.
     */
    @Test
    void cancelReservation_success() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(parkingSlotRepository.findById(reservation.getParkingSlotId())).thenReturn(Optional.of(parkingSlot));

        // Act
        reservationService.cancelReservation(1L);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(SlotStatus.AVAILABLE, parkingSlot.getStatus());
        verify(reservationRepository, times(1)).save(reservation);
        verify(parkingSlotRepository, times(1)).save(parkingSlot);
    }

    /**
     * Verifies that a {@link ConflictException} is thrown when trying to cancel a reservation that is not active.
     */
    @Test
    void cancelReservation_throwsConflictException_whenNotActive() {
        // Arrange
        reservation.setStatus(ReservationStatus.COMPLETED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        assertThrows(ConflictException.class, () -> reservationService.cancelReservation(1L));
    }

    /**
     * Tests the successful processing of a vehicle's arrival for an active reservation.
     */
    @Test
    void processArrival_success() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(parkingSlotRepository.findById(reservation.getParkingSlotId())).thenReturn(Optional.of(parkingSlot));

        // Act
        reservationService.processArrival(1L);

        // Assert
        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
        assertEquals(SlotStatus.OCCUPIED, parkingSlot.getStatus());
        verify(parkingSessionRepository, times(1)).save(any(ParkingSession.class));
    }

    /**
     * Tests the scheduled task that expires reservations and frees up the associated slots.
     */
    @Test
    void expireReservations_correctlyExpiresAndFreesSlot() {
        // Arrange
        reservation.setExpirationTime(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant().minus(Duration.ofMinutes(1)));
        List<Reservation> expiredList = Collections.singletonList(reservation);

        when(reservationRepository.findAll()).thenReturn(expiredList);
        when(parkingSlotRepository.findByIdWithInactive(reservation.getParkingSlotId())).thenReturn(Optional.of(parkingSlot));

        // Act
        reservationService.expireReservations();

        // Assert
        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        assertEquals(SlotStatus.AVAILABLE, parkingSlot.getStatus());
        verify(reservationRepository, times(1)).save(reservation);
        verify(parkingSlotRepository, times(1)).save(parkingSlot);
    }
}
