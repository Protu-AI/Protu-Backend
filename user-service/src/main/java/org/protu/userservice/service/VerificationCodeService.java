package org.protu.userservice.service;

import org.protu.userservice.dto.request.VerifyEmailReqDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.model.User;

public interface VerificationCodeService {
  void validateCode(User user, String code);

  TokensResDto verifyUserEmailAndCode(VerifyEmailReqDto requestDTO);

  String generateVerificationCode(int len);

  void sendVerificationCode(User user, String subject);
}
