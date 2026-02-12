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
 * This class handles the business logic for fetching paginated lists of active and completed sessions,
 * ensuring that related data is safely retrieved and mapped to DTOs.
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
     * This is a read-only operation, optimized for performance. Each session DTO is enriched
     * with details from related entities.
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
     * Retrieves a paginated history of all parking sessions (both active and completed).
     * This is a read-only operation, suitable for historical reporting. Each session DTO is
     * enriched with details from related entities.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs representing all parking sessions.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<ParkingSessionDto> getSessionHistory(Pageable pageable) {
        Page<ParkingSession> sessionPage = parkingSessionRepository.findAll(pageable);
        return sessionPage.map(this::enrichDto);
    }

    /**
     * Enriches a ParkingSessionDto with the name of the associated parking lot and the slot number.
     * This method safely fetches related entities, even if they have been soft-deleted, by using
     * repository methods that bypass the default "deleted=false" filter. This ensures that
     * historical session data remains complete and accessible.
     *
     * @param session The ParkingSession entity to process.
     * @return The enriched ParkingSessionDto.
     * @throws ResourceNotFoundException if the associated parking slot or lot cannot be found,
     *                                   even among soft-deleted records.
     */
    private ParkingSessionDto enrichDto(ParkingSession session) {
        ParkingSessionDto dto = parkingSessionMapper.toDto(session);
        ParkingSlot slot = parkingSlotRepository.findByIdWithInactive(session.getParkingSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + session.getParkingSlotId()));
        ParkingLot lot = parkingLotRepository.findByIdWithInactive(slot.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + slot.getParkingLotId()));
        dto.setParkingLotName(lot.getName());
        dto.setParkingSlotId(slot.getId());
        dto.setParkingSlotNumber(Long.parseLong(slot.getSlotNumber()));
        return dto;
    }
}
