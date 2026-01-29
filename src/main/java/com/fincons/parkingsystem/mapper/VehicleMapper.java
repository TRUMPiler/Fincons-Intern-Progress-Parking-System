package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.VehicleDto;
import com.fincons.parkingsystem.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps between Vehicle entity and VehicleDto.
 */
@Mapper(componentModel = "spring")
public interface VehicleMapper {

    Vehicle toEntity(VehicleDto vehicleDto);

    VehicleDto toDto(Vehicle vehicle);
}
