package com.fincons.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Represents a reservation for a parking spot, holding it for a limited time.
 * This entity is mapped to the `reservations` table and includes a soft-delete mechanism.
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Overrides the default delete behavior to perform a soft delete.
@SQLDelete(sql = "UPDATE reservations SET deleted = true, version = version + 1 WHERE id = ? AND version = ?")
// Ensures that all standard find operations automatically filter out records marked as deleted.
@Where(clause = "deleted=false")
public class Reservation {

    /**
     * The unique identifier for the reservation, serving as the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The vehicle for which the reservation is made.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * The parking lot where the reservation is made.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    /**
     * A direct mapping to the `parking_lot_id` foreign key column. This allows safe access
     * to the ID without triggering a lazy-loading exception, which is crucial when the
     * associated ParkingLot may have been soft-deleted.
     */
    @Column(name = "parking_lot_id", insertable = false, updatable = false)
    private Long parkingLotId;

    /**
     * The timestamp recorded when the reservation was created.
     */
    @Column(nullable = false)
    private LocalDateTime reservationTime;

    /**
     * The timestamp indicating when the reservation will automatically expire if not claimed.
     */
    @Column(nullable = false)
    private LocalDateTime expirationTime;

    /**
     * The current status of the reservation (e.g., ACTIVE, COMPLETED, CANCELLED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    /**
     * A flag indicating whether the entity has been soft-deleted.
     */
    private boolean deleted = Boolean.FALSE;

    /**
     * A version field managed by JPA for optimistic locking, used to handle concurrent
     * modifications to a reservation's status.
     */
    @Version
    private int version;
}
