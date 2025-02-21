package org.protu.userservice.dto;

public record EmailVerificationData(String username, String otp, String otpTtl) {}
