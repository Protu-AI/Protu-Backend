package org.protu.userservice.service;

import org.protu.userservice.dto.request.VerifyReqDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.model.User;

public interface VerificationCodeService {
  TokensResDto verifyUserEmailAndCode(VerifyReqDto requestDTO);

  String generateVerificationCode(int len);

  void sendVerificationCode(User user);
}
