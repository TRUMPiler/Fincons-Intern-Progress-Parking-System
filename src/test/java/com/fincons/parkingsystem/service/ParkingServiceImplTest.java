package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.*;
import com.fincons.parkingsystem.entity.*;
import com.fincons.parkingsystem.exception.BadRequestException;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSessionMapper;
import com.fincons.parkingsystem.repository.*;
import com.fincons.parkingsystem.service.impl.ParkingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ParkingServiceImpl}.
 * This class tests the core parking workflow, including vehicle entry and exit,
 * ensuring the logic is correct and dependencies are properly mocked.
 */
@ExtendWith(MockitoExtension.class)
class ParkingServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ParkingSlotRepository parkingSlotRepository;
    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    @Mock
    private ParkingLotRepository parkingLotRepository;
    @Mock
    private ParkingSessionMapper parkingSessionMapper;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ParkingServiceImpl parkingService;

    private Vehicle vehicle;
    private ParkingLot parkingLot;
    private ParkingSlot parkingSlot;
    private ParkingSession parkingSession;
    private VehicleEntryRequestDto entryRequest;

    /**
     * Sets up mock objects and test data before each test.
     */
    @BeforeEach
    void setUp() {
        vehicle = new Vehicle(1L, "TEST1234", VehicleType.CAR, false);
        parkingLot = new ParkingLot(1L, "Test Lot", "Location", 10, 10.0, Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, false, 0);
        parkingSlot = new ParkingSlot(101L, "A1", SlotStatus.AVAILABLE, parkingLot, 1L, false, 0);
        parkingSession = new ParkingSession(1L, vehicle, parkingSlot, 101L, Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant().minusHours(1), null, 0.0, ParkingSessionStatus.ACTIVE, false, 0);
        entryRequest = new VehicleEntryRequestDto("TEST1234", VehicleType.CAR, 1L);
    }

    /**
     * Tests the successful entry of a new vehicle that does not yet exist in the database.
     */
    @Test
    void enterVehicle_success_newVehicle() {
        // Arrange
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(parkingLot));
        when(vehicleRepository.findByVehicleNumber("TEST1234")).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)).thenReturn(false);
        when(reservationRepository.existsByVehicleAndStatus(vehicle, ReservationStatus.ACTIVE)).thenReturn(false);
        when(parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)).thenReturn(Optional.of(parkingSlot));
        when(parkingSlotRepository.save(any(ParkingSlot.class))).thenReturn(parkingSlot);
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenReturn(parkingSession);
        when(parkingSessionMapper.toDto(any(ParkingSession.class))).thenReturn(new ParkingSessionDto());

        // Act
        ParkingSessionDto result = parkingService.enterVehicle(entryRequest);

        // Assert
        assertNotNull(result);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(parkingSlotRepository, times(1)).save(parkingSlot);
        verify(parkingSessionRepository, times(1)).save(any(ParkingSession.class));
        verify(kafkaProducerService, times(1)).sendVehicleEntry(any(VehicleEnteredEvent.class));
        verify(kafkaProducerService, times(1)).sendSlotUpdateProduce(any(SlotStatusUpdateDto.class));
    }

    /**
     * Verifies that a {@link ConflictException} is thrown if a vehicle tries to enter while already having an active session.
     */
    @Test
    void enterVehicle_throwsConflictException_whenVehicleAlreadyParked() {
        // Arrange
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(parkingLot));
        when(vehicleRepository.findByVehicleNumber("TEST1234")).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> parkingService.enterVehicle(entryRequest));
    }

    /**
     * Verifies that a {@link ConflictException} is thrown when a vehicle tries to enter a full parking lot.
     */
    @Test
    void enterVehicle_throwsConflictException_whenNoSlotsAvailable() {
        // Arrange
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(parkingLot));
        when(vehicleRepository.findByVehicleNumber("TEST1234")).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.existsByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)).thenReturn(false);
        when(reservationRepository.existsByVehicleAndStatus(vehicle, ReservationStatus.ACTIVE)).thenReturn(false);
        when(parkingSlotRepository.findFirstByParkingLotAndStatusOrderByIdAsc(parkingLot, SlotStatus.AVAILABLE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConflictException.class, () -> parkingService.enterVehicle(entryRequest));
    }

    /**
     * Tests the successful exit of a vehicle, including charge calculation and status updates.
     */
    @Test
    void exitVehicle_success() {
        // Arrange
        parkingSession.setEntryTime(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant().minusHours(2)); // Ensure duration > 30 mins for charges
        when(vehicleRepository.findByVehicleNumber("TEST1234")).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.findByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)).thenReturn(Optional.of(parkingSession));
        when(parkingSlotRepository.findByIdWithInactive(parkingSession.getParkingSlotId())).thenReturn(Optional.of(parkingSlot));
        when(parkingLotRepository.findByIdWithInactive(parkingSlot.getParkingLotId())).thenReturn(Optional.of(parkingLot));
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenReturn(parkingSession);
        when(parkingSlotRepository.save(any(ParkingSlot.class))).thenReturn(parkingSlot);
        when(parkingSessionMapper.toDto(any(ParkingSession.class))).thenReturn(new ParkingSessionDto());

        // Act
        ParkingSessionDto result = parkingService.exitVehicle("TEST1234");

        // Assert
        assertNotNull(result);
        assertEquals(ParkingSessionStatus.COMPLETED, parkingSession.getStatus());
        assertEquals(SlotStatus.AVAILABLE, parkingSlot.getStatus());
        verify(kafkaProducerService, times(1)).sendVehicleExit(any(VehicleExitedEvent.class));
        verify(kafkaProducerService, times(1)).sendSlotUpdateProduce(any(SlotStatusUpdateDto.class));
    }

    /**
     * Verifies that a {@link ResourceNotFoundException} is thrown when trying to exit a vehicle with no active session.
     */
    @Test
    void exitVehicle_throwsResourceNotFoundException_whenNoActiveSession() {
        // Arrange
        when(vehicleRepository.findByVehicleNumber("TEST1234")).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.findByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> parkingService.exitVehicle("TEST1234"));
    }

    /**
     * Verifies that a {@link BadRequestException} is thrown if the calculated exit time is before the entry time.
     */
    @Test
    void exitVehicle_throwsBadRequestException_whenExitTimeIsBeforeEntryTime() {
        // Arrange
        parkingSession.setEntryTime(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant().plusHours(1)); // Set entry time in the future
        when(vehicleRepository.findByVehicleNumber("TEST1234")).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.findByVehicleAndStatus(vehicle, ParkingSessionStatus.ACTIVE)).thenReturn(Optional.of(parkingSession));
        when(parkingSlotRepository.findByIdWithInactive(parkingSession.getParkingSlotId())).thenReturn(Optional.of(parkingSlot));
        when(parkingLotRepository.findByIdWithInactive(parkingSlot.getParkingLotId())).thenReturn(Optional.of(parkingLot));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> parkingService.exitVehicle("TEST1234"));
    }
}
