package com.fincons.parkingsystem.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Represents a single, physical parking slot within a parking lot.
 * This entity is mapped to the `parking_slots` table and includes a soft-delete mechanism.
 */
@Entity
@Table(name = "parking_slots")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Overrides the default delete behavior to perform a soft delete.
@SQLDelete(sql = "UPDATE parking_slots SET deleted = true, version = version + 1 WHERE id = ? AND version = ?")
// Ensures that all standard find operations automatically filter out records marked as deleted.
@Where(clause = "deleted=false")
public class ParkingSlot
{
    /**
     * The unique identifier for the parking slot, serving as the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique identifier or number of the slot within its lot (e.g., "A1", "B2").
     */
    @Column(nullable = false)
    private String slotNumber;

    /**
     * The current status of the slot (e.g., AVAILABLE, OCCUPIED, RESERVED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;

    /**
     * The parking lot to which this slot belongs.
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
     * A flag indicating whether the entity has been soft-deleted.
     */
    private boolean deleted = Boolean.FALSE;

    /**
     * A version field managed by JPA for optimistic locking, critical for preventing
     * race conditions when multiple operations try to claim the same slot.
     */
    @Version
    private int version;
}
