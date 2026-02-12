//package com.fincons.parkingsystem.integration;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fincons.parkingsystem.base.AbstractIntegrationTest;
//import com.fincons.parkingsystem.base.RestPage;
//import com.fincons.parkingsystem.dto.ParkingLotDto;
//import com.fincons.parkingsystem.entity.ParkingLot;
//import com.fincons.parkingsystem.entity.ParkingSlot;
//import com.fincons.parkingsystem.entity.Reservation;
//import com.fincons.parkingsystem.entity.ReservationStatus;
//import com.fincons.parkingsystem.entity.SlotStatus;
//import com.fincons.parkingsystem.entity.Vehicle;
//import com.fincons.parkingsystem.entity.VehicleType;
//import com.fincons.parkingsystem.repository.ParkingLotRepository;
//import com.fincons.parkingsystem.repository.ParkingSlotRepository;
//import com.fincons.parkingsystem.repository.ReservationRepository;
//import com.fincons.parkingsystem.repository.VehicleRepository;
//import com.fincons.parkingsystem.utils.Response;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//
//public class ParkingLotControllerIntegrationTest extends AbstractIntegrationTest {
//
//    @Autowired
//    private ParkingLotRepository parkingLotRepository;
//    @Autowired
//    private ParkingSlotRepository parkingSlotRepository;
//    @Autowired
//    private ReservationRepository reservationRepository;
//    @Autowired
//    private VehicleRepository vehicleRepository;
//
//    @BeforeEach
//    void setUp() {
//        reservationRepository.deleteAll();
//        parkingSlotRepository.deleteAll();
//        vehicleRepository.deleteAll();
//        parkingLotRepository.deleteAll();
//    }
//
//    private final String BASE_URL = "/api/parking-lots";
//
//    private ParkingLot createAndSaveLot(String name, int totalSlots, boolean isDeleted) {
//        ParkingLot lot = ParkingLot.builder()
//                .name(name)
//                .totalSlots(totalSlots)
//                .basePricePerHour(10.0)
//                .deleted(isDeleted)
//                .build();
//        return parkingLotRepository.save(lot);
//    }
//
//    @Test
//    void testCreateParkingLot_Success() throws Exception {
//        ParkingLotDto newLotDto = ParkingLotDto.builder()
//                .name("Test Lot Alpha")
//                .location("Test Location")
//                .totalSlots(20)
//                .basePricePerHour(5.0)
//                .build();
//
//        Response<ParkingLotDto> response = performPostRequest(BASE_URL, newLotDto, Response.class, status().isCreated());
//
//        assertThat(response.isSuccess()).isTrue();
//        ParkingLotDto responseData = mapper.convertValue(response.getData(), ParkingLotDto.class);
//        assertThat(responseData.getName()).isEqualTo("Test Lot Alpha");
//        assertThat(parkingLotRepository.count()).isEqualTo(1);
//        assertThat(parkingSlotRepository.count()).isEqualTo(20);
//    }
//
//    @Test
//    void testCreateParkingLot_Failure_NameConflict() throws Exception {
//        createAndSaveLot("Existing Lot", 5, false);
//        ParkingLotDto newLotDto = ParkingLotDto.builder().name("Existing Lot").totalSlots(10).basePricePerHour(3.0).build();
//        Response<String> response = performPostRequest(BASE_URL, newLotDto, Response.class, status().isConflict());
//        assertThat(response.isSuccess()).isFalse();
//        assertThat(response.getMessage()).contains("Parking lot with the same name already exists");
//    }
//
//    @Test
//    void testCreateParkingLot_Failure_Validation() throws Exception {
//        ParkingLotDto newLotDto = ParkingLotDto.builder().name("").totalSlots(10).basePricePerHour(3.0).build();
//        Response<Object> response = performPostRequest(BASE_URL, newLotDto, Response.class, status().isBadRequest());
//        assertThat(response.isSuccess()).isFalse();
//        assertThat(response.getMessage()).isEqualTo("Validation Failed");
//    }
//
//    @Test
//    void testGetAllActiveParkingLots_Success() throws Exception {
//        createAndSaveLot("Active Lot 1", 10, false);
//        createAndSaveLot("Active Lot 2", 5, false);
//        createAndSaveLot("Deleted Lot 1", 15, true);
//
//        Response<RestPage<ParkingLotDto>> response = performGetRequest(BASE_URL, Response.class, status().isOk());
//        RestPage<ParkingLotDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//
//        assertThat(page.getTotalElements()).isEqualTo(2);
//        assertThat(page.getContent()).extracting(ParkingLotDto::getName).containsExactlyInAnyOrder("Active Lot 1", "Active Lot 2");
//    }
//
//    @Test
//    void testGetAllActiveParkingLots_Empty() throws Exception {
//        createAndSaveLot("Deleted Lot 1", 15, true);
//        Response<RestPage<ParkingLotDto>> response = performGetRequest(BASE_URL, Response.class, status().isOk());
//        RestPage<ParkingLotDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isZero();
//    }
//
//    @Test
//    void testGetAllParkingLotsWithInactive_Success() throws Exception {
//        createAndSaveLot("Active Lot 1", 10, false);
//        createAndSaveLot("Deleted Lot 1", 15, true);
//
//        Response<RestPage<ParkingLotDto>> response = performGetRequest(BASE_URL + "/with-inactive", Response.class, status().isOk());
//        RestPage<ParkingLotDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//
//        assertThat(page.getTotalElements()).isEqualTo(9);
////        assertThat(page.getContent()).extracting(ParkingLotDto::getName).containsExactlyInAnyOrder("Active Lot 1", "Deleted Lot 1");
//    }
//
//    @Test
//    void testGetAllParkingLots_NonPaginated_Success() throws Exception {
//        createAndSaveLot("Active Lot 1", 10, false);
//        createAndSaveLot("Active Lot 2", 5, false);
//        createAndSaveLot("Deleted Lot 1", 15, true);
//
//        Response<List<ParkingLotDto>> response = performGetRequest(BASE_URL + "/all", Response.class, status().isOk());
//        List<ParkingLotDto> lots = mapper.convertValue(response.getData(), new TypeReference<>() {});
//
//        assertThat(lots).hasSize(2);
////        assertThat(lots).extracting(ParkingLotDto::getName).containsExactlyInAnyOrder("Deleted Lot 1", "Active Lot 2");
//    }
//
//    @Test
//    void testDeleteParkingLot_Success() throws Exception {
//        ParkingLot lot = createAndSaveLot("To Be Deleted", 1, false);
//        performDeleteRequest(BASE_URL + "/" + lot.getId(), Response.class, status().isOk());
//        ParkingLot deletedLot = parkingLotRepository.findByIdWithInactive(lot.getId()).orElseThrow();
//        assertThat(deletedLot.isDeleted()).isTrue();
//    }
//
//    @Test
//    void testDeleteParkingLot_Failure_NotFound() throws Exception {
//        Response<String> response = performDeleteRequest(BASE_URL + "/999", Response.class, status().isNotFound());
//        assertThat(response.isSuccess()).isFalse();
//    }
//
//    @Test
//    void testDeleteParkingLot_Failure_SlotsOccupied() throws Exception {
//        ParkingLot lot = createAndSaveLot("Busy Lot", 1, false);
//        parkingSlotRepository.save(ParkingSlot.builder().parkingLot(lot).slotNumber("1").status(SlotStatus.OCCUPIED).build());
//        Response<String> response = performDeleteRequest(BASE_URL + "/" + lot.getId(), Response.class, status().isConflict());
//        assertThat(response.getMessage()).contains("Can't delete Parking Lot because slots are occupied");
//    }
//
//    @Test
//    void testDeleteParkingLot_Failure_WithActiveReservation() throws Exception {
//        ParkingLot lot = createAndSaveLot("Reserved Lot", 1, false);
//        ParkingSlot slot = parkingSlotRepository.save(ParkingSlot.builder().parkingLot(lot).slotNumber("1").status(SlotStatus.RESERVED).build());
//        Vehicle vehicle = vehicleRepository.save(Vehicle.builder().vehicleNumber("V1").vehicleType(VehicleType.CAR).build());
//        reservationRepository.save(Reservation.builder().parkingSlot(slot).vehicle(vehicle).status(ReservationStatus.ACTIVE).reservationTime(LocalDateTime.now()).expirationTime(LocalDateTime.now().plusHours(1)).build());
//
//        Response<String> response = performDeleteRequest(BASE_URL + "/" + lot.getId(), Response.class, status().isBadRequest());
//        assertThat(response.getMessage()).contains("Parking Can't be deleted due to active reservation");
//    }
//
//    @Test
//    void testReactivateParkingLot_Success() throws Exception {
//        ParkingLot lot = createAndSaveLot("Deleted Lot", 1, true);
//        performPatchRequest(BASE_URL + "/" + lot.getId() + "/reactivate", null, Response.class, status().isOk());
//        ParkingLot reactivatedLot = parkingLotRepository.findById(lot.getId()).orElseThrow();
//        assertThat(reactivatedLot.isDeleted()).isFalse();
//    }
//
//    @Test
//    void testReactivateParkingLot_Failure_NotFound() throws Exception {
//        Response<String> response = performPatchRequest(BASE_URL + "/999/reactivate", null, Response.class, status().isNotFound());
//        assertThat(response.isSuccess()).isFalse();
//        assertThat(response.getMessage()).contains("Parking lot not found");
//    }
//
//    @Test
//    void testUpdateParkingLot_NotImplemented() throws Exception {
//        ParkingLot lot = createAndSaveLot("Update Lot", 1, false);
//        ParkingLotDto dto = ParkingLotDto.builder().name("New Name").build();
//        performPatchRequest(BASE_URL + "/" + lot.getId(), dto, Void.class, status().isNotImplemented());
//    }
//}
