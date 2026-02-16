package com.fincons.parkingsystem.dto;

import com.fincons.parkingsystem.entity.ReservationStatus;
import lombok.*;

import java.time.Instant;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdate {

    public ReservationUpdate(ReservationDto reservationDto)
    {
        this.id=reservationDto.getId();
        this.vehicleNumber=reservationDto.getVehicleNumber();
        this.parkingSlotId=reservationDto.getParkingSlotId();
        this.parkingLotId=reservationDto.getParkingLotId();
        this.parkingLotName=reservationDto.getParkingLotName();
        this.reservationTime=reservationDto.getReservationTime();
        this.expirationTime=reservationDto.getExpirationTime();
        this.status=reservationDto.getStatus();

    }

    /** The unique identifier of the reservation. */
    private Long id;

    /** The registration number of the vehicle for which the reservation was made. */
    private String vehicleNumber;

    /** The identifier of the parking slot where the reservation is valid. */
    private Long parkingSlotId;

    /** The identifier of the parking lot where the reservation is valid. */
    private Long parkingLotId;

    /** The name of the parking lot where the reservation is valid. */
    private String parkingLotName;

    /** The timestamp when the reservation was created. */
    private Instant reservationTime;

    /** The timestamp when the reservation will expire if not claimed. */
    private Instant expirationTime;

    /** The current status of the reservation (e.g., ACTIVE, COMPLETED, CANCELLED). */
    private ReservationStatus status;
}
