package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.*;
import com.fincons.parkingsystem.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation for broadcasting messages over WebSocket.
 * This class uses SimpMessagingTemplate to send messages to STOMP destinations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    /**
     * Sends a real-time occupancy update for a specific parking lot.
     *
     * @param parkingLotId The ID of the parking lot being updated.
     * @param payload The data payload, typically an {@link com.fincons.parkingsystem.dto.OccupancyUpdateDto}.
     */
    @Override
    public void sendSlotUpdate(Long parkingLotId, Object payload) {
        String topic = "/topic/dashboard/" + parkingLotId;
        log.info("Sending occupancy update to WebSocket topic {}: {}", topic, payload);
        messagingTemplate.convertAndSend(topic, new WebSocketMessage<>("OCCUPANCY_UPDATE", payload));
    }
    /**
     * Sends a real-time notification that a vehicle has entered.
     *
     * @param vehicleEnteredEvent The event details for the vehicle entry.
     */
    @Override
    public void sendEntrySessionUpdate(VehicleEnteredEvent vehicleEnteredEvent) {
        log.info("Sending WebSocket message for session entry update");
        messagingTemplate.convertAndSend("/topic/sessions-entry", new WebSocketMessage<>("SESSION_ENTRY", vehicleEnteredEvent));
    }
    /**
     * Sends a real-time notification that a vehicle has exited.
     *
     * @param vehicleExitedEvent The event details for the vehicle exit.
     */
    @Override
    public void sendExitSessionUpdate(VehicleExitedEvent vehicleExitedEvent) {
        log.info("Sending WebSocket message for session exit update");
        messagingTemplate.convertAndSend("/topic/sessions-exit", new WebSocketMessage<>("SESSION_EXIT", vehicleExitedEvent));
    }
    /**
     * Sends a high occupancy alert for a specific parking lot.
     *
     * @param alert The alert DTO containing the warning message and occupancy details.
     */
    @Override
    public void sendHighOccupancyAlert(HighOccupancyAlertDto alert) {
        String topic = "/topic/alerts/" + alert.getParkingLotId();
        log.warn("Sending high occupancy alert to WebSocket topic {}: {}", topic, alert);
        messagingTemplate.convertAndSend(topic, new WebSocketMessage<>("HIGH_OCCUPANCY_ALERT", alert));
    }
    /**
     * Sends a slot status update for a specific parking lot.
     *
     **/
    @Override
    public void SendSlotStatusUpdate(Long parkingLotId, SlotStatusUpdateDto statusUpdateDto) {
        String topic="/topic/slots/"+parkingLotId;
        log.info("Sending slot status update to WebSocket topic {}: {}", topic, statusUpdateDto);
        messagingTemplate.convertAndSend(topic, new WebSocketMessage<>("SLOT_STATUS_UPDATE", statusUpdateDto));
    }

    @Override
    public void reservationUpdate(ReservationUpdate reservationUpdate) {
        String topic="/topic/reservation";
        log.info("Sending reservation update to WebSocket topic {}: {}", topic, reservationUpdate);
        messagingTemplate.convertAndSend("/topic/reservation", new WebSocketMessage<>("RESERVATION_UPDATE", reservationUpdate));
    }
}
