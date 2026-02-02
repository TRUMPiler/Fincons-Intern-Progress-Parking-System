package com.fincons.parkingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.service.ParkingLotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingLotController.class)
public class ParkingLotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingLotService parkingLotService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateParkingLot_Success() throws Exception {
        // Given
        ParkingLotDto newLotDto = ParkingLotDto.builder().name("Test Lot").location("Test Location").totalSlots(69).basePricePerHour(2.5).build();
        ParkingLotDto createdLotDto = ParkingLotDto.builder().id(1L).name("Test Lot").location("Test Location").totalSlots(50).basePricePerHour(2.5).build();

        when(parkingLotService.createParkingLot(any(ParkingLotDto.class))).thenReturn(createdLotDto);

        // When & Then
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLotDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Test Lot"));
    }

    @Test
    void testGetAllParkingLots_Success() throws Exception {
        // Given
        List<ParkingLotDto> activeLots = Arrays.asList(
                ParkingLotDto.builder().id(1L).name("Lot A").build(),
                ParkingLotDto.builder().id(2L).name("Lot B").build()
        );
        when(parkingLotService.getAllParkingLots()).thenReturn(activeLots);

        // When & Then
        mockMvc.perform(get("/api/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Lot A"));
    }

    @Test
    void testGetAllParkingLotsWithInactiveStatus_Success() throws Exception {
        // Given
        List<ParkingLotDto> allLots = Arrays.asList(
                ParkingLotDto.builder().id(1L).name("Lot A").build(),
                ParkingLotDto.builder().id(2L).name("Lot B").build(),
                ParkingLotDto.builder().id(3L).name("Lot C").build()
        );
        when(parkingLotService.getAllParkingLotsDeleted()).thenReturn(allLots);

        // When & Then
        mockMvc.perform(get("/api/parking-lots/with-inactive")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[2].name").value("Lot C"));
    }

    @Test
    void testDeleteParkingLot_Success() throws Exception {
        // Given
        Long lotId = 1L;
        doNothing().when(parkingLotService).deleteParkingLot(lotId);

        // When & Then
        mockMvc.perform(delete("/api/parking-lots/{id}", lotId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Parking Lot deactivated successfully."));
    }
    @Test
    void testCreateParkingLot_ValidationError_TotalSlotsTooHigh() throws Exception {

        ParkingLotDto invalidDto = ParkingLotDto.builder()
                .name("Test Lot")
                .location("Test Location")
                .totalSlots(100) // ❌ invalid
                .basePricePerHour(2.5)
                .build();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.data.totalSlots")
                        .value("Total slots cannot be more than 70"));
    }

    @Test
    void testReactivateParkingLot_Success() throws Exception {
        // Given
        Long lotId = 1L;
        doNothing().when(parkingLotService).reactivateParkingLot(lotId);

        // When & Then
        mockMvc.perform(patch("/api/parking-lots/{id}/reactivate", lotId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Parking Lot reactivated successfully."));
    }
}
