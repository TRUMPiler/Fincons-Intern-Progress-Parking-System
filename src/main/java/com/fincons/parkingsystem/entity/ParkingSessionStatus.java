package com.fincons.parkingsystem.entity;

/**
 * Represents the status of a parking session.
 * A session can be either active (ongoing) or completed (finished).
 */
public enum ParkingSessionStatus {
    /**
     * The parking session is currently active.
     */
    ACTIVE,
    /**
     * The parking session has been completed.
     */
    COMPLETED
}
