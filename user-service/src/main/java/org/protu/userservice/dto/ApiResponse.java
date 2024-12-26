package org.protu.userservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonPropertyOrder({
    "status",
    "apiVersion",
    "message",
    "timestamp",
    "data"
})
public class ApiResponse<T> {
  String status = "SUCCESS";
  String apiVersion = "v1.0.0";
  String message;
  Timestamp timestamp = new Timestamp(System.currentTimeMillis());
  T data;

  public ApiResponse(T data, String message) {
    this.data = data;
    this.message = message;
  }

  public ApiResponse(String status, String message, T data) {
    this.status = status;
    this.message = message;
    this.data = data;
  }
}
