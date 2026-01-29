package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.mapper.ParkingLotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.service.ParkingLotService;
import com.fincons.parkingsystem.service.ParkingSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the service for managing parking lots.
 */
@Service
@RequiredArgsConstructor
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingSlotService parkingSlotService;

    /**
     * Creates a new parking lot and its slots.
     *
     * @param parkingLotDto DTO with the new parking lot's details.
     * @return The newly created parking lot.
     * @throws ConflictException if a lot with the same name already exists.
     */
    @Override
    @Transactional
    public ParkingLotDto createParkingLot(ParkingLotDto parkingLotDto) {
        ParkingLot parkingLot = parkingLotMapper.toEntity(parkingLotDto);
        if (parkingLotRepository.findByName(parkingLot.getName()).isPresent()) {
            throw new ConflictException("Parking lot with the same name already exists.");
        }

        ParkingLot savedParkingLot = parkingLotRepository.save(parkingLot);
        
        parkingSlotService.createParkingSlotsForLot(savedParkingLot,parkingLotDto.getTotalSlots());

        return parkingLotMapper.toDto(savedParkingLot);
    }

    /**
     * Retrieves a list of all parking lots.
     *
     * @return A list of all parking lots.
     */
    @Override
    public List<ParkingLotDto> getAllParkingLots() {
        return parkingLotRepository.findAll().stream()
                .map(parkingLotMapper::toDto)
                .collect(Collectors.toList());
    }
}
