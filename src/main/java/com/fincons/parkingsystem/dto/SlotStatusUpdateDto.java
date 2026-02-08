package com.fincons.parkingsystem.dto;

import com.fincons.parkingsystem.entity.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for broadcasting real-time updates of a parking slot's status.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotStatusUpdateDto {

    /** The identifier of the parking lot to which the slot belongs. */
    private Long parkingLotId;

    /** The unique identifier of the parking slot that was updated. */
    private Long slotId;

    /** The new status of the parking slot. */
    private SlotStatus newStatus;
}
