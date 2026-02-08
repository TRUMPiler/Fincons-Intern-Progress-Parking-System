package com.fincons.parkingsystem.service.impl;

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
import com.fincons.parkingsystem.service.ParkingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for retrieving parking session information.
 * This class handles the business logic for fetching active and completed sessions.
 */
@Service
@RequiredArgsConstructor
public class ParkingSessionServiceImpl implements ParkingSessionService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionMapper parkingSessionMapper;

    /**
     * Retrieves a paginated list of all currently active parking sessions.
     * This operation is read-only.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs representing active parking sessions.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<ParkingSessionDto> getActiveSessions(Pageable pageable) {
        Page<ParkingSession> sessionPage = parkingSessionRepository.findByStatus(ParkingSessionStatus.ACTIVE, pageable);
        return sessionPage.map(this::enrichDto);
    }

    /**
     * Retrieves a paginated history of all completed parking sessions.
     * This operation is read-only.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs representing completed parking sessions.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<ParkingSessionDto> getSessionHistory(Pageable pageable) {
        Page<ParkingSession> sessionPage = parkingSessionRepository.findAll(pageable);
        return sessionPage.map(this::enrichDto);
    }

    /**
     * Enriches a ParkingSessionDto with the name of the associated parking lot.
     * This method safely fetches related entities, even if they have been soft-deleted.
     *
     * @param session The ParkingSession entity to process.
     * @return The enriched ParkingSessionDto.
     */
    private ParkingSessionDto enrichDto(ParkingSession session) {
        ParkingSessionDto dto = parkingSessionMapper.toDto(session);
        ParkingSlot slot = parkingSlotRepository.findByIdWithInactive(session.getParkingSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + session.getParkingSlotId()));
        ParkingLot lot = parkingLotRepository.findByIdWithInactive(slot.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + slot.getParkingLotId()));
        dto.setParkingLotName(lot.getName());
        dto.setParkingSlotId(Long.parseLong(slot.getSlotNumber()));
        return dto;
    }
}
