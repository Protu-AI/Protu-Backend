package org.protu.userservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ApiResponse<T> {
  String status = "SUCCESS";
  String apiVersion = "v1.0.0";
  String message;
  Timestamp timestamp = new Timestamp(System.currentTimeMillis());
  T data;
  String requestUri;
  String requestMethod;

  public ApiResponse(String message, T data, String requestUri, String requestMethod) {
    this.message = message;
    this.data = data;
    this.requestUri = requestUri;
    this.requestMethod = requestMethod;
  }

  public ApiResponse(String status,String message, T data, String requestUri, String requestMethod) {
    this.status = status;
    this.message = message;
    this.data = data;
    this.requestUri = requestUri;
    this.requestMethod = requestMethod;
  }

}
