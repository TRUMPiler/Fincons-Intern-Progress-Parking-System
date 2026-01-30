package com.fincons.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Represents a single parking session for a vehicle.
 */
@Entity
@Table(name = "parking_sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE parking_sessions SET deleted = true WHERE id=? and version=?")
@Where(clause = "deleted=false")
public class ParkingSession {
    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * The vehicle parked in this session.
     */
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    /**
     * The slot used for this session.
     */
    @ManyToOne
    @JoinColumn(name = "parking_slot_id", nullable = false)
    private ParkingSlot parkingSlot;
    /**
     * Entry timestamp.
     */
    @Column(nullable = false)
    private LocalDateTime entryTime;
    /**
     * Exit timestamp.
     */

    private LocalDateTime exitTime;
    /**
     * Total charge for the session.
     */
    private Double totalAmount;
    /**
     * Current status (e.g., ACTIVE, COMPLETED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSessionStatus status;
    /**
     * Version number for optimistic locking.
     */
    @Version
    private Long version;
    /**
     * Flag for soft deletion.
     */
    private boolean deleted = Boolean.FALSE;

    @PreUpdate
    public void onPreUpdate() {
        this.exitTime = LocalDateTime.now();
    }

}
