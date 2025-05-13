package org.protu.contentservice.common.response;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class ApiResponseBuilder {

  private static <T> ResponseEntity<ApiResponse<T>> buildApiResponse(
      String message,
      T data,
      List<ErrorDetails> errors,
      HttpStatus status,
      String apiVersion,
      HttpServletRequest request) {

    String statusText = errors == null ? "SUCCESS" : "FAILURE";
    return ResponseEntity.status(status)
        .body(
            new ApiResponse<>(message, data, errors,
                new ApiResponse.MetaData(statusText, apiVersion, Timestamp.from(Instant.now()),
                    new ApiResponse.MetaData.RequestDetails(request.getMethod(), request.getRequestURI()))));
  }

  public static <T> ResponseEntity<ApiResponse<T>> buildSuccessApiResponse(
      String message,
      T data,
      HttpStatus status,
      String apiVersion,
      HttpServletRequest request) {

    return buildApiResponse(message, data, null, status, apiVersion, request);
  }

  public static <T> ResponseEntity<ApiResponse<T>> buildFailureApiResponse(
      String message,
      List<ErrorDetails> errors,
      HttpStatus status,
      String apiVersion,
      HttpServletRequest request) {

    return buildApiResponse(message, null, errors, status, apiVersion, request);
  }
}
