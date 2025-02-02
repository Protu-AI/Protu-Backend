package org.protu.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String message, T data, List<Object> errors, MetaData meta) {
  public record MetaData(String status, String apiVersion, Timestamp timestamp, RequestDetails request) {
    public record RequestDetails(String method, String uri) {}
  }
}

