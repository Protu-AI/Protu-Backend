package org.protu.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonPropertyOrder({
    "userId",
    "email",
    "deactivatedBy",
    "deactivateReason",
    "reactivationAllowed"
})
public class DeactivateResDto {
  int userId;
  String email;
  String deactivatedBy = "SELF";
  String deactivateReason = "User requested account closure";
  Boolean reactivationAllowed = Boolean.TRUE;
}
