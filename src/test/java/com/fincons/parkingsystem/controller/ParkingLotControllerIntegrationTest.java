package com.fincons.parkingsystem.controller;

import com.fincons.parkingsystem.dto.ParkingLotDto;
import com.fincons.parkingsystem.repository.ParkingLotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fincons.parkingsystem.utils.Response;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ParkingLotControllerIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ParkingLotRepository parkingLotRepository;

    @BeforeEach
    void setUp() {
        parkingLotRepository.deleteAll();
    }

    @Test
    void testCreateAndRetrieveParkingLots() {
        // Create a new parking lot
        ParkingLotDto newLotDto = ParkingLotDto.builder()
                .name("Integration Test Lot")
                .location("Test Location")
                .totalSlots(20)
                .basePricePerHour(5.0)
                .build();

        ResponseEntity<Response<ParkingLotDto>> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/parking-lots",
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(newLotDto),
                new ParameterizedTypeReference<Response<ParkingLotDto>>() {}
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().isSuccess()).isTrue();
        assertThat(createResponse.getBody().getData().getName()).isEqualTo("Integration Test Lot");

        // Retrieve all parking lots
        ResponseEntity<Response<List<ParkingLotDto>>> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/parking-lots/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Response<List<ParkingLotDto>>>() {}
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().isSuccess()).isTrue();
        assertThat(getResponse.getBody().getData()).hasSize(1);
        assertThat(getResponse.getBody().getData().get(0).getName()).isEqualTo("Integration Test Lot");
    }
}
