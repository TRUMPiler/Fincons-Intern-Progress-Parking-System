package com.fincons.parkingsystem.entity;

/**
 * An enumeration representing the possible statuses of a parking slot.
 * This is used to track whether a slot is currently in use or available.
 */
public enum SlotStatus {
    /**
     * Indicates that the parking slot is currently empty and available for a vehicle to park.
     */
    AVAILABLE,
    /**
     * Indicates that the parking slot is currently taken by a vehicle.
     */
    OCCUPIED,
    /**
     * Indicates that the parking slot has been reserved.
     */
    RESERVED
}
