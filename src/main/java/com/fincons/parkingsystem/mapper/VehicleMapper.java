package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.VehicleDto;
import com.fincons.parkingsystem.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This is the mapper for my Vehicle entities.
 * It handles the conversion between the Vehicle entity and its DTO.
 */
@Mapper(componentModel = "spring")
public interface VehicleMapper {

    /**
     * This method converts a VehicleDto to a Vehicle entity.
     */
    Vehicle toEntity(VehicleDto vehicleDto);

    /**
     * This method converts a Vehicle entity to a VehicleDto.
     */
    VehicleDto toDto(Vehicle vehicle);
}
