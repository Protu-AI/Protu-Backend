package org.protu.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class signUpResDto {
  String email;
  Boolean emailSent;
}
