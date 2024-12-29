package org.protu.userservice.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.exceptions.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class FailureResponseHelper {
  public static ResponseEntity<ApiResponse<ErrorDetails>> buildResponse(HttpServletRequest request, HttpStatus status, String details, String headerMessage) {
    ErrorDetails errorDetails = new ErrorDetails(status.value(),details);
    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", headerMessage, errorDetails,
        request.getRequestURI(), request.getMethod());
    return new ResponseEntity<>(apiResponse, status);
  }
}
