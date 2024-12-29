package org.protu.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.SuccessMessages;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.dto.request.UpdateReqDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserResDto;
import org.protu.userservice.service.JWTService;
import org.protu.userservice.service.UserService;
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

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> getUserById(
      @PathVariable("id") Long userId,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    String token = jwtService.getTokenFromHeader(authHeader);
    Long authUserId = jwtService.getUserIdFromToken(token);
    UserResDto userResDto = userService.getUserById(userId, authUserId);
    return buildResponse(request, HttpStatus.OK, userResDto, SuccessMessages.GET_USER_MSG.message);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> updateUser(
      @PathVariable("id") Long userId,
      @Validated @RequestBody UpdateReqDto userUpdateDto,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    String token = jwtService.getTokenFromHeader(authHeader);
    Long authUserId = jwtService.getUserIdFromToken(token);
    UserResDto userResDto = userService.updateUser(userId, authUserId, userUpdateDto);
    return buildResponse(request, HttpStatus.OK, userResDto, SuccessMessages.UPDATE_USER_MSG.message);
  }


  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<ApiResponse<DeactivateResDto>> deactivateUser(
      @PathVariable("id") Long userId,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    String token = jwtService.getTokenFromHeader(authHeader);
    Long authUserId = jwtService.getUserIdFromToken(token);
    DeactivateResDto deactivateResDto = userService.deactivateUser(userId, authUserId);
    return buildResponse(request, HttpStatus.OK, deactivateResDto, SuccessMessages.DEACTIVATE_USER_MSG.message);
  }
}
