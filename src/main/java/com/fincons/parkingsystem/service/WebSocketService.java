package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.*;

/**
 * Service interface for broadcasting messages over WebSocket.
 * This contract defines the methods for sending various real-time updates to clients.
 */
public interface WebSocketService {

    /**
     * Sends a real-time occupancy update for a specific parking lot.
     *
     * @param parkingLotId The ID of the parking lot being updated.
     * @param payload The data payload, typically an {@link com.fincons.parkingsystem.dto.OccupancyUpdateDto}.
     */
    void sendSlotUpdate(Long parkingLotId, Object payload);

    /**
     * Sends a real-time notification that a vehicle has entered.
     *
     * @param vehicleEnteredEvent The event details for the vehicle entry.
     */
    void sendEntrySessionUpdate(VehicleEnteredEvent vehicleEnteredEvent);

    /**
     * Sends a real-time notification that a vehicle has exited.
     *
     * @param vehicleExitedEvent The event details for the vehicle exit.
     */
    void sendExitSessionUpdate(VehicleExitedEvent vehicleExitedEvent);

    /**
     * Sends a high occupancy alert for a specific parking lot.
     *
     * @param alert The alert DTO containing the warning message and occupancy details.
     */
    void sendHighOccupancyAlert(HighOccupancyAlertDto alert);

    void SendSlotStatusUpdate(Long parkingLotId, SlotStatusUpdateDto statusUpdateDto);
    void reservationUpdate(ReservationUpdate reservationUpdate);
}
