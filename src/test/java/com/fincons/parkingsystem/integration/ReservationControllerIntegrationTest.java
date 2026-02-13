//package com.fincons.parkingsystem.integration;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fincons.parkingsystem.base.AbstractIntegrationTest;
//import com.fincons.parkingsystem.base.RestPage;
//import com.fincons.parkingsystem.dto.ParkingLotDto;
//import com.fincons.parkingsystem.dto.ReservationDto;
//import com.fincons.parkingsystem.dto.ReservationRequestDto;
//import com.fincons.parkingsystem.dto.VehicleEntryRequestDto;
//import com.fincons.parkingsystem.entity.ParkingLot;
//import com.fincons.parkingsystem.entity.ParkingSession;
//import com.fincons.parkingsystem.entity.ParkingSessionStatus;
//import com.fincons.parkingsystem.entity.ParkingSlot;
//import com.fincons.parkingsystem.entity.Reservation;
//import com.fincons.parkingsystem.entity.ReservationStatus;
//import com.fincons.parkingsystem.entity.SlotStatus;
//import com.fincons.parkingsystem.entity.Vehicle;
//import com.fincons.parkingsystem.entity.VehicleType;
//import com.fincons.parkingsystem.repository.*;
//import com.fincons.parkingsystem.service.ParkingLotService;
//import com.fincons.parkingsystem.utils.Response;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.time.Instant
//import java.util.List;
//import java.util.stream.IntStream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//
//public class ReservationControllerIntegrationTest extends AbstractIntegrationTest {
//
//    @Autowired
//    private ParkingLotService parkingLotService;
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
//        parkingSessionRepository.deleteAll();
//        reservationRepository.deleteAll();
//        parkingSlotRepository.deleteAll();
//        vehicleRepository.deleteAll();
//        parkingLotRepository.deleteAll();
//
//        parkingLot = parkingLotService.createParkingLot(ParkingLotDto.builder()
//                .name("Main Lot")
//                .totalSlots(10) // Increased slots for more complex tests
//                .basePricePerHour(10.0)
//                .build());
//    }
//
//    private final String BASE_URL = "/api/reservations";
//
//    private Reservation createAndSaveReservation(String vehicleNumber, ParkingLot lot, ReservationStatus status) {
//        Vehicle vehicle = vehicleRepository.save(Vehicle.builder().vehicleNumber(vehicleNumber).vehicleType(VehicleType.CAR).build());
//        ParkingSlot slot = parkingSlotRepository.findByParkingLotId(lot.getId()).get(0);
//        return reservationRepository.save(Reservation.builder()
//                .vehicle(vehicle)
//                .parkingSlot(slot)
//                .status(status)
//                .reservationTime(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant())
//                .expirationTime(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant().plusMinutes(15))
//                .build());
//    }
//
//    @Test
//    void testCreateAndCancelReservation_Success() throws Exception {
//        // Create Reservation
//        ReservationRequestDto requestDto = new ReservationRequestDto("RES-123", VehicleType.CAR, parkingLot.getId());
//        Response<ReservationDto> createResponse = performPostRequest(BASE_URL, requestDto, Response.class, status().isCreated());
//
//        assertThat(createResponse.isSuccess()).isTrue();
//        ReservationDto responseData = mapper.convertValue(createResponse.getData(), ReservationDto.class);
//        assertThat(responseData.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
//        Long reservationId = responseData.getId();
//
//        // Cancel Reservation
//        performDeleteRequest(BASE_URL + "/" + reservationId, Void.class, status().isOk());
//        Reservation cancelledReservation = reservationRepository.findById(reservationId).orElseThrow();
//        assertThat(cancelledReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
//    }
//
//    @Test
//    void testCreateReservation_Failure_LotFull() throws Exception {
//        // Fill the lot by fetching slots directly from the repository
//        List<ParkingSlot> slots = parkingSlotRepository.findByParkingLotId(parkingLot.getId());
//        slots.forEach(s -> s.setStatus(SlotStatus.OCCUPIED));
//        parkingSlotRepository.saveAll(slots);
//
//        // Attempt to reserve in the full lot
//        ReservationRequestDto requestDto = new ReservationRequestDto("RES-456", VehicleType.CAR, parkingLot.getId());
//        Response<String> response = performPostRequest(BASE_URL, requestDto, Response.class, status().isConflict());
//
//        assertThat(response.getMessage()).contains("No available parking slots");
//    }
//
//    @Test
//    void testCreateReservation_Failure_InvalidBody() throws Exception {
//        ReservationRequestDto requestDto = new ReservationRequestDto(null, VehicleType.CAR, parkingLot.getId());
//        performPostRequest(BASE_URL, requestDto, Response.class, status().isBadRequest());
//    }
//
//    @Test
//    void testCreateReservation_Failure_VehicleAlreadyParked() throws Exception {
//        // Arrange: vehicle has an active session
//        performPostRequest("/api/parking/entry", new VehicleEntryRequestDto("PARKED-CAR", VehicleType.CAR, parkingLot.getId()), Response.class, status().isOk());
//
//        // Act: try to create a reservation for the same vehicle
//        ReservationRequestDto requestDto = new ReservationRequestDto("PARKED-CAR", VehicleType.CAR, parkingLot.getId());
//        Response<String> response = performPostRequest(BASE_URL, requestDto, Response.class, status().isConflict());
//
//        // Assert
//        assertThat(response.getMessage()).contains("Vehicle already has an active parking session");
//    }
//
//    @Test
//    void testCreateReservation_Failure_DuplicateReservation() throws Exception {
//        // Arrange: vehicle already has an active reservation
//        createAndSaveReservation("DUPE-RES", parkingLotRepository.findById(parkingLot.getId()).get(), ReservationStatus.ACTIVE);
//
//        // Act: try to create another reservation
//        ReservationRequestDto requestDto = new ReservationRequestDto("DUPE-RES", VehicleType.CAR, parkingLot.getId());
//        Response<String> response = performPostRequest(BASE_URL, requestDto, Response.class, status().isConflict());
//
//        // Assert
//        assertThat(response.getMessage()).contains("Vehicle already has an active reservation");
//    }
//
//    @Test
//    void testCreateReservation_Failure_LotNotFound() throws Exception {
//        ReservationRequestDto requestDto = new ReservationRequestDto("ANY-CAR", VehicleType.CAR, 999L);
//        Response<String> response = performPostRequest(BASE_URL, requestDto, Response.class, status().isNotFound());
//        assertThat(response.getMessage()).contains("Parking lot not found");
//    }
//
//    @Test
//    void testCancelReservation_Failure_NotFound() throws Exception {
//        Response<String> response = performDeleteRequest(BASE_URL + "/999", Response.class, status().isNotFound());
//        assertThat(response.getMessage()).contains("Reservation not found");
//    }
//
//    @Test
//    void testCancelReservation_Failure_NotActive() throws Exception {
//        Reservation reservation = createAndSaveReservation("COMPLETED-RES", parkingLotRepository.findById(parkingLot.getId()).get(), ReservationStatus.COMPLETED);
//        Response<String> response = performDeleteRequest(BASE_URL + "/" + reservation.getId(), Response.class, status().isConflict());
//        assertThat(response.getMessage()).contains("Only active reservations can be cancelled");
//    }
//
//    @Test
//    void testGetReservationStatus_WithPaginationAndSorting() throws Exception {
//        ParkingLot lot = parkingLotRepository.findById(parkingLot.getId()).get();
//        IntStream.range(0, 5).forEach(i -> createAndSaveReservation("GET-RES-" + i, lot, ReservationStatus.ACTIVE));
//
//        String url = BASE_URL + "?page=0&size=3&sort=vehicle.vehicleNumber,desc";
//        Response<RestPage<ReservationDto>> response = performGetRequest(url, Response.class, status().isOk());
//        RestPage<ReservationDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//
//        assertThat(page.getTotalElements()).isEqualTo(5);
//        assertThat(page.getNumberOfElements()).isEqualTo(3);
//        assertThat(page.getContent().get(0).getVehicleNumber()).isEqualTo("GET-RES-4");
//    }
//
//    @Test
//    void testGetReservationStatus_Empty() throws Exception {
//        Response<RestPage<ReservationDto>> response = performGetRequest(BASE_URL, Response.class, status().isOk());
//        RestPage<ReservationDto> page = mapper.convertValue(response.getData(), new TypeReference<>() {});
//        assertThat(page.getTotalElements()).isZero();
//    }
//
//    @Test
//    void testProcessArrival_Success() throws Exception {
//        Reservation reservation = createAndSaveReservation("ARR-123", parkingLotRepository.findById(parkingLot.getId()).get(), ReservationStatus.ACTIVE);
//        performPostRequest(BASE_URL + "/" + reservation.getId() + "/arrival", null, Void.class, status().isOk());
//
//        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
//        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
//        assertThat(parkingSessionRepository.count()).isEqualTo(1);
//    }
//
//    @Test
//    void testProcessArrival_Failure_ReservationNotFound() throws Exception {
//        performPostRequest(BASE_URL + "/999/arrival", null, Response.class, status().isNotFound());
//    }
//
//    @Test
//    void testProcessArrival_Failure_NotActive() throws Exception {
//        Reservation reservation = createAndSaveReservation("ARR-CANCELLED", parkingLotRepository.findById(parkingLot.getId()).get(), ReservationStatus.CANCELLED);
//        Response<String> response = performPostRequest(BASE_URL + "/" + reservation.getId() + "/arrival", null, Response.class, status().isConflict());
//        assertThat(response.getMessage()).contains("Reservation is not active");
//    }
//}
