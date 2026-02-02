package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSessionDto;

import java.util.List;

/**
 * This is the contract for my service that retrieves information about parking sessions.
 * It defines how I can get lists of active and completed sessions.
 */
public interface ParkingSessionService {

    /**
     * This method gets a list of all the parking sessions that are currently active.
     *
     * @return A list of DTOs for the active sessions.
     */
    List<ParkingSessionDto> getActiveSessions();

    /**
     * This method retrieves the history of all completed parking sessions.
     *
     * @return A list of DTOs for the completed sessions.
     */
    List<ParkingSessionDto> getSessionHistory();
}
