package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingSessionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for retrieving information about parking sessions.
 * This contract defines how to fetch paginated lists of active and completed sessions.
 */
public interface ParkingSessionService {

    /**
     * Retrieves a paginated list of all parking sessions that are currently active.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs, each representing an active parking session.
     */
    Page<ParkingSessionDto> getActiveSessions(Pageable pageable);

    /**
     * Retrieves a paginated historical list of all parking sessions that have been completed.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs, each representing a completed parking session.
     */
    Page<ParkingSessionDto> getSessionHistory(Pageable pageable);
}
