package com.fincons.parkingsystem.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This class represents a parking lot in my system.
 * It's an entity, so it maps to the `parking_lots` table in my database.
 */
@Entity
@Table(name = "parking_lots")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// I'm using @SQLDelete to implement a soft-delete. When I call delete(), it will run this UPDATE statement instead.
@SQLDelete(sql = "UPDATE parking_lots SET deleted = true WHERE id=?")
// The @Where clause makes sure that my queries only return records that haven't been soft-deleted.
@Where(clause = "deleted=false")
public class ParkingLot
{
    /**
     * This is the primary key for the parking lot.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the parking lot.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The location of the parking lot.
     */
    private String location;

    /**
     * The total number of slots in this lot.
     */
    @Column(nullable = false)
    private Integer totalSlots;

    /**
     * The base price per hour for parking here.
     */
    @Column(nullable = false)
    private Double basePricePerHour;

    /**
     * The timestamp for when this lot was created.
     */
    private LocalDateTime createdAt;

    /**
     * This is the list of all the parking slots that belong to this lot.
     * I've set it to cascade all operations, so if I delete a lot, its slots are deleted too.
     */
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingSlot> parkingSlots;

    /**
     * This flag indicates if the lot has been soft-deleted.
     */
    private boolean deleted = Boolean.FALSE;

    /**
     * This method automatically sets the `createdAt` timestamp before the entity is first saved.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
