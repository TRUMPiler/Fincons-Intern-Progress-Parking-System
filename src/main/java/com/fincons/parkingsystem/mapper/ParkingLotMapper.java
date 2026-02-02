package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import org.mapstruct.Mapper;

/**
 * This is the mapper for my ParkingLot entities.
 * It handles the conversion between the ParkingLot entity and its DTO.
 * I'm using MapStruct to make this process automatic and avoid writing boilerplate code.
 */
@Mapper(componentModel = "spring", uses = ParkingSlotMapper.class)
public interface ParkingLotMapper {

    /**
     * This method converts a ParkingLot entity to a ParkingLotDto.
     */
    ParkingLotDto toDto(ParkingLot parkingLot);

    /**
     * This method converts a ParkingLotDto back to a ParkingLot entity.
     */
    ParkingLot toEntity(ParkingLotDto parkingLotDto);
}
