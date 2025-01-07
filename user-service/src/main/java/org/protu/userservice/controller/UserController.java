package org.protu.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.SuccessMessages;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.dto.request.FullUpdateReqDto;
import org.protu.userservice.dto.request.PartialUpdateReqDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserResDto;
import org.protu.userservice.service.JWTService;
import org.protu.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.protu.userservice.helper.SuccessResponseHelper.buildResponse;

@RestController
@RequestMapping("/api/${api.version}/users")
@RequiredArgsConstructor
public class UserController {
  private final JWTService jwtService;
  private final UserService userService;
  @Value("${api.version}")
  private String apiVersion;

  private Long getIdFromAuthHeader(String authHeader) {
    String token = jwtService.getTokenFromHeader(authHeader);
    return jwtService.getUserIdFromToken(token);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> getUserById(
      @PathVariable("id") Long userId,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    UserResDto userResDto = userService.getUserById(userId, getIdFromAuthHeader(authHeader));
    return buildResponse(apiVersion, request, HttpStatus.OK, userResDto, SuccessMessages.GET_USER_MSG.message);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> fullUpdateUser(
      @PathVariable("id") Long userId,
      @Validated @RequestBody FullUpdateReqDto userUpdateDto,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    UserResDto userResDto = userService.fullUpdateUser(userId, getIdFromAuthHeader(authHeader), userUpdateDto);
    return buildResponse(apiVersion, request, HttpStatus.OK, userResDto, SuccessMessages.UPDATE_USER_MSG.message);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> partialUpdateUser(
      @PathVariable("id") Long userId,
      @Validated @RequestBody PartialUpdateReqDto userUpdateDto,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    UserResDto userResDto = userService.partialUpdateUser(userId, getIdFromAuthHeader(authHeader), userUpdateDto);
    return buildResponse(apiVersion, request, HttpStatus.OK, userResDto, SuccessMessages.UPDATE_USER_MSG.message);  }


  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<ApiResponse<DeactivateResDto>> deactivateUser(
      @PathVariable("id") Long userId,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    DeactivateResDto deactivateResDto = userService.deactivateUser(userId, getIdFromAuthHeader(authHeader));
    return buildResponse(apiVersion, request, HttpStatus.OK, deactivateResDto, SuccessMessages.DEACTIVATE_USER_MSG.message);
  }
}
