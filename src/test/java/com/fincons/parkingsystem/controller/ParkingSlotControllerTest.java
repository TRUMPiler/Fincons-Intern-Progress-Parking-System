package com.fincons.parkingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincons.parkingsystem.dto.ParkingSlotAvailability;
import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.service.ParkingSlotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingSlotController.class)
public class ParkingSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingSlotService parkingSlotService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetSlotsByParkingLot_Success() throws Exception {
        // Given
        Long lotId = 1L;
        List<ParkingSlotDto> slots = Arrays.asList(
                ParkingSlotDto.builder().id(1L).slotNumber("A1").status(SlotStatus.AVAILABLE).build(),
                ParkingSlotDto.builder().id(2L).slotNumber("A2").status(SlotStatus.OCCUPIED).build()
        );
        ParkingSlotAvailability availability = new ParkingSlotAvailability(slots, 1L);

        when(parkingSlotService.getParkingSlotAvailability(lotId)).thenReturn(availability);

        // When & Then
        mockMvc.perform(get("/api/parking-lots/{parkingLotId}/slots", lotId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableSlots").value(1))
                .andExpect(jsonPath("$.data.parkingSlots.length()").value(2));
    }

    @Test
    void testUpdateParkingSlot_Success() throws Exception {
        // Given
        ParkingSlotDto requestDto = ParkingSlotDto.builder().id(1L).status(SlotStatus.OCCUPIED).build();
        ParkingSlotDto updatedDto = ParkingSlotDto.builder().id(1L).slotNumber("A1").status(SlotStatus.OCCUPIED).build();

        when(parkingSlotService.updateParkingSlotInformation(any(ParkingSlotDto.class))).thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(patch("/api/parking-lots/update-slot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("OCCUPIED"));
    }
}
