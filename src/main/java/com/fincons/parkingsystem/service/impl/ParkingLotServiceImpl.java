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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is where the business logic for managing my parking lots lives.
 * It handles creating, viewing, deactivating, and reactivating lots.
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
     * This method creates a new parking lot and also creates all the parking slots for it.
     * It's transactional, so either both the lot and slots are created, or nothing is.
     */
    @Override
    @Transactional
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
     * This method gets a list of all the active parking lots.
     */
    @Override
    public List<ParkingLotDto> getAllParkingLots() {
        return parkingLotRepository.findAll().stream()
                .map(parkingLotMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * This method gets a list of all parking lots, including the ones I've deactivated.
     */
    @Override
    public List<ParkingLotDto> getAllParkingLotsDeleted() {
        return parkingLotRepository.findAllWithInactive().stream()
                .map(parkingLotMapper::toDto).collect(Collectors.toList());
    }

    /**
     * This method deactivates a parking lot (soft delete).
     * I added checks to make sure I can't delete a lot if it has cars parked in it or active reservations.
     */
    @Override
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
     * This method reactivates a parking lot that was previously deactivated.
     * I also made sure to reactivate all of its parking slots at the same time.
     */
    @Override
    @Transactional
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
     * I've left this here as a placeholder for updating parking lot details in the future.
     */
    @Override
    public void updateParkingLot(Long id, ParkingLotDto parkingLotDto) {
        // Implementation for updating parking lot details can be added here.
    }
}
