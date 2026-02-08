package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This is the mapper for my Reservation entities.
 * It handles the conversion between the Reservation entity and its DTO.
 */
@Mapper(componentModel = "spring")
public interface ReservationMapper {

    /**
     * This method converts a Reservation entity to a ReservationDto.
     * I'm ignoring the parkingLotName here because I set it manually in the service
     * to handle cases where the lot might be soft-deleted.
     */
    @Mapping(source = "vehicle.vehicleNumber", target = "vehicleNumber")

    @Mapping(source = "parkingSlotId", target = "parkingSlotId")
    @Mapping(target = "parkingLotName", ignore = true)
    ReservationDto toDto(Reservation reservation);

    /**
     * This method converts a ReservationDto back to a Reservation entity.
     */
    @Mapping(source = "vehicleNumber", target = "vehicle.vehicleNumber")
    @Mapping(source = "parkingSlotId", target = "parkingSlotId")
    Reservation toEntity(ReservationDto reservationDto);
}
