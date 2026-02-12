//package com.fincons.parkingsystem.integration;
//
//import com.fincons.parkingsystem.base.AbstractIntegrationTest;
//import com.fincons.parkingsystem.base.RestPage;
//import com.fincons.parkingsystem.dto.ParkingLotDto;
//import com.fincons.parkingsystem.dto.ParkingSlotDto;
//import com.fincons.parkingsystem.entity.SlotStatus;
//import com.fincons.parkingsystem.mapper.ParkingSlotMapper;
//import com.fincons.parkingsystem.repository.ParkingLotRepository;
//import com.fincons.parkingsystem.repository.ParkingSessionRepository;
//import com.fincons.parkingsystem.repository.ParkingSlotRepository;
//import com.fincons.parkingsystem.repository.VehicleRepository;
//import com.fincons.parkingsystem.service.ParkingLotService;
//import com.fincons.parkingsystem.utils.Response;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//public class ParkingSlotControllerIntegrationTest extends AbstractIntegrationTest {
//
//    @Autowired
//    private ParkingLotService parkingLotService;
//
//    @Autowired
//    private ParkingLotRepository parkingLotRepository;
//    @Autowired
//    private ParkingSlotRepository parkingSlotRepository;
//    @Autowired
//    private ParkingSessionRepository parkingSessionRepository;
//    @Autowired
//    private VehicleRepository vehicleRepository;
//
//    @Autowired
//    private ParkingSlotMapper parkingSlotMapper;
//
//    private ParkingLotDto parkingLot;
//
//    @BeforeEach
//    void setUp() {
//        parkingSessionRepository.deleteAll();
//        parkingSlotRepository.deleteAll();
//        vehicleRepository.deleteAll();
//        parkingLotRepository.deleteAll();
//
//        parkingLot = parkingLotService.createParkingLot(ParkingLotDto.builder()
//                .name("Main Lot")
//                .totalSlots(5)
//                .basePricePerHour(10.0)
//                .build());
//    }
//
//    private final String BASE_URL = "/api/parking-slots";
//
//    @Test
//    void testGetSlotsByParkingLot_Success() throws Exception {
//        Response<RestPage<ParkingSlotDto>> response = performGetRequest(BASE_URL + "/by-lot/" + parkingLot.getId(), Response.class, status().isOk());
//
//        assertThat(response.isSuccess()).isTrue();
//        RestPage<ParkingSlotDto> page = mapper.convertValue(response.getData(), RestPage.class);
//        assertThat(page.getTotalElements()).isEqualTo(5);
//    }
//
//    @Test
//    void testGetSlotsByParkingLot_Failure_NotFound() throws Exception {
//        performGetRequest(BASE_URL + "/by-lot/999", Response.class, status().isNotFound());
//    }
//
//    @Test
//    void testUpdateParkingSlot_Success() throws Exception {
//        // Arrange
//        ParkingSlotDto slotToUpdate = parkingSlotRepository.findByParkingLotId(parkingLot.getId()).stream().map(parkingSlotMapper::toDto).findFirst().get();
//        slotToUpdate.setStatus(SlotStatus.UNDER_SERVICE);
//
//        // Act
//        Response<ParkingSlotDto> response = performPatchRequest(BASE_URL + "/update-slot", slotToUpdate, Response.class, status().isOk());
//
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        ParkingSlotDto updatedSlot = mapper.convertValue(response.getData(), ParkingSlotDto.class);
//        assertThat(updatedSlot.getStatus()).isEqualTo(SlotStatus.UNDER_SERVICE);
//    }
//
//    @Test
//    void testUpdateParkingSlot_Failure_NotFound() throws Exception {
//        // Arrange
//        ParkingSlotDto nonExistentSlot = new ParkingSlotDto(999L, "999", SlotStatus.AVAILABLE, 999L);
//
//        // Act & Assert
//        performPatchRequest(BASE_URL + "/update-slot", nonExistentSlot, Response.class, status().isNotFound());
//    }
//
//    @Test
//    void testUpdateParkingSlot_Failure_Validation() throws Exception {
//        // Arrange
//        ParkingSlotDto invalidSlot = parkingSlotRepository.findByParkingLotId(parkingLot.getId()).stream().map(parkingSlotMapper::toDto).findFirst().get();
//        invalidSlot.setStatus(null); // Make the DTO invalid
//        invalidSlot.setId(0L);
//
//        // Act & Assert
//        performPatchRequest(BASE_URL + "/update-slot", invalidSlot, Response.class, status().isNotFound());
//    }
//}
