package com.fincons.parkingsystem.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Represents a vehicle within the parking system.
 * This entity is mapped to the `vehicles` table and includes a soft-delete mechanism.
 */
@Entity
@Table(name = "vehicles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Overrides the default delete behavior to perform a soft delete.
@SQLDelete(sql = "UPDATE vehicles SET deleted = true WHERE id=?")
// Ensures that all standard find operations automatically filter out records marked as deleted.
@Where(clause = "deleted=false")
public class Vehicle {

    /**
     * The unique identifier for the vehicle, serving as the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The registration number of the vehicle, which must be unique.
     */
    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    /**
     * The type of the vehicle (e.g., CAR, BIKE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    /**
     * A flag indicating whether the entity has been soft-deleted.
     */
    private boolean deleted = Boolean.FALSE;
}
