package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingLotStatsDto;
import com.fincons.parkingsystem.service.ParkingLotStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingLotStatsController.class)
public class ParkingLotStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingLotStatsService parkingLotStatsService;

    @Test
    void testGetStats_Success() throws Exception {
        // Given
        Long lotId = 1L;
        ParkingLotStatsDto statsDto = ParkingLotStatsDto.builder()
                .parkingLotId(lotId)
                .parkingLotName("Test Lot")
                .totalSlots(100)
                .occupiedSlots(50L)
                .occupancyPercentage(50.0)
                .totalRevenue(1234.56)
                .build();

        when(parkingLotStatsService.getParkingLotStats(lotId)).thenReturn(statsDto);

        // When & Then
        mockMvc.perform(get("/api/parking-lots/{id}/stats", lotId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.parkingLotId").value(lotId))
                .andExpect(jsonPath("$.data.parkingLotName").value("Test Lot"))
                .andExpect(jsonPath("$.data.occupancyPercentage").value(50.0));
    }
}
