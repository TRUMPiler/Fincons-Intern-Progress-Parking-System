package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.HighOccupancyAlertDto;
import com.fincons.parkingsystem.dto.OccupancyUpdateDto;
import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.ParkingLotDashboardService;
import com.fincons.parkingsystem.service.WebSocketService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation for managing the real-time state of parking lot dashboards.
 * This service maintains an in-memory state of each lot's occupancy and broadcasts updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotDashboardServiceImpl implements ParkingLotDashboardService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final WebSocketService webSocketService;

    // In-memory map to hold the real-time state of each parking lot.
    private final Map<Long, ParkingLotState> dashboardState = new ConcurrentHashMap<>();
    private static final double HIGH_OCCUPANCY_THRESHOLD = 80.0;

    /**
     * Initializes the dashboard state for all parking lots at application startup.
     */
    @PostConstruct
    public void initializeAllDashboards() {
        List<ParkingLot> parkingLots = parkingLotRepository.findAll();
        parkingLots.forEach(lot -> initializeDashboard(lot.getId()));
    }

    /**
     * Initializes or re-initializes the state for a single parking lot.
     * It fetches the current state from the database and broadcasts an initial update.
     *
     * @param parkingLotId The unique identifier of the parking lot to initialize.
     */
    @Override
    public void initializeDashboard(Long parkingLotId) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid parking lot ID: " + parkingLotId));
        // Get counts from DB
        long totalSlots = parkingSlotRepository.countByParkingLot(parkingLot);
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        long availableSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.AVAILABLE);
        long reservedSlots=parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.RESERVED);
        // Update State
        ParkingLotState state = new ParkingLotState(totalSlots, occupiedSlots, availableSlots,reservedSlots);
        dashboardState.put(parkingLotId, state);

        broadcastOccupancyUpdate(parkingLotId);
    }

    /**
     * Updates the occupancy state for a parking lot based on a slot status change event.
     * After updating the state, it broadcasts the new occupancy and checks for high occupancy alerts.
     *
     * @param statusUpdateDto A DTO containing the details of the slot status update.
     */
    @Override
    public void updateOccupancy(SlotStatusUpdateDto statusUpdateDto) {
        Long parkingLotId = statusUpdateDto.getParkingLotId();

        // Ensure state exists, or re-initialize it if missing
        ParkingLotState state = dashboardState.computeIfAbsent(parkingLotId, this::initializeNewDashboardState);

        // Fetch fresh counts from DB to ensure accuracy
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid parking lot ID: " + parkingLotId));

        long currentOccupied = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        long currentAvailable = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.AVAILABLE);
        long reservedSlots=parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.RESERVED);
        // Update the in-memory state
        state.setOccupiedSlots(currentOccupied);
        state.setAvailableSlots(currentAvailable);
        state.setReservedSlots(reservedSlots);
        broadcastOccupancyUpdate(parkingLotId);
        checkAndSendHighOccupancyAlert(parkingLotId);
    }

    /**
     * Broadcasts the current occupancy state of a parking lot to all subscribed clients.
     *
     * @param parkingLotId The ID of the parking lot to broadcast an update for.
     */
    private void broadcastOccupancyUpdate(Long parkingLotId) {
        ParkingLotState state = dashboardState.get(parkingLotId);

        if (state != null) {
            OccupancyUpdateDto update = new OccupancyUpdateDto(
                    parkingLotId,
                    state.getOccupiedSlots()+state.getReservedSlots(),
                    state.getAvailableSlots(),
                    state.getOccupancyPercentage()
            );
            webSocketService.sendSlotUpdate(parkingLotId, update);
        } else {
            // This is safer than throwing ConflictException if a lot is just temporarily empty
            log.warn("Attempted to broadcast update for uninitialized parking lot: {}", parkingLotId);
        }
    }

    /**
     * Checks if the occupancy of a parking lot has exceeded the high occupancy threshold
     * and sends an alert if it has.
     *
     * @param parkingLotId The ID of the parking lot to check.
     */
    private void checkAndSendHighOccupancyAlert(Long parkingLotId) {
        ParkingLotState state = dashboardState.get(parkingLotId);
        if (state != null && state.getOccupancyPercentage() >= HIGH_OCCUPANCY_THRESHOLD) {
            HighOccupancyAlertDto alert = new HighOccupancyAlertDto(
                    parkingLotId,
                    "High occupancy detected!",
                    state.getOccupancyPercentage()
            );
            webSocketService.sendHighOccupancyAlert(alert);
        }
    }

    /**
     * Helper method to initialize the state for a new parking lot if it's not already in the map.
     *
     * @param parkingLotId The ID of the new parking lot.
     * @return The newly created ParkingLotState.
     */
    private ParkingLotState initializeNewDashboardState(Long parkingLotId) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid parking lot ID: " + parkingLotId));

        long totalSlots = parkingLot.getTotalSlots(); // Assuming this is defined in Entity
        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED);
        long availableSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.AVAILABLE);
        long reservedSlots=parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.RESERVED);
        return new ParkingLotState(totalSlots, occupiedSlots, availableSlots,reservedSlots);
    }

    public void SendSlotUpdate(SlotStatusUpdateDto statusUpdateDto) {
        // For Future Reference
    }

    /**
     * An inner class to hold the real-time state of a parking lot's occupancy.
     * Using Lombok for cleaner code.
     */
    @Getter
    @Setter
    private static class ParkingLotState {
        private long totalSlots;
        private long occupiedSlots;
        private long availableSlots;
        private long reservedSlots;
        public ParkingLotState(long totalSlots, long occupiedSlots, long availableSlots,long reservedSlots) {
            this.totalSlots = totalSlots;
            this.occupiedSlots = occupiedSlots;
            this.availableSlots = availableSlots;
            this.reservedSlots=reservedSlots;
        }

        public double getOccupancyPercentage() {
            // FIX: Denominator must be (Occupied + Available) = Total Active Capacity
            // This ignores "Maintenance/Unavailable" slots from the calculation.
            long activeCapacity = occupiedSlots + availableSlots+reservedSlots;
            occupiedSlots+=reservedSlots;
            if (activeCapacity == 0) {
                return 0.0;
            }

            double percentage = ((double) occupiedSlots / activeCapacity) * 100.0;

            // FIX: Round to nearest whole number (e.g., 33.0 instead of 33.33333)
            return Math.round(percentage);
        }
    }
}