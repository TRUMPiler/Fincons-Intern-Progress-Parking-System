package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.ReservationStatus;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.exception.BadRequestException;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingLotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.repository.ReservationRepository;
import com.fincons.parkingsystem.service.ParkingLotService;
import com.fincons.parkingsystem.service.ParkingSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This service provides the core business logic for managing parking lots.
 * It handles operations such as creation, retrieval, and deactivation, ensuring data integrity
 * through transactional management and validation checks.
 */
@Service
@RequiredArgsConstructor
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingSlotService parkingSlotService;
    private final ReservationRepository reservationRepository;

    /**
     * Creates a new parking lot and its associated parking slots.
     * This operation is transactional to ensure that either the lot and all its slots are created, or none are.
     * It prevents the creation of duplicate parking lots by checking for existing names.
     *
     * @param parkingLotDto The DTO containing the details for the new parking lot.
     * @return The DTO of the newly created parking lot, including its generated ID.
     * @throws ConflictException if a parking lot with the same name already exists.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ParkingLotDto createParkingLot(ParkingLotDto parkingLotDto) {
        ParkingLot parkingLot = parkingLotMapper.toEntity(parkingLotDto);
        if (parkingLotRepository.findByName(parkingLot.getName()).isPresent()) {
            throw new ConflictException("Parking lot with the same name already exists.");
        }

        ParkingLot savedParkingLot = parkingLotRepository.save(parkingLot);
        
        parkingSlotService.createParkingSlotsForLot(savedParkingLot, parkingLotDto.getTotalSlots());

        return parkingLotMapper.toDto(savedParkingLot);
    }

    /**
     * Retrieves a paginated list of all active (not soft-deleted) parking lots.
     * This is a read-only operation, optimized for performance and suitable for general user queries.
     *
     * @param pageable An object containing pagination and sorting information.
     * @return A paginated list of DTOs for all active parking lots.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<ParkingLotDto> getAllParkingLots(Pageable pageable) {
        return parkingLotRepository.findAll(pageable)
                .map(parkingLotMapper::toDto);
    }

    /**
     * Retrieves a paginated list of all parking lots, including those that have been soft-deleted.
     * This is a read-only operation intended for administrative views where seeing all records is necessary.
     *
     * @param pageable An object containing pagination and sorting information.
     * @return A paginated list of DTOs for all parking lots, both active and inactive.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<ParkingLotDto> getAllParkingLotsDeleted(Pageable pageable) {
        return parkingLotRepository.findAllWithInactive(pageable)
                .map(parkingLotMapper::toDto);
    }

    /**
     * Deactivates a parking lot (soft delete).
     * Before deactivating, it performs crucial checks to ensure the lot is not currently in use.
     * A lot cannot be deleted if it has any occupied slots or active reservations.
     *
     * @param id The ID of the parking lot to be deactivated.
     * @throws ResourceNotFoundException if the parking lot does not exist.
     * @throws ConflictException if the parking lot has occupied slots.
     * @throws BadRequestException if the parking lot has active reservations.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteParkingLot(Long id) {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));
        if (parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED) > 0) {
            throw new ConflictException("Can't delete Parking Lot because slots are occupied");
        }

        List<ParkingSlot> parkingSlots = parkingSlotRepository.findAllByParkingLotIdWithInactive(parkingLot.getId());
        for (ParkingSlot parkingSlot : parkingSlots) {
            if (reservationRepository.existsByParkingSlotAndStatus(parkingSlot, ReservationStatus.ACTIVE)) {
                throw new BadRequestException("Parking Can't be deleted due to active reservation");
            }
        }
        parkingLotRepository.delete(parkingLot);
    }

    /**
     * Reactivates a soft-deleted parking lot and all of its associated slots.
     * This operation is transactional to ensure that both the lot and its slots are restored together.
     *
     * @param id The ID of the parking lot to be reactivated.
     * @throws ResourceNotFoundException if the parking lot does not exist, even among soft-deleted records.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void reactivateParkingLot(Long id) {
        ParkingLot parkingLot = parkingLotRepository.findByIdWithInactive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));
        parkingLot.setDeleted(false);
        parkingLotRepository.save(parkingLot);

        List<ParkingSlot> slots = parkingSlotRepository.findAllByParkingLotIdWithInactive(id);
        for (ParkingSlot slot : slots) {
            slot.setDeleted(false);
        }
        parkingSlotRepository.saveAll(slots);
    }

    /**
     * Updates the details of an existing parking lot.
     * Note: This method is a placeholder and is not yet implemented.
     *
     * @param id The unique identifier of the parking lot to be updated.
     * @param parkingLotDto A DTO containing the new information for the parking lot.
     */
    @Override
    public void updateParkingLot(Long id, ParkingLotDto parkingLotDto) {
        // Implementation for updating parking lot details can be added here.
    }

    /**
     * Retrieves a non-paginated list of all active (non-deleted) parking lots.
     * This is useful for scenarios where a complete list is needed without pagination, such as populating a dropdown.
     *
     * @return A list of DTOs representing all active parking lots.
     */
    @Override
    public List<ParkingLotDto> findAllParkingLotsNonDeleted() {
        return parkingLotRepository.findAll().stream().map(parkingLotMapper::toDto).toList();
    }
}
