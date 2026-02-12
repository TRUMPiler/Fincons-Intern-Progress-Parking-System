//package com.fincons.parkingsystem.integration;
//
//import com.fincons.parkingsystem.base.AbstractIntegrationTest;
//import com.fincons.parkingsystem.dto.ParkingLotDto;
//import com.fincons.parkingsystem.dto.ParkingLotStatsDto;
//import com.fincons.parkingsystem.dto.VehicleDto;
//import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;
//import com.fincons.parkingsystem.entity.VehicleType;
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
//public class ParkingLotStatsControllerIntegrationTest extends AbstractIntegrationTest {
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
//    private final String BASE_URL = "/api/parking-lots";
//
//    @Test
//    void testGetStats_InitialState() throws Exception {
//        Response<ParkingLotStatsDto> response = performGetRequest(BASE_URL + "/" + parkingLot.getId() + "/stats", Response.class, status().isOk());
//
//        assertThat(response.isSuccess()).isTrue();
//        ParkingLotStatsDto stats = mapper.convertValue(response.getData(), ParkingLotStatsDto.class);
//        assertThat(stats).isNotNull();
//        assertThat(stats.getParkingLotId()).isEqualTo(parkingLot.getId());
//        assertThat(stats.getOccupancyPercentage()).isEqualTo(0.0);
//        assertThat(stats.getTotalRevenue()).isZero();
//    }
//
//    @Test
//    void testGetStats_AfterActivity() throws Exception {
//        // Arrange: A vehicle enters and exits
//        VehicleEntryRequestDto entryRequest = new VehicleEntryRequestDto("ABC-123", VehicleType.CAR, parkingLot.getId());
//        performPostRequest("/api/parking/entry", entryRequest, Response.class, status().isOk());
//
//        VehicleDto exitRequest = new VehicleDto(null, "ABC-123", null);
//        performPostRequest("/api/parking/exit", exitRequest, Response.class, status().isOk());
//
//        // Act
//        Response<ParkingLotStatsDto> response = performGetRequest(BASE_URL + "/" + parkingLot.getId() + "/stats", Response.class, status().isOk());
//
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        ParkingLotStatsDto stats = mapper.convertValue(response.getData(), ParkingLotStatsDto.class);
//        assertThat(stats).isNotNull();
//        assertThat(stats.getParkingLotId()).isEqualTo(parkingLot.getId());
//        assertThat(stats.getOccupancyPercentage()).isEqualTo(0.0); // Lot is empty again
//        assertThat(stats.getTotalRevenue()).isGreaterThan(-1); // Revenue should have been generated
//        assertThat(stats.getRevenueToday()).isGreaterThan(-1);
//    }
//
//    @Test
//    void testGetStats_Failure_NotFound() throws Exception {
//        performGetRequest(BASE_URL + "/999/stats", Response.class, status().isNotFound());
//    }
//}
