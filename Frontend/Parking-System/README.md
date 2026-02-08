# Parking System Frontend

This is an Angular application for a parking system. It allows users to manage parking lots, handle vehicle entries and exits, view active sessions and session history, and make reservations.

## Project Structure

The project is structured as follows:

- `src/app/`: Contains the main application logic.
  - `app.component.ts`: The root component of the application.
  - `app.routes.ts`: Defines the application's routes.
  - `auth.service.ts`: Handles authentication and API calls.
  - `websocket.service.ts`: Manages WebSocket connections for real-time updates.
  - `active-sessions/`: Component for displaying active parking sessions.
  - `homepage/`: The main landing page of the application.
  - `parkinglot-registeration/`: Component for registering and managing parking lots.
  - `reservation/`: Component for creating and managing reservations.
  - `session-history/`: Component for displaying the history of parking sessions.
  - `show-info/`: Component for displaying detailed information about a parking lot.
  - `vehicle-entry/`: Component for handling vehicle entries.
  - `vehicle-exit/`: Component for handling vehicle exits.

## Components

### Homepage

The main landing page of the application. It provides navigation to all the major features.

### Parking Lot Registration

This component allows users to register new parking lots and view a list of existing ones. It includes a form for entering the parking lot's name, location, total slots, and base price per hour.

### Vehicle Entry

This component is used to record the entry of a new vehicle. It includes a form for entering the vehicle number, vehicle type, and selecting a parking lot.

### Vehicle Exit

This component is used to process a vehicle's exit from the parking lot. It calculates the parking fee and displays a bill.

### Active Sessions

This component displays a real-time list of all active parking sessions. It uses a WebSocket connection to receive live updates.

### Session History

This component displays a paginated and sortable list of all completed parking sessions.

### Show Info

This component displays detailed information about a specific parking lot, including its name, location, total slots, and a list of all parking slots with their current status.

### Reservation

This component allows users to make a reservation for a parking slot. It includes a form for entering the vehicle number, vehicle type, and selecting a parking lot. It also displays a list of all existing reservations.