package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.entity.ParkingSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps between ParkingSession entity and ParkingSessionDto.
 */
@Mapper(componentModel = "spring")
public interface ParkingSessionMapper {

    @Mapping(source = "vehicle.vehicleNumber", target = "vehicleNumber")
    @Mapping(source = "parkingSlot.id", target = "parkingSlotId")
    @Mapping(target = "parkingLotName",ignore = true)
    ParkingSessionDto toDto(ParkingSession parkingSession);

    @Mapping(source = "vehicleNumber", target = "vehicle.vehicleNumber")
    @Mapping(source = "parkingSlotId", target = "parkingSlot.id")
    ParkingSession toEntity(ParkingSessionDto parkingSessionDto);
}
