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
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the services for managing parking slots.
 * This class provides the business logic for creating and retrieving parking slots associated with a parking lot.
 */
@Service
public class ParkingSlotServiceImpl implements ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotMapper parkingSlotMapper;

    public ParkingSlotServiceImpl(ParkingSlotRepository parkingSlotRepository, ParkingLotRepository parkingLotRepository, ParkingSlotMapper parkingSlotMapper)
    {
        this.parkingSlotRepository=parkingSlotRepository;
        this.parkingLotRepository=parkingLotRepository;
        this.parkingSlotMapper=parkingSlotMapper;
    }

    /**
     * Creates a specified number of parking slots for a given parking lot.
     * All created slots are initialized with an 'AVAILABLE' status.
     *
     * @param parkingLot the parking lot for which to create the slots.
     * @param totalSlots the total number of slots to create.
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
     * Retrieves all parking slots for a specific parking lot.
     *
     * @param parkingLotId the ID of the parking lot.
     * @return a list of DTOs for the parking slots in the specified lot.
     */
    @Override
    public ParkingSlotAvailability GetParkingSlot(Long parkingLotId) {

        ParkingLot parkingLot = parkingLotRepository.getReferenceById(parkingLotId);

        List<ParkingSlotDto> parkingSlots= parkingSlotRepository.findByParkingLot(parkingLot).stream().map(parkingSlotMapper::toDto).toList();
        Long totalAvailableSlots=parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.AVAILABLE);
        return new ParkingSlotAvailability(parkingSlots,totalAvailableSlots);
    }


}
