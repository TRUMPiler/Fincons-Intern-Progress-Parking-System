package com.fincons.parkingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincons.parkingsystem.dto.ParkingSlotDto;
import com.fincons.parkingsystem.entity.SlotStatus;
import com.fincons.parkingsystem.service.ParkingSlotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ParkingSlotController}.
 * This class tests the controller endpoints for retrieving and updating parking slots,
 * using a mocked service layer.
 */
@WebMvcTest(ParkingSlotController.class)
public class ParkingSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingSlotService parkingSlotService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests the successful retrieval of a paginated list of slots for a specific parking lot.
     */
    @Test
    void testGetSlotsByParkingLot_Success() throws Exception {
        // Arrange
        Long lotId = 1L;
        Page<ParkingSlotDto> page = new PageImpl<>(Collections.singletonList(new ParkingSlotDto()));
        when(parkingSlotService.getParkingSlotAvailability(eq(lotId), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/parking-slots/by-lot/{parkingLotId}", lotId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    /**
     * Tests the successful update of a parking slot's status.
     */
    @Test
    void testUpdateParkingSlot_Success() throws Exception {
        // Arrange
        ParkingSlotDto requestDto = ParkingSlotDto.builder().id(1L).status(SlotStatus.OCCUPIED).build();
        ParkingSlotDto updatedDto = ParkingSlotDto.builder().id(1L).slotNumber("A1").status(SlotStatus.OCCUPIED).build();
        when(parkingSlotService.updateParkingSlotInformation(any(ParkingSlotDto.class))).thenReturn(updatedDto);

        // Act & Assert
        mockMvc.perform(patch("/api/parking-slots/update-slot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("OCCUPIED"));
    }
}
