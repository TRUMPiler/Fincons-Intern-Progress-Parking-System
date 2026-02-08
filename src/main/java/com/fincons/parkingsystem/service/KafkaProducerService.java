package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.dto.VehicleEnteredEvent;
import com.fincons.parkingsystem.dto.VehicleExitedEvent;

/**
 * Service interface for producing and sending messages to Kafka topics.
 * This contract defines the methods for publishing various domain events.
 */
public interface KafkaProducerService {

    /**
     * Sends a generic string message to a default Kafka topic.
     *
     * @param message The message to be sent.
     */
    void sendMessage(String message);

    /**
     * Publishes a {@link VehicleEnteredEvent} to the appropriate Kafka topic.
     *
     * @param vehicleEnteredEvent The event object representing a vehicle entry.
     */
    void sendVehicleEntry(VehicleEnteredEvent vehicleEnteredEvent);

    /**
     * Publishes a {@link VehicleExitedEvent} to the appropriate Kafka topic.
     *
     * @param vehicleExitedEvent The event object representing a vehicle exit.
     */
    void sendVehicleExit(VehicleExitedEvent vehicleExitedEvent);

    /**
     * Publishes a {@link SlotStatusUpdateDto} to the appropriate Kafka topic.
     *
     * @param slotUpdateDto The DTO containing the slot status update information.
     */
    void sendSlotUpdateProduce(SlotStatusUpdateDto slotUpdateDto);
}
