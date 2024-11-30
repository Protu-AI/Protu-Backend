package org.protu.userservice.exceptions;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorDetails {
  int code;
  String message;
  String details;
  Timestamp timestamp;
}
