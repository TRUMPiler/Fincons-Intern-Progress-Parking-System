package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingLotStatsDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSessionRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.ParkingLotStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * This is where the business logic for my parking lot statistics service lives.
 * It handles calculating and retrieving performance data for a specific lot.
 */
@Service
@RequiredArgsConstructor
public class ParkingLotStatsServiceImpl implements ParkingLotStatsService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    /**
     * This method retrieves and calculates key statistics for a single parking lot.
     * It's read-only because I'm only fetching data, not changing it.
     */
    @Override
    @Transactional(readOnly = true)
    public ParkingLotStatsDto getParkingLotStats(Long id) {

        // I use findByIdWithInactive to make sure I can get stats even for deactivated lots.
        ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));

        // I calculate the total revenue, number of occupied slots, and occupancy percentage.
        Double totalRevenue = Optional.ofNullable(parkingSessionRepository.sumOfTotalAmountByParkingLot(parkingLot.getId())).orElse(0.0);
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        double occupancyPercentage = (parkingLot.getTotalSlots() > 0) ? ((double) occupiedSlots / parkingLot.getTotalSlots() * 100) : 0.0;

        // I also calculate how much revenue has been generated today.
        Double revenueToday = parkingSessionRepository.sumOfTotalAmountByParkingLotAndExitTime(parkingLot.getId(), LocalDate.now().atStartOfDay(), LocalDateTime.now());
        if (revenueToday == null) {
            revenueToday = 0.0;
        }

        // I build the DTO with all the calculated stats to send back.
        return ParkingLotStatsDto.builder()
                .parkingLotId(parkingLot.getId())
                .parkingLotName(parkingLot.getName())
                .totalSlots(parkingLot.getTotalSlots())
                .occupiedSlots(occupiedSlots)
                .activeSessions(occupiedSlots)
                .totalRevenue(totalRevenue)
                .deleted(parkingLot.isDeleted())
                .basePricePerHour(parkingLot.getBasePricePerHour())
                .occupancyPercentage(Math.ceil(occupancyPercentage))
                .revenueToday(revenueToday)
                .build();
    }
}
