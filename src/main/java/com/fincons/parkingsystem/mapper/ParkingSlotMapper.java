package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.entity.ParkingSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This is the mapper for my ParkingSlot entities.
 * It handles the conversion between the ParkingSlot entity and its DTO.
 */
@Mapper(componentModel = "spring")
public interface ParkingSlotMapper {

    /**
     * This method converts a ParkingSlot entity to a ParkingSlotDto.
     * I'm mapping the parking lot's ID to the parkingLotId field in the DTO.
     */
    @Mapping(source = "parkingLot.id", target = "parkingLotId")
    ParkingSlotDto toDto(ParkingSlot parkingSlot);

    /**
     * This method converts a ParkingSlotDto back to a ParkingSlot entity.
     */
    @Mapping(source = "parkingLotId", target = "parkingLot.id")
    ParkingSlot toEntity(ParkingSlotDto parkingSlotDto);
}
