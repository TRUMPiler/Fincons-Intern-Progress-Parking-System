package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.entity.ParkingLot;

public interface ParkingSlotService {

    void createParkingSlotsForLot(ParkingLot parkingLot,int slots);

    ParkingSlotAvailability GetParkingSlot(Long parkingLotId);
}
