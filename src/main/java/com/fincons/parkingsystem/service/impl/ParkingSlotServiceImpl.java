package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.mapper.ParkingSlotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.ParkingSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the service for managing parking slots.
 */
@Service
@RequiredArgsConstructor
public class ParkingSlotServiceImpl implements ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotMapper parkingSlotMapper;

    /**
     * Creates parking slots for a new parking lot.
     *
     * @param parkingLot The new parking lot.
     * @param totalSlots The number of slots to create.
     */
    @Override
    public void createParkingSlotsForLot(ParkingLot parkingLot,int totalSlots) {

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
     * Retrieves the availability of slots for a parking lot.
     *
     * @param parkingLotId The ID of the parking lot.
     * @return A DTO with the list of slots and the count of available slots.
     */
    @Override
    public ParkingSlotAvailability getParkingSlotAvailability(Long parkingLotId) {

        ParkingLot parkingLot = parkingLotRepository.getReferenceById(parkingLotId);

        List<ParkingSlotDto> parkingSlots= parkingSlotRepository.findByParkingLot(parkingLot).stream().map(parkingSlotMapper::toDto).toList();
        Long totalAvailableSlots=parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.AVAILABLE);
        return new ParkingSlotAvailability(parkingSlots,totalAvailableSlots);
    }
}
