package org.protu.userservice.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResDto {
  Long id;
  String username;
  String firstName;
  String lastName;
  String email;
  String phoneNumber;
}
