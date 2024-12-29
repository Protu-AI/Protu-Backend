package org.protu.userservice.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartialUpdateReqDto {
  String username;
  String firstName;
  String lastName;
  String email;
  String phoneNumber;
  String password;
}
