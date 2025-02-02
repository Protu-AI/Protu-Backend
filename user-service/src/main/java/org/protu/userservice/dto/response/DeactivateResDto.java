package org.protu.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"publicId", "email", "deactivatedBy", "deactivateReason", "reactivationAllowed"})
public record DeactivateResDto (String publicId, String email, String deactivatedBy, String deactivateReason, Boolean reactivationAllowed){
  public DeactivateResDto {
    if(deactivatedBy == null || deactivatedBy.isEmpty())
      deactivatedBy = "SELF";
    if(deactivateReason == null || deactivateReason.isEmpty())
      deactivateReason = "User requested account closure";
    if(reactivationAllowed == null)
      reactivationAllowed = Boolean.TRUE;
  }
}
