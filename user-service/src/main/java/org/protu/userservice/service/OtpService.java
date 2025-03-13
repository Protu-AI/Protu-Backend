package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.rabbitmq.EmailVerificationData;
import org.protu.userservice.dto.rabbitmq.RabbitMQMessage;
import org.protu.userservice.exceptions.custom.InvalidOrExpiredOtpException;
import org.protu.userservice.model.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {
  private final RedisTemplate<Object, String> redisTemplate;
  private final EmailNotificationProducer producer;

  private String generateOtp(int len) {
    String characters = "0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder Otp = new StringBuilder();
    for (int i = 0; i < len; i++) {
      int index = random.nextInt(characters.length());
      Otp.append(characters.charAt(index));
    }

    return Otp.toString();
  }

  public void verifyUserEnteredOtpOrThrow(String key, String userGivenOTP) {
    if (!Objects.equals(redisTemplate.opsForValue().get(key), userGivenOTP)) {
      throw new InvalidOrExpiredOtpException(FailureMessages.INVALID_OR_EXPIRED_OTP.getMessage(userGivenOTP));
    }
  }

  public void sendOtp(int len, String redisKey, User user, Long otpTtlInMillis) {
    String otp = generateOtp(len);
    redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMillis(otpTtlInMillis));
    long durationInMinutes = otpTtlInMillis / (1000 * 60);
    sendEmail(user.getEmail(), 1, new EmailVerificationData(user.getUsername(), otp, String.valueOf(durationInMinutes)));
  }

  @Async
  public void sendEmail(String to, Integer templateId, Object data) {
    RabbitMQMessage<Object> message = new RabbitMQMessage<>(
        UUID.randomUUID().toString(), to, "protu@gmail.com",
        new RabbitMQMessage.Template<>(templateId, data),
        new RabbitMQMessage.MetaData("user-service", Timestamp.from(Instant.now())));
    producer.send(message);
  }
}
