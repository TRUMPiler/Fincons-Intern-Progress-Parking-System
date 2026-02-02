package com.fincons.parkingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main entry point for the Parking System application.
 * This class initializes the Spring Boot context and enables application-wide features.
 * The @EnableScheduling annotation is used to activate Spring's scheduled task execution capabilities,
 * which is essential for features like the reservation expiration job.
 */
@SpringBootApplication
@EnableScheduling
public class ParkingSystemApplication {

    /**
     * The main method, which serves as the entry point for the Java Virtual Machine (JVM)
     * to start the application. It delegates to Spring Boot's SpringApplication class
     * to set up the application context.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(ParkingSystemApplication.class, args);
    }

}
