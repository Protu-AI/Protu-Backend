package org.protu.userservice.service;

import org.protu.userservice.dto.request.ForgotPasswordReqDto;
import org.protu.userservice.dto.request.LoginReqDto;
import org.protu.userservice.dto.request.RegisterReqDto;
import org.protu.userservice.dto.request.ResetPasswordReqDto;
import org.protu.userservice.dto.response.RegisterResDto;
import org.protu.userservice.dto.response.TokensResDto;

public interface AuthService {
  RegisterResDto registerUser(RegisterReqDto registerRequest);

  void validateUserIdentifier(String userIdentifier);

  TokensResDto authenticate(LoginReqDto loginReqDto);

  void forgotPassword(ForgotPasswordReqDto requestDto);

  void resetPassword(ResetPasswordReqDto requestDto);
}
