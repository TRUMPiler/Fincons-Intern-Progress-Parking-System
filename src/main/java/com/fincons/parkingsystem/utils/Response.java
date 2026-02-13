package com.fincons.parkingsystem.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.Instant;

/**
 * A generic wrapper class for API responses.
 * This class is used to standardize the structure of responses sent from the API,
 * providing a consistent format for clients to consume. It includes metadata such as
 * a timestamp, status code, and a success flag, along with the actual data payload.
 *
 * @param <T> the type of the data being returned in the response.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    /**
     * The timestamp when the response was generated.
     */
    Instant time;
    /**
     * The main data payload of the response.
     * The type of this field is generic, allowing it to hold any kind of data.
     */
    T data;
    /**
     * A message providing additional information about the response,
     * such as a success message or a description of an error.
     */
    String message;
    /**
     * A boolean flag indicating whether the request was successful.
     * `true` for success, `false` for failure.
     */
    boolean success;
    /**
     * The HTTP status code of the response.
     * For example, 200 for success, 404 for not found, etc.
     */
    int statucCode;
}
