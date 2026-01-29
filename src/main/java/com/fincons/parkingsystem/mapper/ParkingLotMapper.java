package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.entity.ParkingLot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = ParkingSlotMapper.class)
public interface ParkingLotMapper
{
    ParkingLotDto toDto(ParkingLot parkingLot);
    ParkingLot toEntity(ParkingLotDto parkingLotDto);
}