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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateReservation_Success() throws Exception {
        // Given
        ReservationRequestDto requestDto = new ReservationRequestDto("TEST-CAR", VehicleType.CAR, 1L);
        ReservationDto responseDto = ReservationDto.builder()
                .id(1L)
                .vehicleNumber("TEST-CAR")
                .parkingLotId(1L)
                .status(ReservationStatus.ACTIVE)
                .build();

        when(reservationService.createReservation(any(ReservationRequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void testCancelReservation_Success() throws Exception {
        // Given
        Long reservationId = 1L;
        doNothing().when(reservationService).cancelReservation(reservationId);

        // When & Then
        mockMvc.perform(delete("/api/reservations/{reservationId}", reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reservation cancelled successfully."));
    }

    @Test
    void testGetReservationStatus_Success() throws Exception {
        // Given
        List<ReservationDto> reservations = Arrays.asList(
                ReservationDto.builder().id(1L).status(ReservationStatus.ACTIVE).build(),
                ReservationDto.builder().id(2L).status(ReservationStatus.COMPLETED).build()
        );
        when(reservationService.getReservationStatus()).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testProcessArrival_Success() throws Exception {
        // Given
        Long reservationId = 1L;
        doNothing().when(reservationService).processArrival(reservationId);

        // When & Then
        mockMvc.perform(post("/api/reservations/{reservationId}/arrival", reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle arrival processed successfully."));
    }
}
