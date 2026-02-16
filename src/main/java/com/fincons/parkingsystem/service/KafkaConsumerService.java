package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ReservationUpdate;
import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.dto.VehicleEnteredEvent;
import com.fincons.parkingsystem.dto.VehicleExitedEvent;

/**
 * Service interface for consuming messages from Kafka topics.
 * This contract defines the methods for handling various domain events received from Kafka.
 */
public interface KafkaConsumerService {

    /**
     * Consumes a generic string message from a Kafka topic.
     *
     * @param message The message received from the topic.
     */
    void consume(String message);

    /**
     * Consumes a {@link VehicleEnteredEvent} from the appropriate Kafka topic.
     *
     * @param vehicleEnteredEvent The event object representing a vehicle entry.
     */
    void vehicleEntryConsume(VehicleEnteredEvent vehicleEnteredEvent);

    /**
     * Consumes a {@link VehicleExitedEvent} from the appropriate Kafka topic.
     *
     * @param vehicleExitedEvent The event object representing a vehicle exit.
     */
    void vehicleExitConsume(VehicleExitedEvent vehicleExitedEvent);

    /**
     * Consumes a {@link SlotStatusUpdateDto} from the appropriate Kafka topic.
     *
     * @param slotUpdateDto The DTO containing the slot status update information.
     */
    void slotUpdateConsume(SlotStatusUpdateDto slotUpdateDto);
    void reservationUpdateConsume(ReservationUpdate reservationUpdate);
}
