package com.fincons.parkingsystem.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Represents a single parking slot within a parking lot.
 * This entity stores information about the slot's number, its current status (available or occupied),
 * and the parking lot it belongs to.
 */
@Entity
@Table(name = "parking_slots")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE parking_slots SET deleted = true WHERE id=? and version=?")
@Where(clause = "deleted=false")
public class ParkingSlot
{
    /**
     * The unique identifier for the parking slot.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * The number or identifier of the slot within the parking lot.
     */
    @Column(nullable = false)
    private String slotNumber;
    /**
     * The current status of the parking slot (e.g., AVAILABLE, OCCUPIED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;
    /**
     * The parking lot to which this slot belongs.
     */
    @ManyToOne
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Version
    private Long version;

    private boolean deleted = Boolean.FALSE;
}