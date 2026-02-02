package com.fincons.parkingsystem.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Represents a single parking slot within a parking lot.
 * This entity is mapped to the `parking_slots` table and includes a soft-delete mechanism.
 * It tracks the status of an individual slot and its association with a parking lot.
 */
@Entity
@Table(name = "parking_slots")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE parking_slots SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
public class ParkingSlot
{
    /**
     * The unique identifier for the parking slot.
     * This is the primary key and is auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * The number or identifier of the slot within the parking lot (e.g., "A1", "B2").
     * This is a mandatory field.
     */
    @Column(nullable = false)
    private String slotNumber;
    /**
     * The current status of the slot, such as AVAILABLE or OCCUPIED.
     * This is an enumerated type and is stored as a string.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;
    /**
     * The parking lot to which this slot belongs.
     * This is a many-to-one relationship, linking the slot to its parent lot.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Column(name = "parking_lot_id", insertable = false, updatable = false)
    private Long parkingLotId;
    /**
     * A flag to indicate whether the parking slot has been soft-deleted.
     * By default, this is `false`.
     */
    private boolean deleted = Boolean.FALSE;
}
