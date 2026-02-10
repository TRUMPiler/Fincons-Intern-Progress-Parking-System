package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.dto.SlotStatusUpdateDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingSlotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.service.KafkaProducerService;
import com.fincons.parkingsystem.service.ParkingSlotService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PSQLException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for managing parking slot resources.
 * This class contains the business logic for creating, updating, and retrieving information
 * about individual parking slots, and it integrates with Kafka to broadcast status changes.
 */
@Service
@RequiredArgsConstructor
public class ParkingSlotServiceImpl implements ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotMapper parkingSlotMapper;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Creates the individual parking slots for a new parking lot.
     * This operation is transactional and generates a specified number of slots,
     * each initialized as AVAILABLE.
     *
     * @param parkingLot The parking lot entity to which the slots will be added.
     * @param totalSlots The total number of slots to create.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createParkingSlotsForLot(ParkingLot parkingLot, int totalSlots) {
        List<ParkingSlot> slots = new ArrayList<>();
        for (int i = 1; i <= totalSlots; i++) {
            slots.add(ParkingSlot.builder()
                    .slotNumber(String.valueOf(i))
                    .status(SlotStatus.AVAILABLE)
                    .parkingLot(parkingLot)
                    .build());
        }
        parkingSlotRepository.saveAll(slots);
    }

    /**
     * Retrieves a paginated list of parking slots for a specific parking lot.
     * This is a read-only operation, suitable for displaying the status of slots to users.
     *
     * @param parkingLotId The unique identifier of the parking lot to check.
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs representing the parking slots.
     * @throws ResourceNotFoundException if the parking lot does not exist.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<ParkingSlotDto> getParkingSlotAvailability(Long parkingLotId, Pageable pageable) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + parkingLotId));
        return parkingSlotRepository.findByParkingLot(parkingLot, pageable)
                .map(parkingSlotMapper::toDto);
    }

    /**
     * Updates the information for a specific parking slot, such as its status.
     * This operation is transactional and retryable to handle concurrent updates safely.
     * After a successful update, it publishes a message to Kafka to notify other services
     * of the slot's status change.
     *
     * @param parkingSlotDto A DTO containing the updated information for the parking slot.
     * @return The updated {@link ParkingSlotDto}.
     * @throws ResourceNotFoundException if the parking slot does not exist.
     */
    @Override
    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    PSQLException.class,
                    CannotAcquireLockException.class,
                    DeadlockLoserDataAccessException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ParkingSlotDto updateParkingSlotInformation(ParkingSlotDto parkingSlotDto) {
        ParkingSlot updateSlot = parkingSlotRepository.findById(parkingSlotDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + parkingSlotDto.getId()));

        if (parkingSlotDto.getStatus() != null) {
            updateSlot.setStatus(parkingSlotDto.getStatus());
        }
        
        ParkingSlot savedSlot = parkingSlotRepository.save(updateSlot);
        SlotStatusUpdateDto statusUpdateDto = new SlotStatusUpdateDto(updateSlot.getParkingLot().getId(), updateSlot.getId(), updateSlot.getStatus());
        kafkaProducerService.sendSlotUpdateProduce(statusUpdateDto);
        return parkingSlotMapper.toDto(savedSlot);
    }
}
