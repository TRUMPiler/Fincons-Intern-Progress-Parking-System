package com.fincons.parkingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.dto.VehicleDto;
import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;
import com.fincons.parkingsystem.entity.ParkingSessionStatus;
import com.fincons.parkingsystem.entity.VehicleType;
import com.fincons.parkingsystem.service.ParkingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingController.class)
public class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingService parkingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testVehicleEntry_Success() throws Exception {
        // Given
        VehicleEntryRequestDto entryRequest = new VehicleEntryRequestDto("TEST1234", VehicleType.CAR, 1L);
        ParkingSessionDto sessionDto = ParkingSessionDto.builder()
                .id(1L)
                .vehicleNumber("TEST1234")
                .parkingSlotId(10L)
                .entryTime(LocalDateTime.now())
                .status(ParkingSessionStatus.ACTIVE)
                .build();

        when(parkingService.enterVehicle(any(VehicleEntryRequestDto.class))).thenReturn(sessionDto);

        // When & Then
        mockMvc.perform(post("/api/parking/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Parking session initiated for this vehicle."))
                .andExpect(jsonPath("$.data.vehicleNumber").value("TEST1234"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void testVehicleExit_Success() throws Exception {
        // Given
        VehicleDto vehicleDto = new VehicleDto(0L, "TEST1234", VehicleType.CAR);
        ParkingSessionDto sessionDto = ParkingSessionDto.builder()
                .id(1L)
                .vehicleNumber("TEST1234")
                .parkingSlotId(10L)
                .entryTime(LocalDateTime.now().minusHours(2))
                .exitTime(LocalDateTime.now())
                .status(ParkingSessionStatus.COMPLETED)
                .totalAmount(10.0)
                .build();

        when(parkingService.exitVehicle(any(String.class))).thenReturn(sessionDto);

        // When & Then
        mockMvc.perform(post("/api/parking/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Parking session completed."))
                .andExpect(jsonPath("$.data.vehicleNumber").value("TEST1234"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.totalAmount").value(10.0));
    }
}
