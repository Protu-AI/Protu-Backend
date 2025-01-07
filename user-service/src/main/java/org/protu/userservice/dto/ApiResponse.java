package org.protu.userservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.sql.Timestamp;

@JsonPropertyOrder({"status", "apiVersion", "message", "timestamp", "data"})
public record ApiResponse<T>(String status, String apiVersion, String message, Timestamp timestamp, T data, String requestUri, String requestMethod) {
  public ApiResponse {
    if (status == null)
      status = "SUCCESS";
    if (apiVersion == null)
      apiVersion = "v1.0.0";
    if (timestamp == null)
      timestamp = new Timestamp(System.currentTimeMillis());
  }

  public ApiResponse(String message, T data, String requestUri, String requestMethod) {
    this("SUCCESS", "v1.0.0", message, new Timestamp(System.currentTimeMillis()), data, requestUri, requestMethod);
  }

  public ApiResponse(String status, String message, T data, String requestUri, String requestMethod) {
    this(status, "v1.0.0", message, new Timestamp(System.currentTimeMillis()), data, requestUri, requestMethod);
  }
}
