package com.fincons.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic wrapper class for messages sent over WebSocket.
 * This provides a standardized structure for all real-time communications.
 *
 * @param <T> The type of the data payload.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessage<T> {

    /** A string identifier for the type of message (e.g., "SLOT_UPDATE", "SESSION_ENTRY"). */
    private String type;

    /** The main data payload of the message. */
    private T payload;
}
