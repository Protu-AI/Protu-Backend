package org.protu.userservice.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.exceptions.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class FailureResponseHelper {
  public static ResponseEntity<ApiResponse<ErrorDetails>> buildResponse(String apiVersion, HttpServletRequest request, HttpStatus status, String details, String headerMessage) {
    List<Object> errors = List.of(new ErrorDetails(status.value(), details));
    return ResponseEntity.status(status)
        .body(new ApiResponse<>(headerMessage, null, errors,
            new ApiResponse.MetaData("FAILURE", apiVersion, Timestamp.from(Instant.now()),
                new ApiResponse.MetaData.RequestDetails(request.getMethod(), request.getRequestURI()))));
  }
}
