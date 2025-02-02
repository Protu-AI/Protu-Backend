package org.protu.userservice.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.userservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.Instant;

public class SuccessResponseHelper {
  public static <T> ResponseEntity<ApiResponse<T>> buildResponse(String apiVersion, HttpServletRequest request, HttpStatus status, T data, String message) {
  return ResponseEntity.status(status)
        .body(new ApiResponse<>(message, data, null,
            new ApiResponse.MetaData("SUCCESS", apiVersion, Timestamp.from(Instant.now()),
            new ApiResponse.MetaData.RequestDetails(request.getMethod(), request.getRequestURI()))));
  }
}
