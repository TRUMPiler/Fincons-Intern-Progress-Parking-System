package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between {@link ParkingLot} entities and {@link ParkingLotDto} objects.
 * This interface automates the mapping process, reducing boilerplate code and ensuring consistency.
 */
@Mapper(componentModel = "spring", uses = ParkingSlotMapper.class)
public interface ParkingLotMapper {

    /**
     * Converts a {@link ParkingLot} entity to a {@link ParkingLotDto}.
     *
     * @param parkingLot The entity to be converted.
     * @return The corresponding DTO.
     */
    ParkingLotDto toDto(ParkingLot parkingLot);

    /**
     * Converts a {@link ParkingLotDto} to a {@link ParkingLot} entity.
     *
     * @param parkingLotDto The DTO to be converted.
     * @return The corresponding entity.
     */
    ParkingLot toEntity(ParkingLotDto parkingLotDto);
}
