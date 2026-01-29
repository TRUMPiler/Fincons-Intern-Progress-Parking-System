package com.fincons.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingLotStatsDto {

    private  Long parkingLotId;
    private String parkingLotName;
    private Integer totalSlots;
    private Long occupiedSlots;
    private Long activeSessions;
    private Double RevenueToday;
    private Double OccupancyPercentage;

}
