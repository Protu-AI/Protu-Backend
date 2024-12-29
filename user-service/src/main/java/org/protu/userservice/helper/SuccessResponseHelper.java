package org.protu.userservice.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.userservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SuccessResponseHelper {
  public static <T> ResponseEntity<ApiResponse<T>> buildResponse(HttpServletRequest request, HttpStatus status, T data, String message) {
    return ResponseEntity.status(status)
        .body(new ApiResponse<>(message,data,request.getRequestURI(), request.getMethod()));
  }
}
