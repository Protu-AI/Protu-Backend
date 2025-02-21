package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppPropertiesConfig;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.rabbitmq.EmailVerificationData;
import org.protu.userservice.dto.rabbitmq.RabbitMQMessage;
import org.protu.userservice.dto.request.VerifyEmailReqDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.exceptions.custom.InvalidOrExpiredOtpException;
import org.protu.userservice.exceptions.custom.UserEmailAlreadyVerifiedException;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.TokenMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OtpService {
  private final TokenMapper tokenMapper;
  private final UserRepository userRepository;
  private final JWTService jwtService;
  private final UserHelper userHelper;
  private final RedisTemplate<Object, Object> redisTemplate;
  private final AppPropertiesConfig properties;
  private final RabbitMQProducer producer;

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

  @Async
  public void sendEmail(String messageId, String to, Integer templateId, Object data){
    RabbitMQMessage<Object> queueMessage = new RabbitMQMessage<>(messageId, to, "protu@gmail.com",
        new RabbitMQMessage.Template<>(templateId, data),
        new RabbitMQMessage.MetaData("user-service", Timestamp.from(Instant.now())));
    producer.send(queueMessage);
  }

  public void checkIfUserEnteredOtpValid(String key, String userGivenOTP){
    if (!Objects.equals(redisTemplate.opsForValue().get(key), userGivenOTP)) {
      throw new InvalidOrExpiredOtpException(FailureMessages.INVALID_OR_EXPIRED_OTP.getMessage(userGivenOTP));
    }
  }

  public TokensResDto verifyUserEmailAndOTP(VerifyEmailReqDto requestDTO) {
    User user = userHelper.fetchUserOrThrow(requestDTO.email(), "email");
    if (user.getIsEmailVerified()) {
      throw new UserEmailAlreadyVerifiedException(FailureMessages.EMAIL_ALREADY_VERIFIED.getMessage());
    }
    checkIfUserEnteredOtpValid(properties.getOtp().getPrefix().getEmail() + user.getId(), requestDTO.OTP());
    return markUserEmailVerified(user);
  }

  public void sendOtp(int len, String redisKey, User user, Long OtpTtlInMillis) {
    String otp = generateOtp(len);
    redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMillis(OtpTtlInMillis));
    sendEmail("OTP_123", user.getEmail(), 1 ,
        new EmailVerificationData(user.getUsername(), otp, String.valueOf(13)));
  }

  public TokensResDto markUserEmailVerified(User user) {
    redisTemplate.opsForValue().getAndDelete(properties.getOtp().getPrefix().getEmail() + user.getId());
    user.setIsEmailVerified(true);
    userRepository.save(user);
    return tokenMapper.toTokensDto(user, jwtService);
  }
}
