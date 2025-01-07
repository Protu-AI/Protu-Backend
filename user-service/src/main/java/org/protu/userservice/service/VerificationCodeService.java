package org.protu.userservice.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.request.VerifyEmailReqDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.exceptions.custom.InvalidVerificationCodeException;
import org.protu.userservice.exceptions.custom.UserEmailAlreadyVerifiedException;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.TokenMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {
  private final EmailService emailService;
  private final UserRepository userRepository;
  private final JWTService jwtService;
  private final TemplateEngine templateEngine;
  private final TokenMapper tokenMapper;
  private final UserHelper userHelper;

  @Value("${verification-code-expiration-time}")
  private String codeExpiryTime;

  public void validateCode(User user, String code) {
    String storedCode = user.getVerificationCode().trim();
    Timestamp codeExpiryTime = user.getCodeExpiryDate();
    code = code.trim();

    if (!storedCode.equals(code)) {
      throw new InvalidVerificationCodeException(FailureMessages.INVALID_VERIFICATION_CODE.getMessage(code));
    }

    if (codeExpiryTime.before(Timestamp.from(Instant.now()))) {
      throw new InvalidVerificationCodeException(FailureMessages.EXPIRED_VERIFICATION_CODE.getMessage(code));
    }
  }

  public TokensResDto verifyUserEmailAndCode(VerifyEmailReqDto requestDTO) {
    User user = userHelper.fetchUserOrThrow(requestDTO.email(), "email");
    validateCode(user, requestDTO.verificationCode());
    return verifyUserEmail(user);
  }

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
      throw new MailSendException(FailureMessages.MAIL_SENDING_FAILED.getMessage(), e);
    }
  }

  public TokensResDto verifyUserEmail(User user) {
    if (user.getIsEmailVerified()) {
      throw new UserEmailAlreadyVerifiedException(FailureMessages.EMAIL_ALREADY_VERIFIED.getMessage());
    }
    
    user.setCodeExpiryDate(Timestamp.from(Instant.now()));
    user.setIsEmailVerified(true);
    userRepository.save(user);
    return tokenMapper.toTokensDto(user, jwtService);
  }
}
