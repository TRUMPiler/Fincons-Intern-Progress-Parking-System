package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.ReservationStatus;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.exception.BadRequestException;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingLotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.repository.ReservationRepository;
import com.fincons.parkingsystem.service.impl.ParkingLotServiceImpl;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ParkingLotServiceImpl}.
 * This class uses Mockito to isolate the service from its dependencies (repositories, mappers),
 * allowing for focused testing of the business logic within the service layer.
 */
@ExtendWith(MockitoExtension.class)
class ParkingLotServiceImplTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private ParkingLotMapper parkingLotMapper;

    @Mock
    private ParkingSlotRepository parkingSlotRepository;

    @Mock
    private ParkingSlotService parkingSlotService;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ParkingLotServiceImpl parkingLotService;

    private ParkingLot parkingLot;
    private ParkingLotDto parkingLotDto;

    /**
     * Sets up common test data before each test.
     * This method initializes a {@link ParkingLot} entity and its corresponding {@link ParkingLotDto}
     * to be used across multiple test cases.
     */
    @BeforeEach
    void setUp() {
        parkingLot = new ParkingLot();
        parkingLot.setId(1L);
        parkingLot.setName("Test Lot");
        parkingLot.setTotalSlots(10);

        parkingLotDto = new ParkingLotDto();
        parkingLotDto.setId(1L);
        parkingLotDto.setName("Test Lot");
        parkingLotDto.setTotalSlots(10);
    }

    /**
     * Tests the successful creation of a parking lot.
     * Verifies that the service correctly saves the new lot and calls the slot creation service.
     */
    @Test
    void createParkingLot_success() {
        // Arrange: Mock repository and mapper behavior for a successful creation
        when(parkingLotMapper.toEntity(any(ParkingLotDto.class))).thenReturn(parkingLot);
        when(parkingLotRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(parkingLotRepository.save(any(ParkingLot.class))).thenReturn(parkingLot);
        when(parkingLotMapper.toDto(any(ParkingLot.class))).thenReturn(parkingLotDto);

        // Act: Call the service method
        ParkingLotDto result = parkingLotService.createParkingLot(parkingLotDto);

        // Assert: Verify the result and interactions
        assertNotNull(result);
        assertEquals(parkingLotDto.getName(), result.getName());
        verify(parkingSlotService, times(1)).createParkingSlotsForLot(parkingLot, parkingLotDto.getTotalSlots());
    }

    /**
     * Tests that creating a parking lot with a pre-existing name throws a {@link ConflictException}.
     */
    @Test
    void createParkingLot_throwsConflictException_whenNameExists() {
        // Arrange: Mock the repository to find an existing lot by name
        when(parkingLotMapper.toEntity(any(ParkingLotDto.class))).thenReturn(parkingLot);
        when(parkingLotRepository.findByName(anyString())).thenReturn(Optional.of(parkingLot));

        // Act & Assert: Expect a ConflictException to be thrown
        assertThrows(ConflictException.class, () -> parkingLotService.createParkingLot(parkingLotDto));
    }

    /**
     * Tests the retrieval of a paginated list of active parking lots.
     */
    @Test
    void getAllParkingLots_returnsPagedParkingLots() {
        // Arrange: Mock the repository to return a page of parking lots
        Page<ParkingLot> page = new PageImpl<>(Collections.singletonList(parkingLot));
        when(parkingLotRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(parkingLotMapper.toDto(any(ParkingLot.class))).thenReturn(parkingLotDto);

        // Act: Call the service method
        Page<ParkingLotDto> result = parkingLotService.getAllParkingLots(PageRequest.of(0, 10));

        // Assert: Verify the returned page is not empty
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    /**
     * Tests the successful deactivation (soft delete) of an empty parking lot.
     */
    @Test
    void deleteParkingLot_success() {
        // Arrange: Mock dependencies to simulate an empty, deletable lot
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(parkingLot));
        when(parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED)).thenReturn(0L);
        when(parkingSlotRepository.findAllByParkingLotIdWithInactive(1L)).thenReturn(Collections.emptyList());

        // Act: Call the delete method
        parkingLotService.deleteParkingLot(1L);

        // Assert: Verify that the delete method on the repository was called
        verify(parkingLotRepository, times(1)).delete(parkingLot);
    }

    /**
     * Tests that attempting to delete a non-existent parking lot throws a {@link ResourceNotFoundException}.
     */
    @Test
    void deleteParkingLot_throwsResourceNotFoundException() {
        // Arrange: Mock repository to find no lot
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Expect a ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> parkingLotService.deleteParkingLot(1L));
    }

    /**
     * Tests that attempting to delete a parking lot with occupied slots throws a {@link ConflictException}.
     */
    @Test
    void deleteParkingLot_throwsConflictException_whenSlotsOccupied() {
        // Arrange: Mock repository to report occupied slots
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(parkingLot));
        when(parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED)).thenReturn(1L);

        // Act & Assert: Expect a ConflictException
        assertThrows(ConflictException.class, () -> parkingLotService.deleteParkingLot(1L));
    }

    /**
     * Tests that attempting to delete a lot with active reservations throws a {@link BadRequestException}.
     */
    @Test
    void deleteParkingLot_throwsBadRequestException_whenActiveReservations() {
        // Arrange: Mock dependencies to simulate a lot with an active reservation
        ParkingSlot slot = new ParkingSlot();
        slot.setId(10L);
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(parkingLot));
        when(parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED)).thenReturn(0L);
        when(parkingSlotRepository.findAllByParkingLotIdWithInactive(1L)).thenReturn(Collections.singletonList(slot));
        when(reservationRepository.existsByParkingSlotAndStatus(slot, ReservationStatus.ACTIVE)).thenReturn(true);

        // Act & Assert: Expect a BadRequestException
        assertThrows(BadRequestException.class, () -> parkingLotService.deleteParkingLot(1L));
    }

    /**
     * Tests the successful reactivation of a soft-deleted parking lot.
     */
    @Test
    void reactivateParkingLot_success() {
        // Arrange: Set the lot as deleted and mock repository responses
        parkingLot.setDeleted(true);
        when(parkingLotRepository.findByIdWithInactive(1L)).thenReturn(Optional.of(parkingLot));
        when(parkingSlotRepository.findAllByParkingLotIdWithInactive(1L)).thenReturn(Collections.emptyList());

        // Act: Call the reactivate method
        parkingLotService.reactivateParkingLot(1L);

        // Assert: Verify the lot is no longer marked as deleted and was saved
        assertFalse(parkingLot.isDeleted());
        verify(parkingLotRepository, times(1)).save(parkingLot);
        verify(parkingSlotRepository, times(1)).saveAll(Collections.emptyList());
    }

    /**
     * Tests that attempting to reactivate a non-existent parking lot throws a {@link ResourceNotFoundException}.
     */
    @Test
    void reactivateParkingLot_throwsResourceNotFoundException() {
        // Arrange: Mock repository to find no lot
        when(parkingLotRepository.findByIdWithInactive(1L)).thenReturn(Optional.empty());

        // Act & Assert: Expect a ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> parkingLotService.reactivateParkingLot(1L));
    }
}
