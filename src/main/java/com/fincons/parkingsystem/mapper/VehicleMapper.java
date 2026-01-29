package com.fincons.parkingsystem.mapper;

import com.fincons.parkingsystem.dto.VehicleDto;
import com.fincons.parkingsystem.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "vehicleNumber", source = "vehicleNumber")
    @Mapping(target = "vehicleType", source = "vehicleType")
    Vehicle toEntity(VehicleDto vehicleDto);

    VehicleDto toDto(Vehicle vehicle);
}
