package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.dto.ParkingLotStatsDto;

public interface ParkingLotStatsService
{
    ParkingLotStatsDto getParkingLotStats(Long id );
}
