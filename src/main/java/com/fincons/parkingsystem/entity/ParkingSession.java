package com.fincons.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Represents a single parking session for a vehicle from entry to exit.
 * This entity is mapped to the `parking_sessions` table and includes a soft-delete mechanism.
 */
@Entity
@Table(name = "parking_sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Overrides the default delete behavior to perform a soft delete.
@SQLDelete(sql = "UPDATE parking_sessions SET deleted = true, version = version + 1 WHERE id = ? AND version = ?")
// Ensures that all standard find operations automatically filter out records marked as deleted.
@Where(clause = "deleted=false")
public class ParkingSession {
    /**
     * The unique identifier for the parking session, serving as the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The vehicle associated with this parking session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * The parking slot occupied during this session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_slot_id", nullable = false)
    private ParkingSlot parkingSlot;

    /**
     * A direct mapping to the `parking_slot_id` foreign key column. This allows safe access
     * to the ID without triggering a lazy-loading exception, which is crucial when the
     * associated ParkingSlot may have been soft-deleted.
     */
    @Column(name = "parking_slot_id", insertable = false, updatable = false)
    private Long parkingSlotId;

    /**
     * The timestamp recorded when the vehicle entered the parking lot.
     */
    @Column(nullable = false)
    private LocalDateTime entryTime;

    /**
     * The timestamp recorded when the vehicle exited the parking lot.
     * This is null for active sessions.
     */
    private LocalDateTime exitTime;

    /**
     * The total amount charged for the parking session, calculated upon exit.
     */
    private Double totalAmount;

    /**
     * The current status of the parking session (e.g., ACTIVE, COMPLETED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSessionStatus status;

    /**
     * A flag indicating whether the entity has been soft-deleted.
     */
    private boolean deleted = Boolean.FALSE;

    /**
     * A version field managed by JPA for optimistic locking.
     */
    @Version
    private int version;

    /**
     * A JPA callback method that automatically sets the `exitTime` timestamp
     * just before the entity is updated.
     */
    @PreUpdate
    public void onPreUpdate() {
        this.exitTime = LocalDateTime.now();
    }

}
