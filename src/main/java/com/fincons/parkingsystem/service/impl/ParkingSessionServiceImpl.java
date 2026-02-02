package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingSessionDto;

import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSessionMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSessionRepository;

import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.ParkingSessionService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is where the business logic for retrieving parking session information lives.
 * It handles fetching active and completed sessions.
 */
@Service
@RequiredArgsConstructor
public class ParkingSessionServiceImpl implements ParkingSessionService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionMapper parkingSessionMapper;

    /**
     * This method gets a list of all the currently active parking sessions.
     * I've made sure to fetch the parking lot name even if the lot or slot has been deactivated.
     */
    @Override
    public List<ParkingSessionDto> getActiveSessions() {
        List<ParkingSessionDto> parkingSessionDtos = parkingSessionRepository.findByStatus(ParkingSessionStatus.ACTIVE).stream()
                .map(parkingSessionMapper::toDto)
                .toList();
        for (ParkingSessionDto parkingSessionDto : parkingSessionDtos) {
            // I use findByIdWithInactive to safely get the slot, even if it's soft-deleted.
            ParkingSlot parkingSlot = parkingSlotRepository.findByIdWithInactive(parkingSessionDto.getParkingSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + parkingSessionDto.getParkingSlotId()));
            // I do the same for the parking lot to get its name.
            ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(parkingSlot.getParkingLotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingSlot.getParkingLotId()));
            parkingSessionDto.setParkingLotName(parkingLot.getName());
        }
        return parkingSessionDtos;
    }

    /**
     * This method retrieves the history of all completed parking sessions.
     * I've also made sure this works correctly with deactivated lots and slots.
     */
    @Override
    public List<ParkingSessionDto> getSessionHistory() {
        List<ParkingSessionDto> parkingSessionDtos = parkingSessionRepository.findByStatus(ParkingSessionStatus.COMPLETED).stream()
                .map(parkingSessionMapper::toDto)
                .toList();
        for (ParkingSessionDto parkingSessionDto : parkingSessionDtos) {
            // I use findByIdWithInactive here as well to prevent errors.
            ParkingSlot parkingSlot = parkingSlotRepository.findByIdWithInactive(parkingSessionDto.getParkingSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + parkingSessionDto.getParkingSlotId()));
            ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(parkingSlot.getParkingLotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingSlot.getParkingLotId()));
            parkingSessionDto.setParkingLotName(parkingLot.getName());
        }
        return parkingSessionDtos;
    }
}
