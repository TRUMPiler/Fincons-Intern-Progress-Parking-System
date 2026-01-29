package com.fincons.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a parking lot in the system.
 * This entity stores information about a specific parking lot, including its name, location,
 * capacity, and pricing information.
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
     * The unique identifier for the parking lot.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the parking lot. Must not be null.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The physical location or address of the parking lot.
     */
    private String location;

    /**
     * The total number of parking slots available in the lot.
     */
    @Column(nullable = false)
    private Integer totalSlots;

    /**
     * The base price for parking per hour.
     */
    @Column(nullable = false)
    private Double basePricePerHour;

    /**
     * The timestamp when the parking lot was created in the system.
     * This is automatically set when the entity is first persisted.
     */
    private LocalDateTime createdAt;

    /**
     * The list of parking slots associated with this parking lot.
     */
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingSlot> parkingSlots;

    @Version
    private Long version;

    private boolean deleted = Boolean.FALSE;

    /**
     * Sets the creation timestamp before the entity is persisted.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
