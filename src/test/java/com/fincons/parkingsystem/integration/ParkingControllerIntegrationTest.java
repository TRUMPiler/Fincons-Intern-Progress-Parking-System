//package com.fincons.parkingsystem.integration;
//
//import com.fincons.parkingsystem.base.AbstractIntegrationTest;
//import com.fincons.parkingsystem.dto.ParkingLotDto;
//import com.fincons.parkingsystem.dto.ParkingSessionDto;
//import com.fincons.parkingsystem.dto.VehicleDto;
//import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;
//import com.fincons.parkingsystem.entity.ParkingSession;
//import com.fincons.parkingsystem.entity.ParkingSessionStatus;
//import com.fincons.parkingsystem.entity.ParkingSlot;
//import com.fincons.parkingsystem.entity.Reservation;
//import com.fincons.parkingsystem.entity.ReservationStatus;
//import com.fincons.parkingsystem.entity.Vehicle;
//import com.fincons.parkingsystem.entity.VehicleType;
//import com.fincons.parkingsystem.repository.ParkingLotRepository;
//import com.fincons.parkingsystem.repository.ParkingSessionRepository;
//import com.fincons.parkingsystem.repository.ParkingSlotRepository;
//import com.fincons.parkingsystem.repository.ReservationRepository;
//import com.fincons.parkingsystem.repository.VehicleRepository;
//import com.fincons.parkingsystem.service.ParkingLotService;
//import com.fincons.parkingsystem.utils.Response;
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
//public class ParkingControllerIntegrationTest extends AbstractIntegrationTest {
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
//    @Autowired
//    private ReservationRepository reservationRepository;
//
//    private ParkingLotDto parkingLot;
//
//    @BeforeEach
//    void setUp() {
//        // Clean up database before each test
//        reservationRepository.deleteAll();
//        parkingSessionRepository.deleteAll();
//        parkingSlotRepository.deleteAll();
//        vehicleRepository.deleteAll();
//        parkingLotRepository.deleteAll();
//
//        // Create a standard parking lot for tests
//        parkingLot = parkingLotService.createParkingLot(ParkingLotDto.builder()
//                .name("Main Lot")
//                .totalSlots(1) // Start with one slot for simple tests
//                .basePricePerHour(10.0)
//                .build());
//    }
//
//    @Test
//    void testVehicleEntryAndExit_Success() throws Exception {
//        // Entry
//        VehicleEntryRequestDto entryRequest = new VehicleEntryRequestDto("ABC-123", VehicleType.CAR, parkingLot.getId());
//        Response<ParkingSessionDto> entryResponse = performPostRequest("/api/parking/entry", entryRequest, Response.class, status().isOk());
//
//        assertThat(entryResponse).isNotNull();
//        assertThat(entryResponse.isSuccess()).isTrue();
//        ParkingSessionDto entryData = mapper.convertValue(entryResponse.getData(), ParkingSessionDto.class);
//        assertThat(entryData.getVehicleNumber()).isEqualTo("ABC-123");
//        assertThat(entryData.getStatus()).isEqualTo(com.fincons.parkingsystem.entity.ParkingSessionStatus.ACTIVE);
//
//        // Exit
//        VehicleDto exitRequest = new VehicleDto(null, "ABC-123", null);
//        Response<ParkingSessionDto> exitResponse = performPostRequest("/api/parking/exit", exitRequest, Response.class, status().isOk());
//
//        assertThat(exitResponse).isNotNull();
//        assertThat(exitResponse.isSuccess()).isTrue();
//        ParkingSessionDto exitData = mapper.convertValue(exitResponse.getData(), ParkingSessionDto.class);
//        assertThat(exitData.getStatus()).isEqualTo(com.fincons.parkingsystem.entity.ParkingSessionStatus.COMPLETED);
//        assertThat(exitData.getTotalAmount()).isNotNull();
//    }
//
//    @Test
//    void testVehicleEntry_Failure_LotNotFound() throws Exception {
//        VehicleEntryRequestDto entryRequest = new VehicleEntryRequestDto("XYZ-789", VehicleType.CAR, 999L); // Non-existent lot ID
//        Response<String> response = performPostRequest("/api/parking/entry", entryRequest, Response.class, status().isNotFound());
//
//        assertThat(response).isNotNull();
//        assertThat(response.isSuccess()).isFalse();
//        assertThat(response.getMessage()).contains("Parking lot not found");
//    }
//
//    @Test
//    void testVehicleEntry_Failure_LotIsFull() throws Exception {
//        // First vehicle enters successfully
//        performPostRequest("/api/parking/entry", new VehicleEntryRequestDto("CAR-1", VehicleType.CAR, parkingLot.getId()), Response.class, status().isOk());
//
//        // Second vehicle tries to enter the now-full lot
//        VehicleEntryRequestDto secondEntryRequest = new VehicleEntryRequestDto("CAR-2", VehicleType.CAR, parkingLot.getId());
//        Response<String> response = performPostRequest("/api/parking/entry", secondEntryRequest, Response.class, status().isConflict());
//
//        assertThat(response).isNotNull();
//        assertThat(response.getMessage()).contains("No available parking slots");
//    }
//
//    @Test
//    void testVehicleEntry_Failure_InvalidRequestBody() throws Exception {
//        // Missing vehicleNumber
//        VehicleEntryRequestDto request = new VehicleEntryRequestDto(null, VehicleType.CAR, parkingLot.getId());
//        performPostRequest("/api/parking/entry", request, Response.class, status().isBadRequest());
//    }
//
//    @Test
//    void testVehicleEntry_Failure_VehicleAlreadyParked() throws Exception {
//        // First vehicle enters successfully
//        performPostRequest("/api/parking/entry", new VehicleEntryRequestDto("DUPE-CAR", VehicleType.CAR, parkingLot.getId()), Response.class, status().isOk());
//
//        // Same vehicle tries to enter again
//        VehicleEntryRequestDto secondRequest = new VehicleEntryRequestDto("DUPE-CAR", VehicleType.CAR, parkingLot.getId());
//        Response<String> response = performPostRequest("/api/parking/entry", secondRequest, Response.class, status().isConflict());
//
//        assertThat(response).isNotNull();
//        assertThat(response.getMessage()).contains("Vehicle already has an active parking session.");
//    }
//
//    @Test
//    void testVehicleEntry_Failure_VehicleHasActiveReservation() throws Exception {
//        // Arrange: Create a vehicle with an active reservation
//        Vehicle vehicle = vehicleRepository.save(Vehicle.builder().vehicleNumber("RESERVED-CAR").vehicleType(VehicleType.CAR).build());
//        List<ParkingSlot> slots = parkingSlotRepository.findByParkingLotId(parkingLot.getId());
//        Reservation reservation = Reservation.builder()
//                .vehicle(vehicle)
//                .parkingSlot(slots.get(0))
//                .status(ReservationStatus.ACTIVE)
//                .reservationTime(LocalDateTime.now())
//                .expirationTime(LocalDateTime.now().plusMinutes(15))
//                .build();
//        reservationRepository.save(reservation);
//
//        // Act: Try to perform a normal entry
//        VehicleEntryRequestDto entryRequest = new VehicleEntryRequestDto("RESERVED-CAR", VehicleType.CAR, parkingLot.getId());
//        Response<String> response = performPostRequest("/api/parking/entry", entryRequest, Response.class, status().isConflict());
//
//        // Assert
//        assertThat(response).isNotNull();
//        assertThat(response.getMessage()).contains("This vehicle already has an active reservation");
//    }
//
//    @Test
//    void testVehicleExit_Failure_VehicleNotFound() throws Exception {
//        VehicleDto exitRequest = new VehicleDto(null, "NON-EXISTENT", null);
//        Response<String> response = performPostRequest("/api/parking/exit", exitRequest, Response.class, status().isNotFound());
//
//        assertThat(response).isNotNull();
//        assertThat(response.getMessage()).contains("Vehicle not found with number: NON-EXISTENT");
//    }
//
//    @Test
//    void testVehicleExit_Failure_InvalidRequestBody() throws Exception {
//        VehicleDto exitRequest = new VehicleDto(null, null, null); // Null vehicle number
//        performPostRequest("/api/parking/exit", exitRequest, Response.class, status().isBadRequest());
//    }
//
//    @Test
//    void testVehicleExit_Failure_NoActiveSession() throws Exception {
//        // Arrange: Create a vehicle and a COMPLETED session for it
//        Vehicle vehicle = vehicleRepository.save(Vehicle.builder().vehicleNumber("NO-SESSION-CAR").vehicleType(VehicleType.CAR).build());
//        List<ParkingSlot> slots = parkingSlotRepository.findByParkingLotId(parkingLot.getId());
//        ParkingSession completedSession = ParkingSession.builder()
//                .vehicle(vehicle)
//                .parkingSlot(slots.get(0))
//                .entryTime(LocalDateTime.now().minusHours(2))
//                .exitTime(LocalDateTime.now().minusHours(1))
//                .status(ParkingSessionStatus.COMPLETED)
//                .build();
//        parkingSessionRepository.save(completedSession);
//
//        // Act: Try to exit the vehicle again
//        VehicleDto exitRequest = new VehicleDto(null, "NO-SESSION-CAR", null);
//        Response<String> response = performPostRequest("/api/parking/exit", exitRequest, Response.class, status().isNotFound());
//
//        // Assert
//        assertThat(response).isNotNull();
//        assertThat(response.getMessage()).contains("No active parking session found for this vehicle.");
//    }
//}
