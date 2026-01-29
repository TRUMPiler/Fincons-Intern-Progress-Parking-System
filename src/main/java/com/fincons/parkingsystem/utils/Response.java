package com.fincons.parkingsystem.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    LocalDateTime time;
    T data;
    String message;
    boolean success;
    int statucCode;
}
