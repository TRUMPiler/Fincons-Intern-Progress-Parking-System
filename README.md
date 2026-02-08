# Parking System API

This project is a comprehensive backend solution for a modern parking management system, built with Spring Boot. It provides a robust set of APIs to handle core parking operations, including vehicle entry and exit, dynamic pricing, reservations, and administrative management of parking lots and slots.

---

## Key Features

- **Core Parking Flow**: Full lifecycle management for parking sessions, from vehicle entry to exit.
- **Dynamic Pricing**: Automatically adjusts parking rates based on lot occupancy to maximize revenue.
- **Reservation System**: Allows users to reserve a parking spot in advance, with automated expiration for unclaimed reservations.
- **Soft Deletes**: Parking lots and slots can be deactivated without permanent data loss, preserving historical records.
- **Administrative Control**: Endpoints for creating, viewing, deactivating, and reactivating parking lots and their associated slots.
- **Concurrency Handling**: Implements optimistic and pessimistic locking strategies to ensure data integrity in a multi-user environment.
- **Statistical Insights**: Provides endpoints to retrieve key performance indicators for each parking lot, such as revenue and occupancy rates.
- **Real-Time Updates**: Uses WebSockets to push live updates to the UI for events like vehicle entry and exit.

---

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3**: Core framework for building the application.
- **Spring Data JPA**: For data persistence and repository management.
- **Spring WebSocket**: For real-time, two-way communication with clients.
- **Spring for Apache Kafka**: For asynchronous, decoupled event-driven communication.
- **PostgreSQL**: Relational database for storing all application data.
- **Hibernate**: ORM for mapping Java objects to database tables.
- **MapStruct**: For high-performance mapping between entities and DTOs.
- **Lombok**: To reduce boilerplate code in model and DTO classes.
- **JUnit 5 & Mockito**: For unit and integration testing.

### Frontend
- **Angular**: A robust framework for building the client-side user interface.
- **TypeScript**: For type-safe frontend code.

---

## Getting Started

### Prerequisites
- **JDK 17** or later
- **Maven 3.8** or later
- **PostgreSQL** database server
- **Apache Kafka** server
- **Node.js and npm** (for the frontend)

### Backend Setup

1. **Clone the repository:**
   ```bash
   git clone <your-repository-url>
   cd ParkingSystem
   ```

2. **Configure the database and Kafka:**
   - Create a PostgreSQL database named `ParkingSystem`.
   - Open `src/main/resources/application-dev.yaml`.
   - Update the `spring.datasource` properties to match your local PostgreSQL configuration.
   - Update the `spring.kafka.bootstrap-servers` property to point to your Kafka instance.

3. **Build and run the application:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   The backend server will start on `http://localhost:8080`.

### Frontend Setup

1. **Navigate to the frontend directory:**
   ```bash
   cd Frontend/Parking-System
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Run the frontend application:**
   ```bash
   ng serve
   ```
   The frontend development server will start on `http://localhost:4200`.

---

## API Endpoints

| Method  | Path                                | Description                                                                        |
|---------|-------------------------------------|------------------------------------------------------------------------------------|
| `POST`  | `/api/parking/entry`                | Records a vehicle's entry and starts a new parking session.                        |
| `POST`  | `/api/parking/exit`                 | Records a vehicle's exit, completes the session, and calculates charges.           |
| `POST`  | `/api/parking-lots`                 | Creates a new parking lot.                                                         |
| `GET`   | `/api/parking-lots`                 | Retrieves a list of all active parking lots.                                       |
| `GET`   | `/api/parking-lots/with-inactive`   | Retrieves all parking lots, including deactivated ones (for admin purposes).       |
| `DELETE`| `/api/parking-lots/{id}`            | Deactivates (soft-deletes) a parking lot.                                          |
| `PATCH` | `/api/parking-lots/{id}/reactivate` | Reactivates a soft-deleted parking lot and its slots.                              |
| `GET`   | `/api/parking-lots/{id}/stats`      | Retrieves performance statistics for a specific parking lot.                       |
| `GET`   | `/api/parking-slots/by-lot/{id}`    | Gets the status and availability of all slots in a specific lot.                   |
| `PATCH` | `/api/parking-slots/update-slot`    | Updates the status of a specific parking slot.                                     |
| `POST`  | `/api/reservations`                 | Creates a new reservation for a parking spot.                                      |
| `DELETE`| `/api/reservations/{id}`            | Cancels an active reservation.                                                     |
| `GET`   | `/api/reservations`                 | Retrieves a list of all reservations.                                              |
| `POST`  | `/api/reservations/{id}/arrival`    | Processes the arrival of a vehicle with a reservation, starting a parking session. |
| `GET`   | `/api/sessions/active`              | Retrieves a list of all currently active parking sessions.                         |
| `GET`   | `/api/sessions/history`             | Retrieves a history of all completed parking sessions.                             |

---

## WebSocket Communication

The application uses WebSockets to push real-time updates to the client.

- **Endpoint**: `ws://localhost:8080/ws`
- **Topic for Session Updates**: `/topic/sessions`

When a vehicle enters or exits a parking lot, a message is broadcast to the `/topic/sessions` topic. The message will be a JSON string representing either a `VehicleEnteredEvent` or a `VehicleExitedEvent`. Clients subscribed to this topic should re-fetch the active session list from the `/api/sessions/active` endpoint upon receiving a message.

---

## Running Tests

You can run the full suite of JUnit tests using the following Maven command from the project root:

```bash
mvn test
```
This will execute all controller tests, ensuring the API layer behaves as expected.
