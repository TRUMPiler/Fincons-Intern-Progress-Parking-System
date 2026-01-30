package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingSessionDto;

import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSessionMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.repository.ParkingSessionRepository;

import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.ParkingSessionService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the service for retrieving parking session information.
 * along with parking lot name for better data visibility
 */
@Service
@RequiredArgsConstructor
public class ParkingSessionServiceImpl implements ParkingSessionService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionMapper parkingSessionMapper;

    /**
     * Retrieves a list of all active parking sessions.
     *
     * @return A list of active parking sessions.
     */
    @Override
    public List<ParkingSessionDto> getActiveSessions() {
        List<ParkingSessionDto> parkingSessionDtos= parkingSessionRepository.findByStatus(ParkingSessionStatus.ACTIVE).stream()
                .map(parkingSessionMapper::toDto)
                .toList();
        for(ParkingSessionDto parkingSessionDto : parkingSessionDtos) {
            ParkingSlot parkingSlot =parkingSlotRepository.findParkingSlotById(parkingSessionDto.getParkingSlotId())
                    .orElseThrow(()-> new ResourceNotFoundException("ParkingLot not found"));
            ParkingLot parkingLot=parkingLotRepository.getReferenceById(parkingSlot.getParkingLot().getId());
            parkingSessionDto.setParkingLotName(parkingLot.getName());
        }
        return parkingSessionDtos;
    }

    /**
     * Retrieves a list of all completed parking sessions.
     *
     * @return A list of completed parking sessions.
     */
    @Override
    public List<ParkingSessionDto> getSessionHistory() {
        List<ParkingSessionDto> parkingSessionDtos= parkingSessionRepository.findByStatus(ParkingSessionStatus.COMPLETED).stream()
                .map(parkingSessionMapper::toDto)
                .toList();
        for(ParkingSessionDto parkingSessionDto : parkingSessionDtos) {
            ParkingSlot parkingSlot =parkingSlotRepository.findParkingSlotById(parkingSessionDto.getParkingSlotId())
                    .orElseThrow(()-> new ResourceNotFoundException("ParkingLot not found"));
            ParkingLot parkingLot=parkingLotRepository.getReferenceById(parkingSlot.getParkingLot().getId());
            parkingSessionDto.setParkingLotName(parkingLot.getName());
        }
        return parkingSessionDtos;
    }
}
