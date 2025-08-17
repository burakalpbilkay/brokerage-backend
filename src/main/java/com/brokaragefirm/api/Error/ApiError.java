package com.brokaragefirm.api.Error;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(Instant timestamp, int status, String error, String message, String path) {
  public static ApiError of(HttpStatus s, String msg, String path) {
    return new ApiError(Instant.now(), s.value(), s.getReasonPhrase(), msg, path);
  }
}
