package com.fincons.parkingsystem.service.impl;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import com.fincons.parkingsystem.entity.ParkingSlot;
import com.fincons.parkingsystem.entity.ReservationStatus;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.exception.ConflictException;
import com.fincons.parkingsystem.exception.ResourceNotFoundException;
import com.fincons.parkingsystem.mapper.ParkingLotMapper;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import com.fincons.parkingsystem.repository.ParkingSlotRepository;
import com.fincons.parkingsystem.repository.ReservationRepository;
import com.fincons.parkingsystem.service.ParkingLotService;
import com.fincons.parkingsystem.service.ParkingSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing parking lot resources.
 * This class contains the business logic for creating, retrieving, and managing the state of parking lots.
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
     * Creates a new parking lot and its associated parking slots. This operation is transactional.
     *
     * @param parkingLotDto DTO containing the details of the new parking lot.
     * @return The DTO of the newly created parking lot.
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
     * Retrieves a list of all active (not soft-deleted) parking lots.
     * This operation is read-only.
     *
     * @return A list of DTOs representing all active parking lots.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ParkingLotDto> getAllParkingLots() {
        return parkingLotRepository.findAll().stream()
                .map(parkingLotMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all parking lots, including those that have been soft-deleted.
     * This operation is read-only.
     *
     * @return A list of DTOs representing all parking lots.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ParkingLotDto> getAllParkingLotsDeleted() {
        return parkingLotRepository.findAllWithInactive().stream()
                .map(parkingLotMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Deactivates a parking lot (soft delete), preventing new entries and reservations.
     * This operation checks for active sessions or reservations before proceeding.
     *
     * @param id The ID of the parking lot to be deactivated.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteParkingLot(Long id) {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));
        if (parkingSlotRepository.countByParkingLotAndStatus(parkingLot, SlotStatus.OCCUPIED) > 0) {
            throw new ConflictException("Can't delete Parking Lot because slots are occupied");
        }
        if(reservationRepository.existsByParkingLotAndStatus(parkingLot, ReservationStatus.ACTIVE))
        {
            throw new ConflictException("Can't delete Parking Lot because it has reservations");
        }
        parkingLotRepository.delete(parkingLot);
    }

    /**
     * Reactivates a soft-deleted parking lot and all of its associated parking slots.
     * This operation is transactional to ensure atomicity.
     *
     * @param id The ID of the parking lot to be reactivated.
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
     * Placeholder for a future implementation of updating parking lot details.
     */
    @Override
    public void updateParkingLot(Long id, ParkingLotDto parkingLotDto) {
        // Implementation for updating parking lot details can be added here.
    }
}
