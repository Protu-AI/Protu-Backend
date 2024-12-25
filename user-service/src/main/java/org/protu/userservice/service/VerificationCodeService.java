package org.protu.userservice.service;

import org.protu.userservice.dto.TokensResponseDto;
import org.protu.userservice.dto.VerificationRequestDTO;
import org.protu.userservice.model.User;

public interface VerificationCodeService {
  TokensResponseDto verifyUserEmailAndCode(VerificationRequestDTO requestDTO);

  String generateVerificationCode(int len);

  void sendVerificationCode(User user);
}
