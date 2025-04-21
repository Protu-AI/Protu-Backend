package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.rabbitmq.EmailData;
import org.protu.userservice.dto.rabbitmq.RabbitMessage;
import org.protu.userservice.exceptions.custom.InvalidOrExpiredOtpException;
import org.protu.userservice.model.User;
import org.protu.userservice.producer.EmailNotificationProducer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

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

  public void sendOtp(int len, String redisKey, User user, Long otpTtlInMillis, String templateId) {
    String otp = generateOtp(len);
    redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMillis(otpTtlInMillis));
    long durationInMinutes = otpTtlInMillis / (1000 * 60);
    EmailData data = new EmailData(templateId, user.getEmail(), "protu@gmail.com", user.getUsername(),
        new EmailData.Otp(otp, String.valueOf(durationInMinutes)));

    sendEmail(data);
  }

  @Async
  public void sendEmail(EmailData data) {
    RabbitMessage<EmailData> message = new RabbitMessage<>("email-notification", "task", data);
    producer.send(message);
  }
}
