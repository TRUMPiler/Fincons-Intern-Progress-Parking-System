package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.mapper.ParkingSessionMapper;
import com.fincons.parkingsystem.repository.ParkingSessionRepository;
import com.fincons.parkingsystem.service.ParkingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the service for retrieving parking session information.
 */
@Service
@RequiredArgsConstructor
public class ParkingSessionServiceImpl implements ParkingSessionService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingSessionMapper parkingSessionMapper;

    /**
     * Retrieves a list of all active parking sessions.
     *
     * @return A list of active parking sessions.
     */
    @Override
    public List<ParkingSessionDto> getActiveSessions() {
        return parkingSessionRepository.findByStatus(ParkingSessionStatus.ACTIVE).stream()
                .map(parkingSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all completed parking sessions.
     *
     * @return A list of completed parking sessions.
     */
    @Override
    public List<ParkingSessionDto> getSessionHistory() {
        return parkingSessionRepository.findByStatus(ParkingSessionStatus.COMPLETED).stream()
                .map(parkingSessionMapper::toDto)
                .collect(Collectors.toList());
    }
}
