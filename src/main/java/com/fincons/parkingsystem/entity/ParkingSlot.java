package com.fincons.parkingsystem.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Represents a single parking slot in a parking lot.
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
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Number of the slot within the lot.
     */
    @Column(nullable = false)
    private String slotNumber;
    /**
     * Current status (e.g., AVAILABLE, OCCUPIED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;
    /**
     * The parking lot this slot belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;
    /**
     * Version number for optimistic locking.
     */
    @Version
    private Long version;
    /**
     * Flag for soft deletion.
     */
    private boolean deleted = Boolean.FALSE;
}
