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
 * Implements the service for retrieving parking lot statistics.
 */
@Service
@RequiredArgsConstructor
public class ParkingLotStatsServiceImpl implements ParkingLotStatsService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    /**
     * Retrieves statistics for a specific parking lot.
     *
     * @param id The ID of the parking lot.
     * @return Statistics for the parking lot.
     * @throws ResourceNotFoundException if the parking lot is not found.
     */
    @Override
    @Transactional(readOnly = true)
    public ParkingLotStatsDto getParkingLotStats(Long id) {

        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));

        Double totalRevenue = Optional.ofNullable(parkingSessionRepository.sumOfTotalAmountByParkingLot(parkingLot.getId())).orElse(0.0);
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        double occupancyPercentage = (parkingLot.getTotalSlots() > 0) ? ((double) occupiedSlots / parkingLot.getTotalSlots() * 100) : 0.0;

        Double revenueToday=parkingSessionRepository.sumOfTotalAmountByParkingLotAndExitTime(parkingLot.getId(), LocalDate.now().atStartOfDay(),LocalDateTime.now());
        return ParkingLotStatsDto.builder()
                .parkingLotId(parkingLot.getId())
                .parkingLotName(parkingLot.getName())
                .totalSlots(parkingLot.getTotalSlots())
                .occupiedSlots(occupiedSlots)
                .activeSessions(occupiedSlots)
                .totalRevenue(totalRevenue)
                .basePricePerHour(parkingLot.getBasePricePerHour())
                .occupancyPercentage(Math.ceil(occupancyPercentage))
                .revenueToday(revenueToday)
                .build();
    }
}
