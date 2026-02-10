package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import com.fincons.parkingsystem.service.ParkingSessionService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ParkingSessionController}.
 * This class tests the endpoints for retrieving active and historical parking sessions.
 */
@WebMvcTest(ParkingSessionController.class)
public class ParkingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingSessionService parkingSessionService;

    /**
     * Tests the successful retrieval of a paginated list of active parking sessions.
     */
    @Test
    void testGetActiveSessions_Success() throws Exception {
        // Arrange
        Page<ParkingSessionDto> page = new PageImpl<>(Collections.singletonList(new ParkingSessionDto()));
        when(parkingSessionService.getActiveSessions(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/sessions/active")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    /**
     * Tests the successful retrieval of a paginated history of all parking sessions.
     */
    @Test
    void testGetSessionHistory_Success() throws Exception {
        // Arrange
        Page<ParkingSessionDto> page = new PageImpl<>(Collections.singletonList(new ParkingSessionDto()));
        when(parkingSessionService.getSessionHistory(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/sessions/history")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
