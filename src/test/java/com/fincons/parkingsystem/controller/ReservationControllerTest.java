package com.fincons.parkingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincons.parkingsystem.dto.ReservationDto;
import com.fincons.parkingsystem.dto.ReservationRequestDto;
import com.fincons.parkingsystem.entity.ReservationStatus;
import com.fincons.parkingsystem.entity.VehicleType;
import com.fincons.parkingsystem.service.ReservationService;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ReservationController}.
 * This class tests the endpoints for creating, canceling, and processing reservations.
 */
@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests the successful creation of a reservation.
     */
    @Test
    void testCreateReservation_Success() throws Exception {
        // Arrange
        ReservationRequestDto requestDto = new ReservationRequestDto("TEST-CAR", VehicleType.CAR, 1L);
        ReservationDto responseDto = ReservationDto.builder()
                .id(1L)
                .vehicleNumber("TEST-CAR")
                .parkingLotId(1L)
                .status(ReservationStatus.ACTIVE)
                .build();
        when(reservationService.createReservation(any(ReservationRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    /**
     * Tests that a validation error on reservation creation results in a 400 Bad Request.
     */
    @Test
    void testCreateReservation_ValidationError() throws Exception {
        // Arrange
        ReservationRequestDto requestDto = new ReservationRequestDto("TEST-CAR", null, 1L); // Null vehicle type

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests the successful cancellation of a reservation.
     */
    @Test
    void testCancelReservation_Success() throws Exception {
        // Arrange
        Long reservationId = 1L;
        doNothing().when(reservationService).cancelReservation(reservationId);

        // Act & Assert
        mockMvc.perform(delete("/api/reservations/{reservationId}", reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservation cancelled successfully."));
    }

    /**
     * Tests the successful retrieval of a paginated list of reservations.
     */
    @Test
    void testGetReservationStatus_Success() throws Exception {
        // Arrange
        List<ReservationDto> reservationsList = Arrays.asList(
                ReservationDto.builder().id(1L).status(ReservationStatus.ACTIVE).build(),
                ReservationDto.builder().id(2L).status(ReservationStatus.COMPLETED).build()
        );
        Page<ReservationDto> reservationPage = new PageImpl<>(reservationsList, PageRequest.of(0, 10), reservationsList.size());
        when(reservationService.getReservationStatus(any(Pageable.class))).thenReturn(reservationPage);

        // Act & Assert
        mockMvc.perform(get("/api/reservations")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    /**
     * Tests the successful processing of a vehicle's arrival for a reservation.
     */
    @Test
    void testProcessArrival_Success() throws Exception {
        // Arrange
        Long reservationId = 1L;
        doNothing().when(reservationService).processArrival(reservationId);

        // Act & Assert
        mockMvc.perform(post("/api/reservations/{reservationId}/arrival", reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle arrival processed successfully."));
    }
}
