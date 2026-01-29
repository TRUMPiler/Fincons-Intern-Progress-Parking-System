package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.entity.ParkingSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParkingSlotMapper {

    @Mapping(source = "parkingLot.id", target = "parkingLotId")
    ParkingSlotDto toDto(ParkingSlot parkingSlot);

    @Mapping(source = "parkingLotId", target = "parkingLot.id")
    ParkingSlot toEntity(ParkingSlotDto parkingSlotDto);
}
