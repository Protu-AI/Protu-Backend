package org.protu.userservice.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.exceptions.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.Instant;

public class FailureResponseHelper {
  public static ResponseEntity<ApiResponse<ErrorDetails>> buildResponse(String apiVersion, HttpServletRequest request, HttpStatus status, String details, String headerMessage) {
    ErrorDetails errorDetails = new ErrorDetails(status.value(),details);
    return ResponseEntity.status(status)
        .body(new ApiResponse<>("FAILURE", apiVersion, headerMessage, errorDetails,
          Timestamp.from(Instant.now()),
          new ApiResponse.RequestInfo(request.getRequestURI(),request.getMethod())));
  }
}
