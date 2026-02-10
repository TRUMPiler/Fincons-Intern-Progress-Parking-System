package com.fincons.parkingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.service.ParkingLotService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ParkingLotController}.
 * This class uses {@link WebMvcTest} to test the controller layer in isolation,
 * with the service layer being mocked.
 */
@WebMvcTest(ParkingLotController.class)
public class ParkingLotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingLotService parkingLotService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests the successful creation of a parking lot via the POST /api/parking-lots endpoint.
     */
    @Test
    void testCreateParkingLot_Success() throws Exception {
        // Arrange
        ParkingLotDto newLotDto = ParkingLotDto.builder().name("Test Lot").location("Test Location").totalSlots(50).basePricePerHour(2.5).build();
        ParkingLotDto createdLotDto = ParkingLotDto.builder().id(1L).name("Test Lot").location("Test Location").totalSlots(50).basePricePerHour(2.5).build();
        when(parkingLotService.createParkingLot(any(ParkingLotDto.class))).thenReturn(createdLotDto);

        // Act & Assert
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLotDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Test Lot"));
    }

    /**
     * Tests the retrieval of a paginated list of active parking lots.
     */
    @Test
    void testGetAllActiveParkingLots_Success() throws Exception {
        // Arrange
        Page<ParkingLotDto> page = new PageImpl<>(Collections.singletonList(new ParkingLotDto()));
        when(parkingLotService.getAllParkingLots(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/parking-lots")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    /**
     * Tests the retrieval of all parking lots, including inactive ones.
     */
    @Test
    void testGetAllParkingLotsWithInactiveStatus_Success() throws Exception {
        // Arrange
        Page<ParkingLotDto> page = new PageImpl<>(Collections.singletonList(new ParkingLotDto()));
        when(parkingLotService.getAllParkingLotsDeleted(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/parking-lots/with-inactive")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    /**
     * Tests the retrieval of a non-paginated list of all active parking lots.
     */
    @Test
    void testGetAllParkingLots_NonPaginated_Success() throws Exception {
        // Arrange
        List<ParkingLotDto> list = Collections.singletonList(new ParkingLotDto());
        when(parkingLotService.findAllParkingLotsNonDeleted()).thenReturn(list);

        // Act & Assert
        mockMvc.perform(get("/api/parking-lots/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    /**
     * Tests the successful deactivation (soft delete) of a parking lot.
     */
    @Test
    void testDeleteParkingLot_Success() throws Exception {
        // Arrange
        doNothing().when(parkingLotService).deleteParkingLot(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/parking-lots/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Parking Lot deactivated successfully."));
    }

    /**
     * Tests the successful reactivation of a soft-deleted parking lot.
     */
    @Test
    void testReactivateParkingLot_Success() throws Exception {
        // Arrange
        doNothing().when(parkingLotService).reactivateParkingLot(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/parking-lots/{id}/reactivate", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Parking Lot reactivated successfully."));
    }

    /**
     * Tests that the update endpoint, which is not implemented, returns the correct HTTP status.
     */
    @Test
    void testUpdateParkingLot_NotImplemented() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/parking-lots/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ParkingLotDto())))
                .andExpect(status().isNotImplemented());
    }

    /**
     * Tests that a validation error (e.g., blank name) results in a 400 Bad Request.
     */
    @Test
    void testCreateParkingLot_ValidationError() throws Exception {
        // Arrange
        ParkingLotDto invalidDto = ParkingLotDto.builder().name("").location("Test Location").totalSlots(50).basePricePerHour(2.5).build();

        // Act & Assert
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
