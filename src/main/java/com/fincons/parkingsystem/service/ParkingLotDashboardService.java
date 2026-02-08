package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;

/**
 * Service interface for managing the real-time state of parking lot dashboards.
 * This contract defines methods for initializing and updating the occupancy state.
 */
public interface ParkingLotDashboardService {

    /**
     * Updates the occupancy state for a parking lot based on a slot status change.
     *
     * @param statusUpdateDto A DTO containing the details of the slot status update.
     */
    void updateOccupancy(SlotStatusUpdateDto statusUpdateDto);

    /**
     * Initializes the dashboard state for a specific parking lot.
     * This method is typically called at application startup or when a lot is first accessed.
     *
     * @param parkingLotId The unique identifier of the parking lot to initialize.
     */
    void initializeDashboard(Long parkingLotId);
    public void SendSlotUpdate(SlotStatusUpdateDto statusUpdateDto);
}
