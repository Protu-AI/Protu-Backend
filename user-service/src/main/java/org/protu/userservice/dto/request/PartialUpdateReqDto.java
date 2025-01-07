package org.protu.userservice.dto.request;

public record PartialUpdateReqDto (String username, String firstName, String lastName, String email, String phoneNumber, String password){}
