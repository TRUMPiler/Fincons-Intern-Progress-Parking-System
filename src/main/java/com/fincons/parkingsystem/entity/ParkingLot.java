package com.fincons.parkingsystem.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.Instant;
import java.util.List;

/**
 * Represents a parking lot within the system.
 * This entity is mapped to the `parking_lots` table and incorporates a soft-delete mechanism
 * through the @SQLDelete and @Where annotations.
 */
@Entity
@Table(name = "parking_lots")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Overrides the default delete behavior to perform a soft delete by setting the 'deleted' flag to true.
@SQLDelete(sql = "UPDATE parking_lots SET deleted = true, version = version + 1 WHERE id = ? AND version = ?")
// Ensures that all standard find operations automatically filter out records marked as deleted.
@Where(clause = "deleted=false")
public class ParkingLot
{
    /**
     * The unique identifier for the parking lot, serving as the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the parking lot. This field is mandatory.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The physical address or location of the parking lot.
     */
    private String location;

    /**
     * The total number of parking slots available in this lot.
     */
    @Column(nullable = false)
    private Integer totalSlots;

    /**
     * The base hourly rate for parking in this lot.
     */
    @Column(nullable = false)
    private Double basePricePerHour;

    /**
     * The timestamp recorded when the parking lot was first created.
     */
    private Instant createdAt;

    /**
     * A list of all parking slots associated with this lot.
     * The `CascadeType.ALL` ensures that operations (e.g., delete) on the lot are propagated to its slots.
     */
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingSlot> parkingSlots;

    /**
     * A flag indicating whether the entity has been soft-deleted.
     */
    private boolean deleted = Boolean.FALSE;

    /**
     * A version field managed by JPA for optimistic locking, used to prevent concurrent update conflicts.
     */
    @Version
    private int version;

    /**
     * A JPA callback method that automatically sets the `createdAt` timestamp
     * before the entity is first persisted.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant();
    }
}
