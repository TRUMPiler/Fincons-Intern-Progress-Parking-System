package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.dto.ParkingLotStatsDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.mapper.ParkingLotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSessionRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.ParkingLotStatsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingLotStatsServiceImpl implements ParkingLotStatsService
{
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingLotMapper parkingLotMapper;


    @Override
    @Transactional
    public ParkingLotStatsDto getParkingLotStats(Long id) {

        ParkingLot savedParketLot=parkingLotRepository.getReferenceById(id);
        Double parkingLotRevenue=parkingSessionRepository.sumOfTotalAmountByParkingLot(savedParketLot.getId())==null?0.0:parkingSessionRepository.sumOfTotalAmountByParkingLot(savedParketLot.getId());
        Long occupiedSlots=parkingSlotRepository.countByParkingLotAndStatus(savedParketLot, SlotStatus.OCCUPIED);
        Double OccupiedPercentage=(double)  occupiedSlots/ savedParketLot.getTotalSlots() * 100;
        List<ParkingSlot> parkingSlots=parkingSlotRepository.findByParkingLot(savedParketLot);
        Long activeSessions=0L;
        for (ParkingSlot parkingSlot:parkingSlots)
        {
            activeSessions+=parkingSessionRepository.countParkingSessionByParkingSlotAndStatus(parkingSlot, ParkingSessionStatus.ACTIVE);
        }
        return ParkingLotStatsDto.builder()
                .parkingLotId(savedParketLot.getId())
                .parkingLotName(savedParketLot.getName())
                .totalSlots(savedParketLot.getTotalSlots())
                .occupiedSlots(occupiedSlots)
                .activeSessions(activeSessions)
                .RevenueToday(parkingLotRevenue)
                .OccupancyPercentage(OccupiedPercentage)
                .build();
    }
}