package org.protu.userservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.sql.Timestamp;

@JsonPropertyOrder({"status", "apiVersion", "message", "timestamp", "data", "request"})
public record ApiResponse<T>(String status, String apiVersion, String message, T data, Timestamp timestamp ,RequestInfo request) {
  public record RequestInfo(String uri, String method) {}
}
