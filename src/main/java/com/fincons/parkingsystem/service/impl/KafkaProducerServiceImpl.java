package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.dto.VehicleEnteredEvent;
import com.fincons.parkingsystem.dto.VehicleExitedEvent;
import com.fincons.parkingsystem.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation for producing and sending messages to Kafka topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaMessageTemplate;
    private final KafkaTemplate<String, Object> kafkaEntryMessageTemplate;
    private final KafkaTemplate<String, Object> kafkaExitMessageTemplate;
    private final KafkaTemplate<String, Object> kafkaSlotUpdateMessageTemplate;

    private static final String DEFAULT_TOPIC_NAME = "parking-system";
    private static final String VEHICLE_ENTRY_TOPIC_NAME = "vehicle-entry";
    private static final String VEHICLE_EXIT_TOPIC_NAME = "vehicle-exit";
    private static final String SLOT_UPDATE_TOPIC_NAME = "slot-update";

    /**
     * Sends a generic string message to the default Kafka topic.
     *
     * @param message The message to be sent.
     */
    @Override
    public void sendMessage(String message) {
        log.info("Producing message to topic {}: {}", DEFAULT_TOPIC_NAME, message);
        kafkaMessageTemplate.send(DEFAULT_TOPIC_NAME, message);
    }

    /**
     * Publishes a {@link VehicleEnteredEvent} to the vehicle entry topic.
     *
     * @param vehicleEnteredEvent The event object representing a vehicle entry.
     */
    @Override
    public void sendVehicleEntry(VehicleEnteredEvent vehicleEnteredEvent) {
        log.info("Producing vehicle entry event to topic {}: {}", VEHICLE_ENTRY_TOPIC_NAME, vehicleEnteredEvent);
        kafkaEntryMessageTemplate.send(VEHICLE_ENTRY_TOPIC_NAME, vehicleEnteredEvent);
    }

    /**
     * Publishes a {@link VehicleExitedEvent} to the vehicle exit topic.
     *
     * @param vehicleExitedEvent The event object representing a vehicle exit.
     */
    @Override
    public void sendVehicleExit(VehicleExitedEvent vehicleExitedEvent) {
        log.info("Producing vehicle exit event to topic {}: {}", VEHICLE_EXIT_TOPIC_NAME, vehicleExitedEvent);
        kafkaExitMessageTemplate.send(VEHICLE_EXIT_TOPIC_NAME, vehicleExitedEvent);
    }

    /**
     * Publishes a {@link SlotStatusUpdateDto} to the slot update topic.
     *
     * @param slotUpdateDto The DTO containing the slot status update information.
     */
    @Override
    public void sendSlotUpdateProduce(SlotStatusUpdateDto slotUpdateDto) {
        log.info("Producing slot update event to topic {}: {}", SLOT_UPDATE_TOPIC_NAME, slotUpdateDto);
        kafkaSlotUpdateMessageTemplate.send(SLOT_UPDATE_TOPIC_NAME, slotUpdateDto);

    }
}
