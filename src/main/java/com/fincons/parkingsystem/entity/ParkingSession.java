package com.fincons.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Represents a parking session for a vehicle in a parking slot.
 * This entity tracks the details of a single parking event, including the vehicle, the slot used,
 * entry and exit times, the total cost, and the session's status.
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
     * The unique identifier for the parking session.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * The vehicle associated with this parking session.
     */
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    /**
     * The parking slot used for this session. A slot can have many sessions over time.
     */
    @ManyToOne
    @JoinColumn(name = "parking_slot_id", nullable = false)
    private ParkingSlot parkingSlot;
    /**
     * The time the vehicle entered the parking lot.
     */
    @Column(nullable = false)
    private LocalDateTime entryTime;
    /**
     * The time the vehicle exited the parking lot.
     */
    private LocalDateTime exitTime;


    /**
     * The total amount charged for the parking session.
     */

    private Double totalAmount;
    /**
     * The current status of the parking session (e.g., ACTIVE, COMPLETED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSessionStatus status;

    @Version
    private Long version;

    private boolean deleted = Boolean.FALSE;


}
