package com.fincons.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a parking lot.
 */
@Entity
@Table(name = "parking_lots")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE parking_lots SET deleted = true WHERE id=? and version=?")
@Where(clause = "deleted=false")
public class ParkingLot {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the parking lot.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Physical location.
     */
    private String location;

    /**
     * Total number of slots.
     */
    @Column(nullable = false)
    private Integer totalSlots;

    /**
     * Base price per hour.
     */
    @Column(nullable = false)
    private Double basePricePerHour;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * List of parking slots in this lot.
     */
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingSlot> parkingSlots;

    /**
     * Version number for optimistic locking.
     */
    @Version
    private Long version;

    /**
     * Flag for soft deletion.
     */
    private boolean deleted = Boolean.FALSE;

    /**
     * Sets the creation timestamp automatically before the entity is first saved.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
