package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSlotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.impl.ParkingSlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Unit tests for {@link ParkingSlotServiceImpl}.
 * This class tests the business logic for managing parking slots, ensuring that dependencies
 * are mocked to isolate the service layer.
 */
@ExtendWith(MockitoExtension.class)
class ParkingSlotServiceImplTest {

    @Mock
    private ParkingSlotRepository parkingSlotRepository;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private ParkingSlotMapper parkingSlotMapper;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ParkingSlotServiceImpl parkingSlotService;

    private ParkingLot parkingLot;
    private ParkingSlot parkingSlot;
    private ParkingSlotDto parkingSlotDto;

    /**
     * Initializes common test objects before each test execution.
     */
    @BeforeEach
    void setUp() {
        parkingLot = new ParkingLot();
        parkingLot.setId(1L);
        parkingLot.setName("Test Lot");
        parkingLot.setTotalSlots(10);

        parkingSlot = new ParkingSlot();
        parkingSlot.setId(101L);
        parkingSlot.setSlotNumber("A1");
        parkingSlot.setStatus(SlotStatus.AVAILABLE);
        parkingSlot.setParkingLot(parkingLot);
        parkingSlot.setParkingLotId(parkingLot.getId());

        parkingSlotDto = new ParkingSlotDto();
        parkingSlotDto.setId(101L);
        parkingSlotDto.setSlotNumber("A1");
        parkingSlotDto.setStatus(SlotStatus.AVAILABLE);
        parkingSlotDto.setParkingLotId(parkingLot.getId());
    }

    /**
     * Verifies that the {@code createParkingSlotsForLot} method generates and saves the correct number of slots.
     */
    @Test
    void createParkingSlotsForLot_createsCorrectNumberOfSlots() {
        // Arrange
        int totalSlots = 5;
        parkingLot.setTotalSlots(totalSlots);
        ArgumentCaptor<List<ParkingSlot>> captor = ArgumentCaptor.forClass(List.class);

        // Act
        parkingSlotService.createParkingSlotsForLot(parkingLot, totalSlots);

        // Assert
        verify(parkingSlotRepository, times(1)).saveAll(captor.capture());
        assertEquals(totalSlots, captor.getValue().size());
        assertEquals(SlotStatus.AVAILABLE, captor.getValue().get(0).getStatus());
    }

    /**
     * Tests the successful retrieval of a paginated list of parking slots for a given lot.
     */
    @Test
    void getParkingSlotAvailability_success() {
        // Arrange
        Page<ParkingSlot> page = new PageImpl<>(Collections.singletonList(parkingSlot));
        when(parkingLotRepository.findById(parkingLot.getId())).thenReturn(Optional.of(parkingLot));
        when(parkingSlotRepository.findByParkingLot(any(ParkingLot.class), any(Pageable.class))).thenReturn(page);
        when(parkingSlotMapper.toDto(any(ParkingSlot.class))).thenReturn(parkingSlotDto);

        // Act
        Page<ParkingSlotDto> result = parkingSlotService.getParkingSlotAvailability(parkingLot.getId(), PageRequest.of(0, 10));

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(parkingSlotDto.getId(), result.getContent().get(0).getId());
    }

    /**
     * Verifies that a {@link ResourceNotFoundException} is thrown when trying to get slots for a non-existent parking lot.
     */
    @Test
    void getParkingSlotAvailability_throwsResourceNotFoundException_whenParkingLotNotFound() {
        // Arrange
        when(parkingLotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> parkingSlotService.getParkingSlotAvailability(999L, PageRequest.of(0, 10)));
    }

    /**
     * Tests the successful update of a parking slot's status.
     * Verifies that the repository's save method is called and a Kafka event is produced.
     */
    @Test
    void updateParkingSlotInformation_success() {
        // Arrange
        ParkingSlotDto updateDto = new ParkingSlotDto();
        updateDto.setId(parkingSlot.getId());
        updateDto.setStatus(SlotStatus.OCCUPIED);

        when(parkingSlotRepository.findById(parkingSlot.getId())).thenReturn(Optional.of(parkingSlot));
        when(parkingSlotRepository.save(any(ParkingSlot.class))).thenReturn(parkingSlot);
        when(parkingSlotMapper.toDto(any(ParkingSlot.class))).thenReturn(updateDto);

        // Act
        ParkingSlotDto result = parkingSlotService.updateParkingSlotInformation(updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(SlotStatus.OCCUPIED, result.getStatus());
        verify(parkingSlotRepository, times(1)).save(parkingSlot);
        verify(kafkaProducerService, times(1)).sendSlotUpdateProduce(any(SlotStatusUpdateDto.class));
    }

    /**
     * Verifies that a {@link ResourceNotFoundException} is thrown when trying to update a non-existent slot.
     */
    @Test
    void updateParkingSlotInformation_throwsResourceNotFoundException_whenSlotNotFound() {
        // Arrange
        ParkingSlotDto updateDto = new ParkingSlotDto();
        updateDto.setId(999L);
        updateDto.setStatus(SlotStatus.OCCUPIED);

        when(parkingSlotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> parkingSlotService.updateParkingSlotInformation(updateDto));
    }

    /**
     * Tests that a slot's status is not changed if the update DTO has a null status.
     */
    @Test
    void updateParkingSlotInformation_noStatusChange() {
        // Arrange
        ParkingSlotDto updateDto = new ParkingSlotDto();
        updateDto.setId(parkingSlot.getId());
        // Status is null in DTO

        when(parkingSlotRepository.findById(parkingSlot.getId())).thenReturn(Optional.of(parkingSlot));
        when(parkingSlotRepository.save(any(ParkingSlot.class))).thenReturn(parkingSlot);
        when(parkingSlotMapper.toDto(any(ParkingSlot.class))).thenReturn(parkingSlotDto); // Mapper returns original DTO

        // Act
        ParkingSlotDto result = parkingSlotService.updateParkingSlotInformation(updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(SlotStatus.AVAILABLE, result.getStatus()); // Status should remain unchanged
        verify(parkingSlotRepository, times(1)).save(parkingSlot);
        verify(kafkaProducerService, times(1)).sendSlotUpdateProduce(any(SlotStatusUpdateDto.class));
    }
}
