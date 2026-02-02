package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.entity.ParkingSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This is the mapper for my ParkingSession entities.
 * It handles the conversion between the ParkingSession entity and its DTO.
 */
@Mapper(componentModel = "spring")
public interface ParkingSessionMapper {

    /**
     * This method converts a ParkingSession entity to a ParkingSessionDto.
     * I'm ignoring the parkingLotName here because I set it manually in the service
     * to handle cases where the lot might be soft-deleted.
     */
    @Mapping(source = "vehicle.vehicleNumber", target = "vehicleNumber")
    @Mapping(source = "parkingSlotId", target = "parkingSlotId")
    @Mapping(target = "parkingLotName", ignore = true)
    ParkingSessionDto toDto(ParkingSession parkingSession);

    /**
     * This method converts a ParkingSessionDto back to a ParkingSession entity.
     */
    @Mapping(source = "vehicleNumber", target = "vehicle.vehicleNumber")
    @Mapping(source = "parkingSlotId", target = "parkingSlot.id")
    ParkingSession toEntity(ParkingSessionDto parkingSessionDto);
}
