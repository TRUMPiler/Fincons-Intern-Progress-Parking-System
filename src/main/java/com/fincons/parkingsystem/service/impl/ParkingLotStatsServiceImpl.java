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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service implementation for retrieving statistical data about parking lots.
 * This class handles the business logic for calculating and aggregating performance data.
 */
@Service
@RequiredArgsConstructor
public class ParkingLotStatsServiceImpl implements ParkingLotStatsService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    /**
     * Retrieves and calculates key statistics for a single parking lot.
     * This operation is read-only and can retrieve stats for both active and inactive lots.
     *
     * @param id The unique identifier of the parking lot for which to retrieve statistics.
     * @return A DTO containing the calculated statistics.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public ParkingLotStatsDto getParkingLotStats(Long id) {

        ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));

        Double totalRevenue = Optional.ofNullable(parkingSessionRepository.sumOfTotalAmountByParkingLot(parkingLot.getId())).orElse(0.0);
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        double occupancyPercentage = (parkingLot.getTotalSlots() > 0) ? ((double) occupiedSlots / parkingLot.getTotalSlots() * 100) : 0.0;
        long availableSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.AVAILABLE);
        Double revenueToday = Optional.ofNullable(parkingSessionRepository.sumOfTotalAmountByParkingLotAndExitTime(parkingLot.getId(), Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant())).orElse(0.0);

        return ParkingLotStatsDto.builder()
                .parkingLotId(parkingLot.getId())
                .parkingLotName(parkingLot.getName())
                .totalSlots(parkingLot.getTotalSlots())
                .occupiedSlots(occupiedSlots)
                .availableSlots(availableSlots)
                .totalRevenue(totalRevenue)
                .deleted(parkingLot.isDeleted())
                .basePricePerHour(parkingLot.getBasePricePerHour())
                .occupancyPercentage(Math.ceil(occupancyPercentage))
                .revenueToday(revenueToday)
                .build();
    }
}
