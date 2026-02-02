package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSlotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.ParkingSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for managing parking slot resources.
 * This class contains the business logic for creating, updating, and retrieving information
 * about parking slots.
 */
@Service
@RequiredArgsConstructor
public class ParkingSlotServiceImpl implements ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotMapper parkingSlotMapper;

    /**
     * Creates the individual parking slots for a new parking lot.
     *
     * @param parkingLot The parking lot entity to which the slots will be added.
     * @param totalSlots The total number of slots to create.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createParkingSlotsForLot(ParkingLot parkingLot, int totalSlots) {
        List<ParkingSlot> slots = new ArrayList<>();
        for (int i = 1; i <= totalSlots; i++) {
            slots.add(ParkingSlot.builder()
                    .slotNumber(String.valueOf(i))
                    .status(SlotStatus.AVAILABLE)
                    .parkingLot(parkingLot)
                    .build());
        }
        parkingSlotRepository.saveAll(slots);
    }

    /**
     * Retrieves the current availability of parking slots for a specific parking lot.
     * This operation is read-only.
     *
     * @param parkingLotId The unique identifier of the parking lot to check.
     * @return A DTO that encapsulates the list of all parking slots and the count of available ones.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public ParkingSlotAvailability getParkingSlotAvailability(Long parkingLotId) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingLotId));
        List<ParkingSlotDto> parkingSlots = parkingSlotRepository.findByParkingLot(parkingLot).stream().map(parkingSlotMapper::toDto).toList();
        Long totalAvailableSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.AVAILABLE);
        return new ParkingSlotAvailability(parkingSlots, totalAvailableSlots);
    }

    /**
     * Updates the information for a specific parking slot.
     * This operation is transactional to ensure data consistency.
     *
     * @param parkingSlotDto A DTO containing the updated information for the parking slot.
     * @return The updated {@link ParkingSlotDto}.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ParkingSlotDto updateParkingSlotInformation(ParkingSlotDto parkingSlotDto) {
        ParkingSlot updateSlot = parkingSlotRepository.findById(parkingSlotDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + parkingSlotDto.getId()));

        // Only update the status if it's provided in the DTO
        if (parkingSlotDto.getStatus() != null) {
            updateSlot.setStatus(parkingSlotDto.getStatus());
        }
        
        ParkingSlot savedSlot = parkingSlotRepository.save(updateSlot);
        return parkingSlotMapper.toDto(savedSlot);
    }
}
