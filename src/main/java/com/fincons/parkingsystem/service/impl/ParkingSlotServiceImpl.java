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
 * This is where the business logic for my parking slot service lives.
 * It handles creating slots and checking their availability.
 */
@Service
@RequiredArgsConstructor
public class ParkingSlotServiceImpl implements ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotMapper parkingSlotMapper;

    /**
     * This method creates all the individual parking slots for a new parking lot.
     * I just loop and create the specified number of slots, setting them all to 'AVAILABLE'.
     */
    @Override
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
     * This method gets the current availability of slots for a specific parking lot.
     * It returns a list of all the slots and a count of how many are free.
     */
    @Override
    public ParkingSlotAvailability getParkingSlotAvailability(Long parkingLotId) {

        ParkingLot parkingLot = parkingLotRepository.getReferenceById(parkingLotId);

        List<ParkingSlotDto> parkingSlots = parkingSlotRepository.findByParkingLot(parkingLot).stream().map(parkingSlotMapper::toDto).toList();
        Long totalAvailableSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.AVAILABLE);
        return new ParkingSlotAvailability(parkingSlots, totalAvailableSlots);
    }
}
