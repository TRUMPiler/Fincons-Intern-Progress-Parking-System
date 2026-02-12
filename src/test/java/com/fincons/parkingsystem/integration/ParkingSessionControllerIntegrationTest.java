//package com.fincons.parkingsystem.integration;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fincons.parkingsystem.base.AbstractIntegrationTest;
//import com.fincons.parkingsystem.base.RestPage;
//import com.fincons.parkingsystem.dto.ParkingSessionDto;
//import com.fincons.parkingsystem.entity.ParkingLot;
//import com.fincons.parkingsystem.entity.ParkingSession;
//import com.fincons.parkingsystem.entity.ParkingSessionStatus;
//import com.fincons.parkingsystem.entity.ParkingSlot;
//import com.fincons.parkingsystem.entity.SlotStatus;
//import com.fincons.parkingsystem.entity.Vehicle;
//import com.fincons.parkingsystem.entity.VehicleType;
//import com.fincons.parkingsystem.repository.*;
//import com.fincons.parkingsystem.utils.Response;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.time.LocalDateTime;
//import java.util.stream.IntStream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//public class ParkingSessionControllerIntegrationTest extends AbstractIntegrationTest {
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
//    @BeforeEach
//    void setUp() {
//        parkingSessionRepository.deleteAll();
//        parkingSlotRepository.deleteAll();
//        vehicleRepository.deleteAll();
//        parkingLotRepository.deleteAll();
//    }
//
//    private final String BASE_URL = "/api/sessions";
//
//    // Helper method to create and save a parking session with all its dependencies
//    private void createAndSaveParkingSession(String vehicleNumber, VehicleType vehicleType, ParkingSessionStatus status, LocalDateTime entryTime, LocalDateTime exitTime, Double totalAmount, String lotName, int slotNumber) {
//        // Create ParkingLot
//        ParkingLot parkingLot = ParkingLot.builder()
//                .name(lotName)
//                .location("Location " + lotName)
//                .totalSlots(1)
//                .basePricePerHour(10.0)
//                .build();
//        parkingLot = parkingLotRepository.save(parkingLot);
//
//        // Create ParkingSlot
//        ParkingSlot parkingSlot = ParkingSlot.builder()
//                .slotNumber(String.valueOf(slotNumber))
//                .status(SlotStatus.AVAILABLE) // Will be updated by session
//                .parkingLot(parkingLot)
//                .build();
//        parkingSlot = parkingSlotRepository.save(parkingSlot);
//
//        // Create Vehicle
//        Vehicle vehicle = Vehicle.builder()
//                .vehicleNumber(vehicleNumber)
//                .vehicleType(vehicleType)
//                .build();
//        vehicle = vehicleRepository.save(vehicle);
//
//        // Create ParkingSession
//        ParkingSession session = ParkingSession.builder()
//                .vehicle(vehicle)
//                .parkingSlot(parkingSlot)
//                .entryTime(entryTime)
//                .exitTime(exitTime)
//                .totalAmount(totalAmount)
//                .status(status)
//                .build();
//        parkingSessionRepository.save(session);
//    }
//
//    @Test
//    void testGetActiveSessions_WithResults() throws Exception {
//        // Arrange
//        createAndSaveParkingSession("ABC-123", VehicleType.CAR, ParkingSessionStatus.ACTIVE, LocalDateTime.now().minusHours(1), null, null, "Lot A", 1);
//
//        // Act
//        Response<RestPage<ParkingSessionDto>> response = performGetRequest(BASE_URL + "/active", Response.class, status().isOk());
//
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isEqualTo(1);
//        assertThat(page.getContent().get(0).getVehicleNumber()).isEqualTo("ABC-123");
//        assertThat(page.getContent().get(0).getStatus()).isEqualTo(ParkingSessionStatus.ACTIVE);
//    }
//
//    @Test
//    void testGetActiveSessions_Empty() throws Exception {
//        // Act
//        Response<RestPage<ParkingSessionDto>> response = performGetRequest(BASE_URL + "/active", Response.class, status().isOk());
//
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isZero();
//    }
//
//    @Test
//    void testGetSessionHistory_WithResults() throws Exception {
//        // Arrange
//        createAndSaveParkingSession("DEF-456", VehicleType.CAR, ParkingSessionStatus.COMPLETED, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), 20.0, "Lot B", 2);
//
//        // Act
//        Response<RestPage<ParkingSessionDto>> response = performGetRequest(BASE_URL + "/history", Response.class, status().isOk());
//
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isEqualTo(1);
//        assertThat(page.getContent().get(0).getVehicleNumber()).isEqualTo("DEF-456");
//        assertThat(page.getContent().get(0).getStatus()).isEqualTo(ParkingSessionStatus.COMPLETED);
//    }
//
//    @Test
//    void testGetSessionHistory_Empty() throws Exception {
//        // Act
//        Response<RestPage<ParkingSessionDto>> response = performGetRequest(BASE_URL + "/history", Response.class, status().isOk());
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isZero();
//    }
//
//    @Test
//    void testGetActiveSessions_PaginationAndSorting() throws Exception {
//        // Arrange
//        IntStream.range(0, 5).forEach(i ->
//                createAndSaveParkingSession("VEH-" + (5 - i), VehicleType.CAR, ParkingSessionStatus.ACTIVE, LocalDateTime.now().minusMinutes(i), null, null, "Lot C", i + 1)
//        );
//
//        // Act: Request page 0, size 2, sorted by vehicleNumber ascending
//        String url1 = BASE_URL + "/active?page=0&size=2&sort=vehicle.vehicleNumber,asc";
//        Response response1 = performGetRequest(url1, Response.class, status().isOk());
//
//        // Assert
//        assertThat(response1.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page1 = mapper.convertValue(response1.getData(), new TypeReference<>() {});
//        assertThat(page1.getTotalElements()).isEqualTo(5);
//        assertThat(page1.getTotalPages()).isEqualTo(3);
//        assertThat(page1.getNumberOfElements()).isEqualTo(2);
//        assertThat(page1.getContent().get(0).getVehicleNumber()).isEqualTo("VEH-1");
//        assertThat(page1.getContent().get(1).getVehicleNumber()).isEqualTo("VEH-2");
//
//        // Act: Request page 1, size 2, sorted by entryTime descending
//        String url2 = BASE_URL + "/active?page=1&size=2&sort=entryTime,desc";
//        Response response2 = performGetRequest(url2, Response.class, status().isOk());
//
//        // Assert
//        assertThat(response2.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page2 = mapper.convertValue(response2.getData(), new TypeReference<>() {});
//        assertThat(page2.getNumberOfElements()).isEqualTo(2);
//        assertThat(page2.getContent().get(0).getVehicleNumber()).isEqualTo("VEH-3");
//        assertThat(page2.getContent().get(1).getVehicleNumber()).isEqualTo("VEH-2");
//    }
//
//    @Test
//    void testGetSessionHistory_PaginationAndSorting() throws Exception {
//        // Arrange
//        IntStream.range(0, 5).forEach(i ->
//                createAndSaveParkingSession("HIST-" + (5 - i), VehicleType.BIKE, ParkingSessionStatus.COMPLETED, LocalDateTime.now().minusHours(5 - i), LocalDateTime.now().minusHours(4 - i), 10.0 + i, "Lot D", i + 1)
//        );
//
//        // Act: Request page 0, size 3, sorted by totalAmount descending
//        String url = BASE_URL + "/history?page=0&size=3&sort=totalAmount,desc";
//        Response response = performGetRequest(url, Response.class, status().isOk());
//
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isEqualTo(5);
//        assertThat(page.getTotalPages()).isEqualTo(2);
//        assertThat(page.getNumberOfElements()).isEqualTo(3);
//        assertThat(page.getContent().get(0).getTotalAmount()).isEqualTo(14.0);
//        assertThat(page.getContent().get(1).getTotalAmount()).isEqualTo(13.0);
//        assertThat(page.getContent().get(2).getTotalAmount()).isEqualTo(12.0);
//    }
//
//    @Test
//    void testGetActiveSessions_PageBeyondBounds() throws Exception {
//        // Arrange
//        createAndSaveParkingSession("BOUND-1", VehicleType.CAR, ParkingSessionStatus.ACTIVE, LocalDateTime.now().minusMinutes(10), null, null, "Lot E", 1);
//
//        // Act: Request page 10 (beyond the single page of results)
//        String url = BASE_URL + "/active?page=10&size=5";
//        Response response = performGetRequest(url, Response.class, status().isOk());
//
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isEqualTo(1);
//        assertThat(page.getTotalPages()).isEqualTo(1);
//        assertThat(page.getNumberOfElements()).isZero();
//        assertThat(page.getContent()).isEmpty();
//    }
//
//    @Test
//    void testGetSessionHistory_PageBeyondBounds() throws Exception {
//        // Arrange
//        createAndSaveParkingSession("BOUND-2", VehicleType.CAR, ParkingSessionStatus.COMPLETED, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2), 30.0, "Lot F", 1);
//
//        // Act: Request page 5 (beyond the single page of results)
//        String url = BASE_URL + "/history?page=5&size=2";
//        Response response = performGetRequest(url, Response.class, status().isOk());
//
//        // Assert
//        assertThat(response.isSuccess()).isTrue();
//        RestPage<ParkingSessionDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isEqualTo(1);
//        assertThat(page.getTotalPages()).isEqualTo(1);
//        assertThat(page.getNumberOfElements()).isZero();
//        assertThat(page.getContent()).isEmpty();
//    }
//}
