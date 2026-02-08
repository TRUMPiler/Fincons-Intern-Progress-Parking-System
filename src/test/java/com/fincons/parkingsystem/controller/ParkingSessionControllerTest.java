//package com.fincons.parkingsystem.controller;
//
//import com.fincons.parkingsystem.dto.ParkingSessionDto;
//import com.fincons.parkingsystem.entity.ParkingSessionStatus;
//import com.fincons.parkingsystem.service.ParkingSessionService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ParkingSessionController.class)
//public class ParkingSessionControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ParkingSessionService parkingSessionService;
//
//    @Test
//    void testGetActiveSessions_Success() throws Exception {
//        // Given
//        List<ParkingSessionDto> activeSessions = Arrays.asList(
//                ParkingSessionDto.builder().id(1L).vehicleNumber("CAR-001").status(ParkingSessionStatus.ACTIVE).build(),
//                ParkingSessionDto.builder().id(2L).vehicleNumber("CAR-002").status(ParkingSessionStatus.ACTIVE).build()
//        );
//        when(parkingSessionService.getActiveSessions()).thenReturn(activeSessions);
//
//        // When & Then
//        mockMvc.perform(get("/api/sessions/active")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.length()").value(2))
//                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
//    }
//
//    @Test
//    void testGetSessionHistory_Success() throws Exception {
//        // Given
//        List<ParkingSessionDto> sessionHistory = Arrays.asList(
//                ParkingSessionDto.builder().id(3L).vehicleNumber("CAR-003").status(ParkingSessionStatus.COMPLETED).build(),
//                ParkingSessionDto.builder().id(4L).vehicleNumber("CAR-004").status(ParkingSessionStatus.COMPLETED).build()
//        );
//        when(parkingSessionService.getSessionHistory()).thenReturn(sessionHistory);
//
//        // When & Then
//        mockMvc.perform(get("/api/sessions/history")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.length()").value(2))
//                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"));
//    }
//}
