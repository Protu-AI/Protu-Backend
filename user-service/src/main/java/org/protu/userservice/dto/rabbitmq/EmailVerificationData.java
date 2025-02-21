package org.protu.userservice.dto.rabbitmq;

public record EmailVerificationData(String username, String otp, String otpTtl) {}
