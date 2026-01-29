package com.fincons.parkingsystem.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Represents a vehicle.
 */
@Entity
@Table(name = "vehicles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE vehicles SET deleted = true WHERE id=? and version=?")
@Where(clause = "deleted=false")
public class Vehicle {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Vehicle's registration number (unique).
     */
    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    /**
     * Type of vehicle (e.g., CAR, BIKE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

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
