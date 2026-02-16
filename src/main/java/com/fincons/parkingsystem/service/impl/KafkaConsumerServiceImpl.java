package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ReservationUpdate;
import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.dto.VehicleEnteredEvent;
import com.fincons.parkingsystem.dto.VehicleExitedEvent;
import com.fincons.parkingsystem.service.KafkaConsumerService;
import com.fincons.parkingsystem.service.ParkingLotDashboardService;
import com.fincons.parkingsystem.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service implementation for consuming messages from Kafka topics.
 * This class contains methods annotated with @KafkaListener to handle incoming events
 * and delegate them to the appropriate services.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final WebSocketService webSocketService;
    private final ParkingLotDashboardService parkingLotDashboardService;

    /**
     * Consumes a generic string message from a Kafka topic.
     * This method is currently not in active use but serves as an example.
     *
     * @param message The message received from the topic.
     */
    @Override
    public void consume(String message) {
        log.info("Consumed message from Kafka: {}", message);
    }

    /**
     * Listens for and consumes {@link VehicleEnteredEvent} messages from the "vehicle-entry" topic.
     * Upon consumption, it forwards the event to the WebSocket service to notify clients.
     *
     * @param vehicleEnteredEvent The event object representing a vehicle entry.
     */
    @KafkaListener(
            topics = "vehicle-entry",
            groupId = "group_id",
            containerFactory = "vehicleEnteredKafkaListenerFactory"
    )
    @Override
    public void vehicleEntryConsume(VehicleEnteredEvent vehicleEnteredEvent) {
        log.info("Vehicle Entry Notified: {}", vehicleEnteredEvent);
        webSocketService.sendEntrySessionUpdate(vehicleEnteredEvent);
    }

    /**
     * Listens for and consumes {@link VehicleExitedEvent} messages from the "vehicle-exit" topic.
     * Upon consumption, it forwards the event to the WebSocket service to notify clients.
     *
     * @param vehicleExitedEvent The event object representing a vehicle exit.
     */
    @KafkaListener(
            topics = "vehicle-exit",
            groupId = "group_id",
            containerFactory = "vehicleExitedKafkaListenerFactory"
    )
    @Override
    public void vehicleExitConsume(VehicleExitedEvent vehicleExitedEvent) {
        log.info("Vehicle Exit Notified: {}", vehicleExitedEvent);
        webSocketService.sendExitSessionUpdate(vehicleExitedEvent);
    }

    /**
     * Listens for and consumes {@link SlotStatusUpdateDto} messages from the "slot-update" topic.
     * Upon consumption, it delegates the event to the ParkingLotDashboardService to update
     * the real-time occupancy state.
     *
     * @param slotUpdateDto The DTO containing the slot status update information.
     */
    @KafkaListener(
            topics = "slot-update",
            groupId = "group_id",
            containerFactory = "slotUpdateKafkaListenerFactory"
    )
    @Override
    public void slotUpdateConsume(SlotStatusUpdateDto slotUpdateDto) {
        log.info("Slot Update Notified: {}", slotUpdateDto);
        parkingLotDashboardService.updateOccupancy(slotUpdateDto);
        webSocketService.SendSlotStatusUpdate(slotUpdateDto.getParkingLotId(), slotUpdateDto);
    }

    @KafkaListener(
            topics = "reservation",
            groupId = "group_id",
            containerFactory = "reservationUpdateKafkaListenerFactory"
    )
    @Override
    public void reservationUpdateConsume(ReservationUpdate reservationUpdate) {
        log.info("Reservation Update Notified: {}", reservationUpdate);
        webSocketService.reservationUpdate(reservationUpdate);
    }

}
