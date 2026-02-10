package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSession;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSessionMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSessionRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.impl.ParkingSessionServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Unit tests for {@link ParkingSessionServiceImpl}.
 * This class tests the retrieval of active and historical parking sessions,
 * ensuring that the service correctly interacts with repositories and mappers.
 */
@ExtendWith(MockitoExtension.class)
class ParkingSessionServiceImplTest {

    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    @Mock
    private ParkingLotRepository parkingLotRepository;
    @Mock
    private ParkingSlotRepository parkingSlotRepository;
    @Mock
    private ParkingSessionMapper parkingSessionMapper;

    @InjectMocks
    private ParkingSessionServiceImpl parkingSessionService;

    private ParkingSession parkingSession;
    private ParkingSlot parkingSlot;
    private ParkingLot parkingLot;
    private ParkingSessionDto parkingSessionDto;

    /**
     * Sets up common test data before each test.
     */
    @BeforeEach
    void setUp() {
        parkingLot = new ParkingLot();
        parkingLot.setId(1L);
        parkingLot.setName("Test Lot");

        parkingSlot = new ParkingSlot();
        parkingSlot.setId(101L);
        parkingSlot.setSlotNumber("1");
        parkingSlot.setParkingLotId(parkingLot.getId());

        parkingSession = new ParkingSession();
        parkingSession.setId(1L);
        parkingSession.setParkingSlotId(parkingSlot.getId());
        parkingSession.setStatus(ParkingSessionStatus.ACTIVE);

        parkingSessionDto = new ParkingSessionDto();
        parkingSessionDto.setId(parkingSession.getId());
        parkingSessionDto.setParkingSlotId(parkingSlot.getId());
        parkingSessionDto.setParkingLotName(parkingLot.getName());
    }

    /**
     * Tests the successful retrieval of a paginated list of active parking sessions.
     */
    @Test
    void getActiveSessions_success() {
        // Arrange
        Page<ParkingSession> sessionPage = new PageImpl<>(Collections.singletonList(parkingSession));
        when(parkingSessionRepository.findByStatus(eq(ParkingSessionStatus.ACTIVE), any(Pageable.class))).thenReturn(sessionPage);
        when(parkingSessionMapper.toDto(any(ParkingSession.class))).thenReturn(parkingSessionDto);
        when(parkingSlotRepository.findByIdWithInactive(anyLong())).thenReturn(Optional.of(parkingSlot));
        when(parkingLotRepository.findByIdWithInactive(anyLong())).thenReturn(Optional.of(parkingLot));

        // Act
        Page<ParkingSessionDto> result = parkingSessionService.getActiveSessions(PageRequest.of(0, 10));

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(parkingSessionDto.getParkingLotName(), result.getContent().get(0).getParkingLotName());
    }

    /**
     * Tests the successful retrieval of a paginated history of all parking sessions.
     */
    @Test
    void getSessionHistory_success() {
        // Arrange
        Page<ParkingSession> sessionPage = new PageImpl<>(Collections.singletonList(parkingSession));
        when(parkingSessionRepository.findAll(any(Pageable.class))).thenReturn(sessionPage);
        when(parkingSessionMapper.toDto(any(ParkingSession.class))).thenReturn(parkingSessionDto);
        when(parkingSlotRepository.findByIdWithInactive(anyLong())).thenReturn(Optional.of(parkingSlot));
        when(parkingLotRepository.findByIdWithInactive(anyLong())).thenReturn(Optional.of(parkingLot));

        // Act
        Page<ParkingSessionDto> result = parkingSessionService.getSessionHistory(PageRequest.of(0, 10));

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(parkingSessionDto.getParkingLotName(), result.getContent().get(0).getParkingLotName());
    }

    /**
     * Verifies that a {@link ResourceNotFoundException} is thrown during DTO enrichment if the parking slot is not found.
     */
    @Test
    void enrichDto_throwsResourceNotFoundException_whenSlotNotFound() {
        // Arrange
        Page<ParkingSession> sessionPage = new PageImpl<>(Collections.singletonList(parkingSession));
        when(parkingSessionRepository.findByStatus(any(), any(Pageable.class))).thenReturn(sessionPage);
        when(parkingSessionMapper.toDto(any(ParkingSession.class))).thenReturn(parkingSessionDto);
        when(parkingSlotRepository.findByIdWithInactive(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            parkingSessionService.getActiveSessions(PageRequest.of(0, 10));
        });
    }

    /**
     * Verifies that a {@link ResourceNotFoundException} is thrown during DTO enrichment if the parking lot is not found.
     */
    @Test
    void enrichDto_throwsResourceNotFoundException_whenLotNotFound() {
        // Arrange
        Page<ParkingSession> sessionPage = new PageImpl<>(Collections.singletonList(parkingSession));
        when(parkingSessionRepository.findByStatus(any(), any(Pageable.class))).thenReturn(sessionPage);
        when(parkingSessionMapper.toDto(any(ParkingSession.class))).thenReturn(parkingSessionDto);
        when(parkingSlotRepository.findByIdWithInactive(anyLong())).thenReturn(Optional.of(parkingSlot));
        when(parkingLotRepository.findByIdWithInactive(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            parkingSessionService.getActiveSessions(PageRequest.of(0, 10));
        });
    }
}
