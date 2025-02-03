package org.protu.userservice.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppPropertiesConfig;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.request.VerifyEmailReqDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.exceptions.custom.InvalidOrExpiredOtpException;
import org.protu.userservice.exceptions.custom.UserEmailAlreadyVerifiedException;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.TokenMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OtpService {
  private final TokenMapper tokenMapper;
  private final UserRepository userRepository;
  private final EmailService emailService;
  private final JWTService jwtService;
  private final TemplateEngine templateEngine;
  private final UserHelper userHelper;
  private final RedisTemplate<Object, Object> redisTemplate;
  private final AppPropertiesConfig properties;

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

  private void sendEmail(String email, String generatedCode, String subject){
    Context context = new Context();
    context.setVariable("verificationCode", generatedCode);
    String htmlContent = templateEngine.process("verification-email", context);
    try {
      emailService.sendEmail(email, subject, htmlContent);
    } catch (MessagingException e) {
      throw new MailSendException("Failed to send email", e);
    }
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

  public void sendOtp(int len, String redisKey, User user, String subject, Long OtpTtlInMillis) {
    String generatedCode = generateOtp(len);
    redisTemplate.opsForValue().set(redisKey, generatedCode, Duration.ofMillis(OtpTtlInMillis));
    sendEmail(user.getEmail(), generatedCode, subject);
  }

  public TokensResDto markUserEmailVerified(User user) {
    redisTemplate.opsForValue().getAndDelete(properties.getOtp().getPrefix().getEmail() + user.getId());
    user.setIsEmailVerified(true);
    userRepository.save(user);
    return tokenMapper.toTokensDto(user, jwtService);
  }
}
