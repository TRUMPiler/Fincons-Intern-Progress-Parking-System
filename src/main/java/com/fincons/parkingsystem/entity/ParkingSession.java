package com.fincons.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * This class represents a single parking session for a vehicle.
 * It's an entity, so it maps to the `parking_sessions` table in my database.
 */
@Entity
@Table(name = "parking_sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// I'm using @SQLDelete to implement a soft-delete.
@SQLDelete(sql = "UPDATE parking_sessions SET deleted = true WHERE id=?")
// The @Where clause makes sure that my queries only return records that haven't been soft-deleted.
@Where(clause = "deleted=false")
public class ParkingSession {
    /**
     * This is the primary key for the parking session.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * This links the session to a vehicle.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * This links the session to a specific parking slot.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_slot_id", nullable = false)
    private ParkingSlot parkingSlot;

    /**
     * I added this field to safely access the parking slot's ID without causing lazy-loading issues,
     * especially if the slot has been soft-deleted.
     */
    @Column(name = "parking_slot_id", insertable = false, updatable = false)
    private Long parkingSlotId;

    /**
     * The time the vehicle entered the lot.
     */
    @Column(nullable = false)
    private LocalDateTime entryTime;

    /**
     * The time the vehicle exited the lot.
     */
    private LocalDateTime exitTime;

    /**
     * The total amount charged for the session.
     */
    private Double totalAmount;

    /**
     * The status of the session (e.g., ACTIVE, COMPLETED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSessionStatus status;

    /**
     * This flag indicates if the session has been soft-deleted.
     */
    private boolean deleted = Boolean.FALSE;

    /**
     * This method automatically sets the `exitTime` timestamp when the session is updated.
     */
    @PreUpdate
    public void onPreUpdate() {
        this.exitTime = LocalDateTime.now();
    }

}
