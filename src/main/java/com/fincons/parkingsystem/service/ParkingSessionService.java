package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSessionDto;

import java.util.List;

/**
 * Service for retrieving parking session information.
 */
public interface ParkingSessionService {

    /**
     * Retrieves a list of all active parking sessions.
     *
     * @return A list of active parking sessions.
     */
    List<ParkingSessionDto> getActiveSessions();

    /**
     * Retrieves a list of all completed parking sessions.
     *
     * @return A list of completed parking sessions.
     */
    List<ParkingSessionDto> getSessionHistory();
}
