//package com.fincons.parkingsystem.service;
//
//import com.fincons.parkingsystem.dto.ParkingLotDto;
//import com.fincons.parkingsystem.dto.ParkingSessionDto;
//import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;
//import com.fincons.parkingsystem.entity.VehicleType;
//import com.fincons.parkingsystem.exception.ConflictException;
//import com.fincons.parkingsystem.repository.ParkingLotRepository;
//import com.fincons.parkingsystem.repository.ParkingSessionRepository;
//import com.fincons.parkingsystem.repository.ParkingSlotRepository;
//import com.fincons.parkingsystem.repository.VehicleRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@Testcontainers
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//public class ParkingServiceIntegrationTest {
//
//    @Container
//    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
//            .withDatabaseName("testdb")
//            .withUsername("test")
//            .withPassword("test");
//
//    @DynamicPropertySource
//    static void setProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
//        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
//    }
//
//    @Autowired
//    private ParkingService parkingService;
//
//    @Autowired
//    private ParkingLotService parkingLotService;
//
//    @Autowired
//    private VehicleRepository vehicleRepository;
//
//    @Autowired
//    private ParkingSlotRepository parkingSlotRepository;
//
//    @Autowired
//    private ParkingSessionRepository parkingSessionRepository;
//
//    @Autowired
//    private ParkingLotRepository parkingLotRepository;
//
//    @BeforeEach
//    void setUp() {
//        parkingSessionRepository.deleteAll();
//        parkingSlotRepository.deleteAll();
//        vehicleRepository.deleteAll();
//        parkingLotRepository.deleteAll();
//    }
//
//    @Test
//    void testVehicleEntryAndExit_Success() {
//        // Create a parking lot
//        ParkingLotDto parkingLot = parkingLotService.createParkingLot(ParkingLotDto.builder()
//                .name("Test Lot")
//                .totalSlots(1)
//                .basePricePerHour(10.0)
//                .build());
//
//        // Vehicle Entry
//        VehicleEntryRequestDto entryRequest = new VehicleEntryRequestDto("INTEGRATION-TEST", VehicleType.CAR, parkingLot.getId());
//        ParkingSessionDto entrySession = parkingService.enterVehicle(entryRequest);
//
//        assertThat(entrySession).isNotNull();
//        assertThat(entrySession.getVehicleNumber()).isEqualTo("INTEGRATION-TEST");
//        assertThat(entrySession.getStatus()).isEqualTo(com.fincons.parkingsystem.entity.ParkingSessionStatus.ACTIVE);
//
//        // Verify that a slot is occupied
//        long occupiedSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLotRepository.findById(parkingLot.getId()).get(), com.fincons.parkingsystem.entity.SlotStatus.OCCUPIED);
//        assertThat(occupiedSlots).isEqualTo(1);
//
//        // Attempt to enter the same vehicle again should fail
//        assertThrows(ConflictException.class, () -> {
//            parkingService.enterVehicle(entryRequest);
//        });
//
//        // Vehicle Exit
//        ParkingSessionDto exitSession = parkingService.exitVehicle("INTEGRATION-TEST");
//
//        assertThat(exitSession).isNotNull();
//        assertThat(exitSession.getStatus()).isEqualTo(com.fincons.parkingsystem.entity.ParkingSessionStatus.COMPLETED);
//        assertThat(exitSession.getTotalAmount()).isNotNull();
//
//        // Verify that the slot is now available
//        long availableSlots = parkingSlotRepository.countByParkingLotAndStatus(parkingLotRepository.findById(parkingLot.getId()).get(), com.fincons.parkingsystem.entity.SlotStatus.AVAILABLE);
//        assertThat(availableSlots).isEqualTo(1);
//    }
//}
