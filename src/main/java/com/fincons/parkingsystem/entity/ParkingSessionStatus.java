package com.fincons.parkingsystem.entity;

/**
 * An enumeration representing the possible statuses of a parking session.
 * This is used to track the lifecycle of a parking session from start to finish.
 */
public enum ParkingSessionStatus {
    /**
     * Indicates that the parking session is currently ongoing.
     * The vehicle is still parked in the lot.
     */
    ACTIVE,
    /**
     * Indicates that the parking session has finished.
     * The vehicle has exited the lot and the final charges have been calculated.
     */
    COMPLETED
}
