package com.fincons.parkingsystem.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Represents a vehicle in the parking system.
 * This entity stores information about a vehicle, including its registration number and type.
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
     * The unique identifier for the vehicle.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The registration number of the vehicle. This is a unique identifier for the vehicle.
     */
    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    /**
     * The type of the vehicle (e.g., CAR, BIKE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Version
    private Long version;

    private boolean deleted = Boolean.FALSE;
}
