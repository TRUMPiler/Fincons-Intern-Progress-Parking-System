package com.fincons.parkingsystem.service;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing parking lot resources.
 * This contract defines the business logic for CRUD operations and other administrative actions
 * related to parking lots.
 */
public interface ParkingLotService {

    /**
     * Creates a new parking lot based on the provided data.
     *
     * @param parkingLotDto A DTO containing the details for the new parking lot.
     * @return The DTO of the newly created parking lot, including its generated ID.
     */
    ParkingLotDto createParkingLot(ParkingLotDto parkingLotDto);

    /**
     * Retrieves a paginated list of all active (not soft-deleted) parking lots.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs representing all active parking lots.
     */
    Page<ParkingLotDto> getAllParkingLots(Pageable pageable);

    /**
     * Retrieves a paginated list of all parking lots, including those that have been soft-deleted.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of DTOs representing all parking lots, both active and inactive.
     */
    Page<ParkingLotDto> getAllParkingLotsDeleted(Pageable pageable);

    /**
     * Deactivates a parking lot using a soft-delete mechanism.
     *
     * @param id The unique identifier of the parking lot to be deactivated.
     */
    void deleteParkingLot(Long id);

    /**
     * Reactivates a soft-deleted parking lot.
     *
     * @param id The unique identifier of the parking lot to be reactivated.
     */
    void reactivateParkingLot(Long id);

    /**
     * Updates the details of an existing parking lot.
     *
     * @param id The unique identifier of the parking lot to be updated.
     * @param parkingLotDto A DTO containing the new information for the parking lot.
     */
    void updateParkingLot(Long id, ParkingLotDto parkingLotDto);

    /**
     * Retrieves a non-paginated list of all active (non-deleted) parking lots.
     *
     * @return A list of DTOs representing all active parking lots.
     */
    List<ParkingLotDto> findAllParkingLotsNonDeleted();
}
