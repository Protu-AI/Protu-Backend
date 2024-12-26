package org.protu.userservice.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.request.VerifyEmailReqDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.exceptions.custom.InvalidVerificationCodeException;
import org.protu.userservice.exceptions.custom.UserEmailAlreadyVerifiedException;
import org.protu.userservice.exceptions.custom.UserNotFoundException;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.protu.userservice.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {
  private final EmailServiceImpl emailService;
  private final UserRepository userRepository;
  private final JWTServiceImpl jwtService;
  private final TemplateEngine templateEngine;

  @Value("${verification-code-expiration-time}")
  private String codeExpiryTime;

  @Override
  public void validateCode(User user, String code) {
    String storedCode = user.getVerificationCode().trim();
    Timestamp codeExpiryTime = user.getCodeExpiryDate();
    code = code.trim();

    if (!storedCode.equals(code)) {
      throw new InvalidVerificationCodeException("The verification code entered is incorrect. Please check the code and ensure it's entered correctly.");
    }

    if (codeExpiryTime.before(Timestamp.from(Instant.now()))) {
      throw new InvalidVerificationCodeException("The verification code entered has expired. Please request a new verification code.");
    }
  }

  @Override
  public TokensResDto verifyUserEmailAndCode(VerifyEmailReqDto requestDTO) {
    Optional<User> userOpt = userRepository.findByEmail(requestDTO.getEmail());
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("User with email:" + requestDTO.getEmail() + " not found.");
    }

    User user = userOpt.get();
    validateCode(user, requestDTO.getVerificationCode());
    return verifyUserEmail(user);
  }

  @Override
  public String generateVerificationCode(int len) {
    String characters = "0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder verificationCode = new StringBuilder();
    for (int i = 0; i < len; i++) {
      int index = random.nextInt(characters.length());
      verificationCode.append(characters.charAt(index));
    }

    return verificationCode.toString();
  }

  @Override
  public void sendVerificationCode(User user, String subject) {
    String generatedCode = generateVerificationCode(5);
    user.setVerificationCode(generatedCode);
    user.setCodeExpiryDate(Timestamp.valueOf(LocalDateTime.now().plusMinutes(Long.parseLong(codeExpiryTime))));
    userRepository.save(user);

    Context context = new Context();
    context.setVariable("verificationCode", generatedCode);
    String htmlContent = templateEngine.process("verification-email", context);
    try {
      emailService.sendEmail(
          user.getEmail(),
          subject,
          htmlContent
      );
    } catch (MessagingException e) {
      throw new MailSendException("Failed to send verification email.");
    }
  }

  public TokensResDto verifyUserEmail(User user) {
    if (user.getIsEmailVerified()) {
      throw new UserEmailAlreadyVerifiedException("The email for this user is already verified.");
    }
    user.setCodeExpiryDate(Timestamp.from(Instant.now()));
    user.setIsEmailVerified(true);
    userRepository.save(user);

    return TokensResDto.builder()
        .userId(user.getId())
        .accessToken(jwtService.generateAccessToken(user.getId()))
        .refreshToken(jwtService.generateRefreshToken(user.getId()))
        .refreshTokenExpiresIn(jwtService.getRefreshTokenDuration())
        .accessTokenExpiresIn(jwtService.getAccessTokenDuration())
        .tokenType("Bearer")
        .build();
  }
}
